package org.atomix.in.practice;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.atomix.AtomixReplica;
import io.atomix.catalyst.concurrent.Listener;
import io.atomix.variables.AbstractDistributedValue;
import io.atomix.variables.AbstractDistributedValue.ChangeEvent;

public class VariableExample {

  public <T> CompletableFuture<Void> setValue(AtomixReplica replica, String key, T value) {

    return replica.<T>getValue(key).thenCompose(dv -> dv.set(value));
  }

  public <T> CompletableFuture<T> readValue(AtomixReplica replica, String key) {

    return replica.<T>getValue(key).thenCompose(AbstractDistributedValue::get);
  }

  public CompletableFuture<Void> deleteValue(AtomixReplica replica, String key) {

    return replica.getValue(key).thenCompose(AbstractDistributedValue::delete);
  }

  public <T> CompletableFuture<Listener<ChangeEvent<T>>> listenValueChanges(AtomixReplica replica, String key,
                                                                            Consumer<ChangeEvent<T>> listener) {

    return replica.<T>getValue(key).thenCompose(distributedValue -> distributedValue.onChange(listener));
  }
}
