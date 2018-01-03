package org.atomix.in.practice;

import java.util.concurrent.CompletableFuture;

import io.atomix.AtomixReplica;
import io.atomix.variables.AbstractDistributedValue;
import io.atomix.variables.DistributedLong;


/**
 * This class shows examples related to Long type variable. It skips all that relates to set & get
 * because all of them already covered on {@link VariableExample}.
 * Here are examples of counters but, also,not including approaches to get value before increment / decrement.
 */
public class LongExample {

  /**
   * Set value to Long type distributed variable.
   *
   * @param replica Atomix Replica.
   * @param key     Variable key.
   * @param value   Variable value.
   * @return {@link CompletableFuture} without result.
   */
  public CompletableFuture<Void> setValue(AtomixReplica replica, String key, Long value) {

    return replica.getLong(key).thenCompose(distributedLong -> distributedLong.set(value));
  }

  /**
   * Get value.
   *
   * @param replica Atomix Replica.
   * @param key     Variable key.
   * @return {@link CompletableFuture} with actual result of Long Distributed Variable.
   */
  public CompletableFuture<Long> getValue(AtomixReplica replica, String key) {

    return replica.getLong(key).thenCompose(AbstractDistributedValue::get);
  }

  /**
   * Increment variable value and get it actual value.
   *
   * @param replica Atomix Replica.
   * @param key     Variable key.
   * @return {@link CompletableFuture} with already incremented value.
   */
  public CompletableFuture<Long> incrementAndGetValue(AtomixReplica replica, String key) {

    return replica.getLong(key).thenCompose(DistributedLong::incrementAndGet);
  }

  /**
   * Decrement variable value and get it actual value.
   *
   * @param replica Atomix Replica.
   * @param key     Variable key.
   * @return {@link CompletableFuture} with already decremented value.
   */
  public CompletableFuture<Long> decrementAndGetValue(AtomixReplica replica, String key) {

    return replica.getLong(key).thenCompose(DistributedLong::decrementAndGet);
  }

}
