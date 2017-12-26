package org.atomix.in.practice;

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

  @Test
  public void setAndCheckVariableCorrectlyTest() {

    String variableKey = "test-key";

    String variableMessage = "Hello world!";

    //Build cluster
    CompletableFuture<AtomixReplica[]> clusterFuture = CLUSTER_EXAMPLE.buildClusterOfTwoNodes();

    Boolean resultOfComparison = clusterFuture.thenCompose(atomixReplicas ->
        VARIABLE_EXAMPLE.setValue(atomixReplicas[0], variableKey, variableMessage)
            .thenCompose(aVoid -> CompletableFuture.completedFuture(atomixReplicas[1]))
    ).thenCompose(atomixReplica -> VARIABLE_EXAMPLE.readValue(atomixReplica, variableKey))
        .thenCompose(value -> CompletableFuture.completedFuture(variableMessage.equals(value)))
        .join();

    assertTrue(resultOfComparison);
  }

  @Test
  public void setAndCheckVariableIncorrectlyTest() {

    String variableKey = "test-key";
    String variableKey2 = "test-key2";

    String variableMessage = "Hello world!";

    //Build cluster
    CompletableFuture<AtomixReplica[]> clusterFuture = CLUSTER_EXAMPLE.buildClusterOfTwoNodes();

    Boolean resultOfComparison = clusterFuture.thenCompose(atomixReplicas ->
        VARIABLE_EXAMPLE.setValue(atomixReplicas[0], variableKey, variableMessage)
            .thenCompose(aVoid -> CompletableFuture.completedFuture(atomixReplicas[1]))
    ).thenCompose(atomixReplica -> VARIABLE_EXAMPLE.readValue(atomixReplica, variableKey2))
        .thenCompose(value -> CompletableFuture.completedFuture(Objects.isNull(value)))
        .join();

    assertTrue(resultOfComparison);
  }

  @Test
  public void setAndDeleteVariableTest() {

    String variableKey = "test-key";

    String variableMessage = "Hello world!";

    //Build cluster
    CompletableFuture<AtomixReplica[]> clusterFuture = CLUSTER_EXAMPLE.buildClusterOfTwoNodes();

    Boolean resultOfComparison = clusterFuture.thenCompose(atomixReplicas ->
        VARIABLE_EXAMPLE.setValue(atomixReplicas[0], variableKey, variableMessage)
            .thenCompose(aVoid -> CompletableFuture.completedFuture(atomixReplicas[1]))
    ).thenCompose(atomixReplica -> VARIABLE_EXAMPLE.deleteValue(atomixReplica, variableKey)
        .thenCompose(aVoid -> CompletableFuture.completedFuture(atomixReplica)))
        .thenCompose(atomixReplica -> VARIABLE_EXAMPLE.readValue(atomixReplica, variableKey))
        .thenCompose(value -> CompletableFuture.completedFuture(Objects.isNull(value)))
        .join();

    assertTrue(resultOfComparison);
  }


  // TODO already investigating this feature and thinging about good example

  @Test
  public void listenEventChanges() {
    String valueKey = "test-key";

    Integer oldValue = 5;
    Integer newValue = 10;

    boolean[] results = new boolean[2];

    Consumer<ChangeEvent<Integer>> listener = che -> {
      results[0] = che.oldValue().equals(oldValue);
      results[1] = che.newValue().equals(newValue);
    };

    CompletableFuture<AtomixReplica[]> clusterFuture = CLUSTER_EXAMPLE.buildClusterOfTwoNodes();

    clusterFuture.thenCompose(atomixReplicas -> VARIABLE_EXAMPLE.setValue(atomixReplicas[0], valueKey, oldValue)
        .thenCompose(aVoid -> CompletableFuture.completedFuture(atomixReplicas)))
        .thenCompose(atomixReplicas -> VARIABLE_EXAMPLE.listenValueChanges(atomixReplicas[1], valueKey, listener)
            .thenCompose(fn -> CompletableFuture.completedFuture(atomixReplicas[0])))
        .thenCompose(atomixReplica -> VARIABLE_EXAMPLE.setValue(atomixReplica, valueKey, newValue)).join();

    assertEquals(true, results[0]);
    assertEquals(true, results[1]);
  }
}
