package de.doubleslash.innovationsmanagement.countodown;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.CountodownApplication;

public class SetupCountodown {
  private final static Logger logger = LoggerFactory.getLogger(SetupCountodown.class);
  private final static int THREADPOOL_SIZE = 5;

  public static void main(final String[] args) throws InterruptedException {
    final EntryPointBackend backend = new EntryPointBackend(THREADPOOL_SIZE);

    final Locale lang = backend.getCurrentLanguage();
    if (lang != null) {
      Locale.setDefault(lang);
    }

    final Thread applicationLauncherThread = new Thread(() -> {
      CountodownApplication.initialize(backend);
    });
    logger.info("Lauch CountodownApplication");

    applicationLauncherThread.start();
    applicationLauncherThread.join();

    backend.end();
    logger.info("Countodown ended");
    System.exit(0);

    // backend must be ended before endAllRunningThreads is called;
    endAllRunningThreads(); // not working
  }

  /**
   * Will end all Threads running at the startPoint of this Method. All Threads have a certain time
   * to end them self, after that time, the threads will be interrupted. If the threads won't
   * interrupt, they will be killed.
   */
  @SuppressWarnings("deprecation")
  private static void endAllRunningThreads() {
    final LinkedList<Thread> runningThreads = new LinkedList<>();
    for (final Thread t : Thread.getAllStackTraces().keySet()) {
      if (isSystemOrCurrentOrDeamonThread(t)) {
        if (logger.isInfoEnabled()) {
          logger.info("Not ending Thread " + t);
        }
        continue;
      }
      runningThreads.addFirst(t);
    }
    final SecondsToWaitForThreads wait5 = new SecondsToWaitForThreads(5);
    forAllThreadsAlive(runningThreads, wait5);

    forAllThreadsAlive(runningThreads, (thread) -> {
      /*
       * 'runningThreads' is a List of all Threads backwards, if a Thread is waiting for a
       * childThread, this might be enough to end the Thread without the need of an
       * InterruptedException
       */
      try {
        thread.join(10);
      } catch (final InterruptedException ignore) {
        logger.warn("interrupted");
      }
      if (!thread.isAlive()) {
        return;
      }
      logger.info(thread + " did not end and will be interrupted");
      thread.interrupt();
    });
    final SecondsToWaitForThreads lastChance = new SecondsToWaitForThreads(1);
    forAllThreadsAlive(runningThreads, lastChance);
    forAllThreadsAlive(
        runningThreads,
        (thread) -> {
          logger.error(thread
              + " is not ended properly and ignored interruption. Thread will be killed");
          thread.stop();
        });

    logger.info("Countodown CleanUp ended");
  }

  static class SecondsToWaitForThreads implements Consumer<Thread> {
    private final static int SECOND = 1000;
    final long maxWait;

    public SecondsToWaitForThreads(final int secondsToWaitFromNow) {
      this.maxWait = System.currentTimeMillis() + secondsToWaitFromNow * SECOND;
    }

    @Override
    public void accept(final Thread t) {
      long wait = maxWait - System.currentTimeMillis();
      if (wait <= 0) {
        wait = 1;
      }
      if (logger.isInfoEnabled()) {
        logger.info("Waiting " + wait + "millis for " + t + " to end");
      }
      try {
        t.join(wait);
      } catch (final InterruptedException ignore) {
        logger.warn("interrupted");
      }
    }
  }

  private static void forAllThreadsAlive(final List<Thread> threads, final Consumer<Thread> whatToDo) {
    final Iterator<Thread> it = threads.iterator();
    while (it.hasNext()) {
      final Thread t = it.next();
      if (!t.isAlive()) {
        it.remove();
        continue;
      }
      whatToDo.accept(t);
    }
  }

  private static boolean isSystemOrCurrentOrDeamonThread(final Thread t) {
    if (t == Thread.currentThread() || t.isDaemon()) {
      return true;
    }

    final ThreadGroup tg = t.getThreadGroup();
    if (tg == null) {
      return false;// most likely Dead, this is no concern of this method
    }
    if (tg.getName().equals("system")) {
      return true;
    }
    return false;
  }

}
