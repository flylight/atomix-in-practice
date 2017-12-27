package org.atomix.in.practice;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.atomix.AtomixReplica;
import io.atomix.variables.AbstractDistributedValue.ChangeEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VariableExampleTest {

  private static final ClusterExample CLUSTER_EXAMPLE = new ClusterExample();
  private static final VariableExample VARIABLE_EXAMPLE = new VariableExample();

  private static AtomixReplica[] cluster;

  @BeforeClass
  public static void setupCluster() {
    cluster = CLUSTER_EXAMPLE.buildClusterOfTwoNodes().join();
  }

  @AfterClass
  public static void stopCluster() {
    cluster[0].shutdown().thenCompose(aVoid -> cluster[1].server().shutdown()).join();
  }

  @Test
  public void setAndCheckVariableCorrectlyTest() {

    String variableKey = "test-key";

    String variableMessage = "Hello world!";

    Boolean resultOfComparison = VARIABLE_EXAMPLE.setValue(cluster[0], variableKey, variableMessage)
        .thenCompose(aVoid -> CompletableFuture.completedFuture(cluster[1]))
        .thenCompose(atomixReplica -> VARIABLE_EXAMPLE.readValue(atomixReplica, variableKey))
        .thenCompose(value -> CompletableFuture.completedFuture(variableMessage.equals(value)))
        .join();

    assertTrue(resultOfComparison);
  }

  @Test
  public void setAndCheckVariableIncorrectlyTest() {

    String variableKey = "test-key";
    String variableKey2 = "test-key2";

    String variableMessage = "Hello world!";

    Boolean resultOfComparison = VARIABLE_EXAMPLE.setValue(cluster[0], variableKey, variableMessage)
        .thenCompose(aVoid -> CompletableFuture.completedFuture(cluster[1]))
        .thenCompose(atomixReplica -> VARIABLE_EXAMPLE.readValue(atomixReplica, variableKey2))
        .thenCompose(value -> CompletableFuture.completedFuture(Objects.isNull(value)))
        .join();

    assertTrue(resultOfComparison);
  }

  @Test
  public void setAndDeleteVariableTest() {

    String variableKey = "test-key";

    String variableMessage = "Hello world!";

    Boolean resultOfComparison =
        VARIABLE_EXAMPLE.setValue(cluster[0], variableKey, variableMessage)
            .thenCompose(aVoid -> CompletableFuture.completedFuture(cluster[1]))
            .thenCompose(atomixReplica -> VARIABLE_EXAMPLE.deleteValue(atomixReplica, variableKey)
                .thenCompose(aVoid -> CompletableFuture.completedFuture(atomixReplica)))
            .thenCompose(atomixReplica -> VARIABLE_EXAMPLE.readValue(atomixReplica, variableKey))
            .thenCompose(value -> CompletableFuture.completedFuture(Objects.isNull(value)))
            .join();

    assertTrue(resultOfComparison);
  }

  @Test
  public void listenEventChanges() throws InterruptedException {
    String valueKey = "test-key";

    Integer oldValue = 5;
    Integer newValue = 10;

    boolean[] results = new boolean[2];

    Consumer<ChangeEvent<Integer>> listener = che -> {

      results[0] = che.oldValue().equals(oldValue);
      results[1] = che.newValue().equals(newValue);

      // Notify execution of test
      synchronized (VariableExampleTest.this) {
        notify();
      }

    };

    VARIABLE_EXAMPLE.setValue(cluster[0], valueKey, oldValue)
        .thenCompose(aVoid -> CompletableFuture.completedFuture(cluster))
        .thenCompose(atomixReplicas -> VARIABLE_EXAMPLE.listenValueChanges(atomixReplicas[1], valueKey, listener)
            .thenCompose(fn -> CompletableFuture.completedFuture(atomixReplicas[0])))
        .thenCompose(atomixReplica -> VARIABLE_EXAMPLE.setValue(atomixReplica, valueKey, newValue)).join();

    //Test going to wait until listener will be invoked and notify execution
    synchronized (this) {
      wait(10000);
    }

    assertEquals(true, results[0]);
    assertEquals(true, results[1]);
  }
}
