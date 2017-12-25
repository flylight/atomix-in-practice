package org.atomix.in.practice;

import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;

/**
 * Simple class that produce method to build Atomix replicas
 */
public class AtomixServer {

  /**
   * Creates new {@link AtomixReplica} based on IN MEMORY storage and NETTY transport protocol.
   *
   * @param host desired replica host.
   * @param port desired replica port.
   *
   * @return in-memory and netty transport {@link AtomixReplica}  instance attached to provided host and port.
   */
  public static AtomixReplica createInMemoryNettyTransportReplica(String host, int port) {

    Storage storageConfig = Storage.builder()
        .withStorageLevel(StorageLevel.MEMORY)
        .build();

    Transport nettyTransport = NettyTransport.builder()
        .build();

    return AtomixReplica.builder(new Address(host, port))
        .withStorage(storageConfig)
        .withTransport(nettyTransport)
        .build();
  }

}
