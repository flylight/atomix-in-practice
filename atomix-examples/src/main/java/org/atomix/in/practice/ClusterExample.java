package org.atomix.in.practice;

import java.util.concurrent.CompletableFuture;

import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;

/**
 * Examples of creation Atomix cluster.
 */
public class ClusterExample {

  /**
   * Creates two {@link AtomixReplica} instances using composition of {@link CompletableFuture}. It means that
   * we link creation of replicas one by one where second replica join previous. Also we manage array which will be
   * returned as {@link CompletableFuture} and contains two replicas as a cluster.
   *
   * Both {@link AtomixReplica} start on localhost (127.0.0.1) but first replica uses port 8091 and second 8092.
   *
   * Using {@link CompletableFuture} functionality we build non-blocking approach to create cluster which is also
   * {@link CompletableFuture} that allow users to continue build non-blocking applications.
   *
   * @return {@link CompletableFuture} of {@link AtomixReplica} joined into single cluster.
   */
  public CompletableFuture<AtomixReplica[]> buildClusterOfTwoNodes() {

    String serverHost = "127.0.0.1";

    int node1Port = 8091;
    int node2Port = 8092;

    //Here will be our cluster
    AtomixReplica[] cluster = new AtomixReplica[2];

    return AtomixServer.createInMemoryNettyTransportReplica(serverHost, node1Port)
        .bootstrap()
        // When replica 1 finished it's creation -> and add it into array
        .whenComplete((r1, exception) -> cluster[0] = r1)
        // When replica 1 finished it's creation -> start creation of 2nd replica that joining previous
        .thenCompose(r -> AtomixServer.createInMemoryNettyTransportReplica(serverHost, node2Port)
            .join(new Address(serverHost, node1Port)))
        // When second replica finished it's creation -> add it into array
        .whenComplete((r2, exception) -> cluster[1] = r2)
        // When second replica finished creation return already populated array
        .thenApply(r -> cluster);
  }

}
