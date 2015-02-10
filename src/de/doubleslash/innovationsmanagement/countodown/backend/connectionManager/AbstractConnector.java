package de.doubleslash.innovationsmanagement.countodown.backend.connectionManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class AbstractConnector {

  private final static Logger logger = LoggerFactory.getLogger(AbstractConnector.class);

  protected final ThreadPoolExecutor threadPool;
  protected final ObserverableValueImplementation<Throwable> latestException;

  protected ThreadPoolExecutor getThreadPool() {
    return threadPool;
  }

  protected ObserverableValueImplementation<Throwable> getLatestException() {
    return latestException;
  }

  public AbstractConnector(final ThreadPoolExecutor threadPool,
      final ObserverableValueImplementation<Throwable> latestException) {
    this.threadPool = threadPool;
    this.latestException = latestException;

  }

  private final static String notEmpty = "notEmpty";

  private Task createEmptyTask() {
    return new Task(notEmpty, notEmpty, notEmpty);
  }

  /**
   * creates a blocking Queue which will be filled by parameter Consumer. The first Object in this
   * Queue is the Poison Pill, it will be inserted again at the end of the Queue to close it.
   *
   * @param consumer
   *          Fills the queue, the Parameter of this Consumer is the Queue
   * @return
   */
  protected BlockingQueue<Task> createBlockingQueueWithPoisonPillFilledByRunnable(
      final Consumer<BlockingQueue<Task>> consumer) {

    long startingTime;
    if (logger.isTraceEnabled()) {
      startingTime = System.currentTimeMillis();
    } else {
      startingTime = 0;
    }

    final Task endQ = createEmptyTask();
    final BlockingQueue<Task> queue = new LinkedBlockingQueue<>();
    // add 'Poison Pill' so Consumers learn to know it
    queue.add(endQ);
    threadPool.submit(() -> {
      try {
        consumer.accept(queue);
        queue.put(endQ);
      } catch (final InterruptedException e) {
        logger.warn("interrupted while filling queue");
        while (!queue.offer(endQ)) {
          queue.clear();
        }
      } catch (final Exception e) {
        logger.error(e.toString());
        latestException.set(e);
        // make shure endQ is in queue before this returns
        while (!queue.offer(endQ)) {
          queue.clear();
        }
        return;
      }
      if (logger.isTraceEnabled()) {
        final long finishTime = System.currentTimeMillis() - startingTime;
        logger.trace("Finished filling BlockingQ after: " + finishTime / 1000 + " seconds and "
            + finishTime % 1000 + " millis");
      }
    });
    return queue;
  }
}
