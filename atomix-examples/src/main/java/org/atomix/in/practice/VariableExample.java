package org.atomix.in.practice;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.atomix.AtomixReplica;
import io.atomix.catalyst.concurrent.Listener;
import io.atomix.variables.AbstractDistributedValue;
import io.atomix.variables.AbstractDistributedValue.ChangeEvent;

/**
 * This class demonstrates how to use Distributed Variable features.
 *
 * NOTICE : Not all possible methods are covered (for example Get with Consistency Level ot operations with TTL).
 */
public class VariableExample {

  /**
   * Set Value to {@link io.atomix.variables.DistributedValue} by Key and return {@link CompletableFuture} wth no result.
   *
   * @param replica Atomix cluster replica.
   * @param key Variable key.
   * @param value new Value.
   * @param <T> Type of variable value.
   * @return {@link CompletableFuture} with no result.
   */
  public <T> CompletableFuture<Void> setValue(AtomixReplica replica, String key, T value) {

    return replica.<T>getValue(key).thenCompose(dv -> dv.set(value));
  }

  /**
   * Get {@link io.atomix.variables.DistributedValue} by  Key and then get actual value as {@link CompletableFuture}.
   *
   * @param replica Atomix cluster replica.
   * @param key Variable key.
   * @param <T> Type of variable value.
   * @return {@link CompletableFuture} with actual value result.
   */
  public <T> CompletableFuture<T> readValue(AtomixReplica replica, String key) {

    return replica.<T>getValue(key).thenCompose(AbstractDistributedValue::get);
  }

  /**
   * Delete variable by Key.
   *
   * @param replica Atomix cluster replica.
   * @param key Variable key.
   * @return {@link CompletableFuture} with no result.
   */
  public CompletableFuture<Void> deleteValue(AtomixReplica replica, String key) {

    return replica.getValue(key).thenCompose(AbstractDistributedValue::delete);
  }

  /**
   * Add listener to variable to listen events related to this variable.
   *
   * @param replica Atomix cluster replica.
   * @param key Variable key.
   * @param listener Listener.
   * @param <T> Type of variable value.
   * @return {@link CompletableFuture} that contains same listener.
   */
  public <T> CompletableFuture<Listener<ChangeEvent<T>>> listenValueChanges(AtomixReplica replica, String key,
                                                                            Consumer<ChangeEvent<T>> listener) {

    return replica.<T>getValue(key).thenCompose(distributedValue -> distributedValue.onChange(listener));
  }

  /**
   * Set value only if expected value equals actual.
   *
   * @param replica Atomix cluster replica.
   * @param key Variable key.
   * @param expected Expected value that should match actual.
   * @param value New value of variable that should be set if compare operation will be successful.
   * @param <T> Type of variable value.
   * @return {@link CompletableFuture} with result of operation.
   */
  public <T> CompletableFuture<Boolean> compareAndSetValue(AtomixReplica replica, String key, T expected, T value) {

    return replica.<T>getValue(key).thenCompose(distributedValue -> distributedValue.compareAndSet(expected, value));
  }

  //  TODO need time to understand
//  public <T> CompletableFuture<DistributedValue<T>> changeValueStateToOpen(AtomixReplica replica, String key) {
//
//    return replica.<T>getValue(key).thenCompose(AbstractResource::open);
//  }
//
//  public CompletableFuture<Void> changeValueStateToClose(AtomixReplica replica, String key) {
//
//    return replica.getValue(key).thenCompose(AbstractResource::close);
//  }
//
//  public <T> CompletableFuture<DistributedValue<T>> getDistributedValue(AtomixReplica replica, String key) {
//
//    return replica.getValue(key);
//  }

}
