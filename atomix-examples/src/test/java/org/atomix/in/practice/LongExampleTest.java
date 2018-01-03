package org.atomix.in.practice;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.atomix.AtomixReplica;

import static org.junit.Assert.assertEquals;

public class LongExampleTest {

  private static final ClusterExample CLUSTER_EXAMPLE = new ClusterExample();

  private static final LongExample LONG_EXAMPLE = new LongExample();

  private static final String VALUE_KEY = "test-key";

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
  public void multithreadingIncrement() {

    ExecutorService threadPool = Executors.newFixedThreadPool(2);

    LONG_EXAMPLE.setValue(cluster[0], VALUE_KEY, 0L).join();

    List<Future<Long>> results = new ArrayList<>();

    //Adding tasks that increment counter using random replica from cluster
    for (int i = 0; i < 10; i++) {
      results.add(threadPool.submit(() ->
          LONG_EXAMPLE.incrementAndGetValue(cluster[new Random().nextInt(1)], VALUE_KEY).join()));
    }

    //Wait all tasks
    results.forEach(future -> {
      try {
        future.get();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    Long result = LONG_EXAMPLE.getValue(cluster[1], VALUE_KEY).join();

    assertEquals(Long.valueOf(10), result);
  }

  @Test
  public void multithreadingDecrement() {

    ExecutorService threadPool = Executors.newFixedThreadPool(2);

    LONG_EXAMPLE.setValue(cluster[0], VALUE_KEY, 100L).join();

    List<Future<Long>> results = new ArrayList<>();

    //Adding tasks that decrement counter using random replica from cluster
    for (int i = 0; i < 10; i++) {
      results.add(threadPool.submit(() ->
          LONG_EXAMPLE.decrementAndGetValue(cluster[new Random().nextInt(1)], VALUE_KEY).join()));
    }

    //Wait all tasks
    results.forEach(future -> {
      try {
        future.get();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    Long result = LONG_EXAMPLE.getValue(cluster[1], VALUE_KEY).join();

    assertEquals(Long.valueOf(90), result);
  }

  @Test
  public void concurrentModificationDecrement() {

    ExecutorService threadPool = Executors.newFixedThreadPool(2);

    LONG_EXAMPLE.setValue(cluster[0], VALUE_KEY, 50L).join();

    List<Future<Long>> results = new ArrayList<>();

    //Worker 1 : add 10 tasks to make parallel incrementing
    new Thread() {
      @Override
      public synchronized void start() {
        for (int i = 0; i < 10; i++) {
          results.add(threadPool.submit(() ->
              LONG_EXAMPLE.incrementAndGetValue(cluster[new Random().nextInt(1)], VALUE_KEY).join()));
        }
      }
    }.start();

    //Worker 2 : add 15 tasks to make parallel decrementing
    new Thread() {
      @Override
      public synchronized void start() {
        for (int i = 0; i < 15; i++) {
          results.add(threadPool.submit(() ->
              LONG_EXAMPLE.decrementAndGetValue(cluster[new Random().nextInt(1)], VALUE_KEY).join()));
        }
      }
    }.start();

    //Wait all tasks
    results.forEach(future -> {
      try {
        future.get();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    Long result = LONG_EXAMPLE.getValue(cluster[1], VALUE_KEY).join();

    assertEquals(Long.valueOf(45), result);
  }
}
