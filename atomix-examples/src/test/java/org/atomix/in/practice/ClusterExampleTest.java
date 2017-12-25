package org.atomix.in.practice;

import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.atomix.AtomixReplica;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClusterExampleTest {

  private static final ClusterExample CLUSTER_EXAMPLE = new ClusterExample();

  @Test
  public void testSimpleCluster() {

    //Build cluster
    CompletableFuture<AtomixReplica[]> clusterFuture = CLUSTER_EXAMPLE.buildClusterOfTwoNodes();

    //Assign task to test built cluster and wait until testing finish
    Boolean isServerSuccessfullyTestedAndAccomplished = clusterFuture.thenCompose(this::testCluster).join();

    //Check that cluster built, tested and accomplished successfully
    assertTrue(isServerSuccessfullyTestedAndAccomplished);
  }

  private CompletableFuture<Boolean> testCluster(AtomixReplica[] cluster) {

    assertEquals(2, cluster.length);
    assertTrue(Objects.nonNull(cluster[0]));
    assertTrue(Objects.nonNull(cluster[1]));

    assertTrue(cluster[0].server().isRunning());
    assertTrue(cluster[1].server().isRunning());

    return cluster[0].server()
        .shutdown()
        .thenCompose(aVoid -> cluster[1].server().shutdown())
        .thenCompose(aVoid -> CompletableFuture
            .completedFuture(!cluster[0].server().isRunning() && !cluster[1].server().isRunning()));
  }

}
