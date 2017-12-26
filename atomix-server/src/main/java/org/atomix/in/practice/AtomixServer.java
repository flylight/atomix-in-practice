package org.atomix.in.practice;

import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;

/**
 * Simple class that produce method to build Atomix replicas.
 *
 * Detailed description of all available replica configuration you can
 * find on official website : http://atomix.io/atomix/docs/configuration/
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
