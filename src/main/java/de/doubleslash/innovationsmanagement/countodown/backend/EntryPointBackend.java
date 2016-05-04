package de.doubleslash.innovationsmanagement.countodown.backend;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.beans.value.ChangeListener;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.AbstractConnector;
import de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.reader.JiraReader;
import de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.reader.LocalReader;
import de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.writer.LocalWriter;
import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.data.filter.ColorDateOption;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraOption;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraQueryBuilder;
import de.doubleslash.innovationsmanagement.countodown.data.filter.LocalFilter;
import de.doubleslash.innovationsmanagement.countodown.data.filter.TaskFilter;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;
import de.doubleslash.innovationsmanagement.countodown.util.Occurrence;

public class EntryPointBackend extends AbstractConnector {

   private final static Logger logger = LoggerFactory.getLogger(EntryPointBackend.class);

   private final ThreadPoolExecutor threadPool;
   private final ObserverableValueImplementation<ExecutionException> latestException;
   private final ObserverableValueImplementation<Throwable> lastestThrowable;
   private final LocalWriter localWriter;
   private final LocalReader localReader;
   private final JiraReader jiraReader;

   private final ObserverableValueImplementation<List<ColorDateOption>> observarableColorDate;
   private final ObserverableValueImplementation<Locale> observarableLang;
   private final ObserverableValueImplementation<List<JiraOption>> observerableJiraServer;

   public EntryPointBackend(final int sizeThreadPool) {
      super(getNewThreadPoolExecutor(sizeThreadPool), getNewObserverableValueImplementation());
      this.threadPool = getThreadPool();
      this.lastestThrowable = getLatestException();
      this.latestException = getNewObserverableValueImplementation();
      lastestThrowable.addListener((obs, oldE, newE) -> {
         this.latestException.set(new ExecutionException(newE));
      });
      this.localWriter = new LocalWriter(lastestThrowable);
      this.localReader = new LocalReader(threadPool, lastestThrowable);
      this.jiraReader = new JiraReader(lastestThrowable, threadPool);
      ;
      latestException.addListener((obs, oldE, newE) -> {
         logger.error(ExceptionUtils.getStackTrace(newE));
      });

      this.observarableColorDate = new ObserverableValueImplementation<>();
      this.observarableColorDate.set(localReader.readColorDateOptions());
      this.observarableColorDate.addListener((obs) -> {
         localWriter.saveColorDateOptions(this.observarableColorDate.getValue());
      });

      this.observerableJiraServer = new ObserverableValueImplementation<>();
      this.observerableJiraServer.set(localReader.loadAllJiraOptions());
      this.observerableJiraServer.addListener((obs) -> {
         localWriter.saveJiraOptions(this.observerableJiraServer.getValue());
      });

      this.observarableLang = new ObserverableValueImplementation<>();
      this.observarableLang.set(localReader.loadCurrentLocale());
      this.observarableLang.addListener((obs) -> {
         localWriter.saveCurrentLocale(this.observarableLang.getValue());
      });
   }

   private static ThreadPoolExecutor getNewThreadPoolExecutor(final int sizeThreadPool) {
      return new ThreadPoolExecutor(sizeThreadPool, sizeThreadPool, 30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

   }

   private static <T> ObserverableValueImplementation<T> getNewObserverableValueImplementation() {
      return new ObserverableValueImplementation<T>();
   }

   /**
    * Shutdown Threadpool, may leave running threads if this threads ignore InterruptedExceptions
    */
   public void end() {
      jiraReader.LogOutAll();
      threadPool.shutdown();
      try {
         if (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
            logger.warn("Timeout in Shutdown, force shutdown now");
            threadPool.shutdownNow();

         }
      } catch (final InterruptedException e) {
         logger.error("ThreadPool Termination is interrupted: " + e);
      }
      logger.info("Backend ended");
   }

   public boolean checkIfKeyExists(final String key) {
      return localReader.getAllSavedTaskKeys().contains(key);
   }

   public Occurrence saveTask(final Task task) {

      if (logger.isInfoEnabled()) {
         logger.info("Start Saving");
      }
      final Occurrence finished = new Occurrence();

      threadPool.execute(() -> {
         try {
            localWriter.saveTask(task);
            if (logger.isInfoEnabled()) {
               logger.info("Saved Task: \n" + task);
            }
         } catch (final IOException e) {
            final String message = "Error in SaveTask ";
            logger.error(message + e);
            latestException.set(new ExecutionException(message, e));
         }

         if (logger.isInfoEnabled()) {
            logger.info("End Saving");
         }
         finished.signal();

      });
      return finished;
   }

   public void setExceptionHandler(final ChangeListener<? super ExecutionException> listener) {
      latestException.addListener(listener);
   }

   public BlockingQueue<Task> loadAllTasks(final List<TaskFilter> queryList) {
      if (logger.isInfoEnabled()) {
         logger.info("Start Loading");
      }
      return createBlockingQueueWithPoisonPillFilledByRunnable((queue) -> {
         loadAllTasksIntoQ(queryList, queue);
         if (logger.isInfoEnabled()) {
            logger.info("End Loading");
         }
      });

   }

   private void loadAllTasksIntoQ(final List<TaskFilter> queryList, final BlockingQueue<Task> queue) {
      if (queryList == null) {
         return;
      }

      for (final TaskFilter filter : queryList) {
         if (filter instanceof JiraQueryBuilder) {
            jiraReader.getSequentialLoader((JiraQueryBuilder) filter).accept(queue);
         } else if (filter instanceof LocalFilter) {
            localReader.loadAllSavedTasksIntoQ(queue, (LocalFilter) filter);

         }
      }

   }

   public boolean checkIfValidJira(final String jiraAdress) {
      return jiraReader.isServerAdressValid(jiraAdress);
   }

   public void logInJira(final String username, final String password, final String jiraAdress)
         throws IllegalArgumentException {
      jiraReader.LogInJira(username, password, jiraAdress);
   }

   public void logOutJira(final String username, final String jiraAdress) {
      jiraReader.LogOutJira(username, jiraAdress);
   }

   public String getUserName() {
      return localReader.getUserName();
   }

   public ObserverableValueImplementation<List<ColorDateOption>> getColorDateOptions() {
      return observarableColorDate;
   }

   /**
    * @return Name, Adress
    */
   public List<JiraOption> getAllJiraServer() {
      final List<JiraOption> list = observerableJiraServer.getValue();
      if (list == null) {
         return new LinkedList<JiraOption>();
      }
      return new LinkedList<>(list);// This is cloning the list
   }

   public void saveAllJiraServer(final List<JiraOption> serverList) {
      observerableJiraServer.set(serverList);
   }

   public Locale getCurrentLanguage() {
      return observarableLang.getValue();
   }

   public void saveCurrentLanguage(final Locale current) {
      observarableLang.set(current);
   }

}
