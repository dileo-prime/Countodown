package de.doubleslash.innovationsmanagement.countodown.backend;

import java.time.LocalDate;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.reader.JiraReader;
import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraQueryBuilder;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class EntryPointBackendTest {

  EntryPointBackend toTest;
  ObserverableValueImplementation<ExecutionException> occured;
  JiraReader reader;

  @Before
  public void before() {

    toTest = new EntryPointBackend(5);
    occured = new ObserverableValueImplementation<ExecutionException>();
    toTest.setExceptionHandler((observerable, oldV, newV) -> {
      occured.set(newV);
    });
    // ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 10 * 2, 30L, TimeUnit.SECONDS,
    // new LinkedBlockingQueue<Runnable>());
    // reader = new JiraReader("nwehrle", "#####", null, "jira.doubleslash.de", threadPool);
    reader = null;

  }

  @After
  public void shutdown() {
    toTest.end();
  }

  @Test
  public void testLoadAllSavedTasks() throws Exception {
    final boolean showFinished = false;
    final BlockingQueue<Task> q = reader.loadSequential(new JiraQueryBuilder(LocalDate.now(),
        LocalDate.now(), showFinished, null, null));

    class Consumer implements Runnable {

      Task closeQ;
      BlockingQueue<Task> bq;
      int name;

      public Consumer(final BlockingQueue<Task> bq, final int name) {
        this.bq = bq;
        this.closeQ = bq.peek();
        this.name = name;
      }

      @Override
      public void run() {
        boolean alive = true;
        while (alive) {
          try {
            final Task t = bq.take();
            if (t == closeQ) {
              alive = false;
              bq.put(closeQ);
              System.out.println("Consumer " + name + " ends");
              continue;
            }
            // toTest.saveTask(t);
            System.out.println("Consumer " + name + " read file: " + t.getKey() + " from "
                + t.getSource());
          } catch (final InterruptedException e) {
            alive = false;
            System.out.println("Consumer got interrupted");
          }
        }
      }
    }
    final Thread t1 = new Thread(new Consumer(q, 1));
    final Thread t2 = new Thread(new Consumer(q, 2));
    final Thread t3 = new Thread(new Consumer(q, 3));
    final Thread t4 = new Thread(new Consumer(q, 4));

    q.remove(); // remove PoisonPill

    t1.start();
    t2.start();
    t3.start();
    t4.start();
    System.out.println("all Threads Started");
    t1.join();
    t2.join();
    t3.join();
    t4.join();
    final ExecutionException ex = occured.getValue();
    if (ex != null) {
      System.err.println("Mesage: " + ex.getMessage() + "\n\n" + ExceptionUtils.getStackTrace(ex));
      Assert.fail();
    }

    System.out.println("All Threads ended");
  }
}
