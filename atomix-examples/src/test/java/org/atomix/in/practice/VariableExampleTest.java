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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VariableExampleTest {

  private static final String TEST_KEY = "test-key";
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

    String variableMessage = "Hello world!";

    Boolean resultOfComparison = VARIABLE_EXAMPLE.setValue(cluster[0], TEST_KEY, variableMessage)
        .thenCompose(aVoid -> CompletableFuture.completedFuture(cluster[1]))
        .thenCompose(atomixReplica -> VARIABLE_EXAMPLE.readValue(atomixReplica, TEST_KEY))
        .thenCompose(value -> CompletableFuture.completedFuture(variableMessage.equals(value)))
        .join();

    assertTrue(resultOfComparison);
  }

  @Test
  public void setAndCheckVariableIncorrectlyTest() {

    String variableKey2 = "test-key2";

    String variableMessage = "Hello world!";

    Boolean resultOfComparison = VARIABLE_EXAMPLE.setValue(cluster[0], TEST_KEY, variableMessage)
        .thenCompose(aVoid -> CompletableFuture.completedFuture(cluster[1]))
        .thenCompose(atomixReplica -> VARIABLE_EXAMPLE.readValue(atomixReplica, variableKey2))
        .thenCompose(value -> CompletableFuture.completedFuture(Objects.isNull(value)))
        .join();

    assertTrue(resultOfComparison);
  }

  @Test
  public void setAndDeleteVariableTest() {

    String variableMessage = "Hello world!";

    Boolean resultOfComparison =
        VARIABLE_EXAMPLE.setValue(cluster[0], TEST_KEY, variableMessage)
            .thenCompose(aVoid -> CompletableFuture.completedFuture(cluster[1]))
            .thenCompose(atomixReplica -> VARIABLE_EXAMPLE.deleteValue(atomixReplica, TEST_KEY)
                .thenCompose(aVoid -> CompletableFuture.completedFuture(atomixReplica)))
            .thenCompose(atomixReplica -> VARIABLE_EXAMPLE.readValue(atomixReplica, TEST_KEY))
            .thenCompose(value -> CompletableFuture.completedFuture(Objects.isNull(value)))
            .join();

    assertTrue(resultOfComparison);
  }

  @Test
  public void listenEventChanges() throws InterruptedException {

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

    VARIABLE_EXAMPLE.setValue(cluster[0], TEST_KEY, oldValue)
        .thenCompose(aVoid -> CompletableFuture.completedFuture(cluster))
        .thenCompose(atomixReplicas -> VARIABLE_EXAMPLE.listenValueChanges(atomixReplicas[1], TEST_KEY, listener)
            .thenCompose(fn -> CompletableFuture.completedFuture(atomixReplicas[0])))
        .thenCompose(atomixReplica -> VARIABLE_EXAMPLE.setValue(atomixReplica, TEST_KEY, newValue)).join();

    //Test going to wait until listener will be invoked and notify execution
    synchronized (this) {
      wait(10000);
    }

    assertEquals(true, results[0]);
    assertEquals(true, results[1]);
  }

  @Test
  public void compareAndSetVariableTest() {

    int value1 = 1;
    int value2 = 2;
    int value3 = 3;

    Boolean successfulResult = VARIABLE_EXAMPLE
        .setValue(cluster[0], TEST_KEY, value1).thenCompose(aVoid -> VARIABLE_EXAMPLE
            .compareAndSetValue(cluster[1], TEST_KEY, value1, value2)).join();

    Boolean unsuccessfulResult = VARIABLE_EXAMPLE
        .compareAndSetValue(cluster[1], TEST_KEY, value1, value3).join();

    assertTrue(successfulResult);
    assertFalse(unsuccessfulResult);
  }


  //  TODO doesn't work as expected
//   @Test
//  public void openAndCloseVariableStateTest() throws InterruptedException {
//
//    String initialValue = "initial";
//    String value = "test-value";
//
//    Boolean isClosed = VARIABLE_EXAMPLE.setValue(cluster[0], TEST_KEY, initialValue)
//        .thenCompose(aVoid -> VARIABLE_EXAMPLE.changeValueStateToClose(cluster[1], TEST_KEY))
//        .thenCompose(aVoid -> VARIABLE_EXAMPLE.getDistributedValue(cluster[1], TEST_KEY))
//        .thenCompose(distributedValue -> CompletableFuture.completedFuture(distributedValue.isClosed()))
//        .join();
//
//    Thread.sleep(5000);
//
//    Boolean isClosed2 = VARIABLE_EXAMPLE.getDistributedValue(cluster[1], TEST_KEY)
//        .thenCompose(distributedValue -> CompletableFuture.completedFuture(distributedValue.isClosed()))
//        .join();
//
//    assertFalse(isClosed);
//    assertTrue(isClosed2);
//
//  }
}
