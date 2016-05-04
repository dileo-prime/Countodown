package de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.reader;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javafx.scene.paint.Color;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.LocalConnector;
import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.data.filter.ColorDateOption;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraOption;
import de.doubleslash.innovationsmanagement.countodown.data.filter.LocalFilter;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;
import de.doubleslash.innovationsmanagement.countodown.util.RelativeDate;
import de.doubleslash.innovationsmanagement.countodown.util.SynchronizedIterator;

public class LocalReader extends LocalConnector implements Reader {

   private final static Logger logger = LoggerFactory.getLogger(LocalReader.class);

   final ThreadPoolExecutor threadPool;

   public LocalReader(final ThreadPoolExecutor threadPool,
         final ObserverableValueImplementation<Throwable> latestException) {
      super(latestException);
      this.threadPool = threadPool;
   }

   public List<String> getAllSavedTaskKeys() {
      final File[] sources = getAllSavedSources();
      final List<String> out = new ArrayList<>(sources.length);
      for (final File directory : sources) {
         final File[] jsonFiles = directory.listFiles((dir, name) -> {
            return name.endsWith(JSON);
         });
         for (final File f : jsonFiles) {
            out.add(f.getName().replace(JSON, ""));
         }
      }
      return out;
   }

   public void loadAllSavedTasksIntoQ(final BlockingQueue<Task> queue, final LocalFilter filter) {
      final File[] sources = getAllSavedSources();
      final LinkedList<Future<Void>> threadResults = new LinkedList<>();

      for (final File f : sources) {
         threadResults.add(threadPool.submit(() -> {
            try {
               loadJsonTaksAndPutIntoQ(f, queue, filter);
            } catch (final InterruptedException e) {
               logger.warn("interrupted");
            } catch (final ExecutionException e) {
               logger.error("Error: " + e);
               latestException.set(e.getCause());
            }
            return null;
         }));
      }

      for (final Future<Void> t : threadResults) {
         try {
            t.get(); // join
         } catch (final InterruptedException ignore) {
            logger.warn("interrupted");
            break;
         } catch (final ExecutionException e) {
            latestException.set(e.getCause());
            break;
         }
      }
      // build Exception
   }

   private void loadJsonTaksAndPutIntoQ(final File directory, final BlockingQueue<Task> queue, final LocalFilter filter)
         throws InterruptedException, ExecutionException {
      final File[] jsonFiles = directory.listFiles((dir, name) -> {
         return name.endsWith(JSON);
      });
      final List<File> jsonFileList = Arrays.asList(jsonFiles);
      final SynchronizedIterator<File> iter = new SynchronizedIterator<>(jsonFileList.iterator());
      final LinkedList<Future<Void>> threadCount = new LinkedList<>();
      for (int i = 0; i < threadPool.getCorePoolSize(); i++) {
         threadCount.add(threadPool.submit(() -> {
            LoadJsonIntoQueueUntilIterEnds(iter, queue, filter);
            return null;
         }));
      }

      for (final Future<Void> f : threadCount) {
         f.get(); // join
      }
   }

   private final void LoadJsonIntoQueueUntilIterEnds(final SynchronizedIterator<File> iter,
         final BlockingQueue<Task> queue, final LocalFilter filter) {
      File f;
      while (true) {
         f = iter.nextOrNull();
         if (f == null) {
            break;
         }

         byte[] json;
         try {
            json = FileUtils.readFileToByteArray(f);
         } catch (final IOException ie) {
            latestException.set(ie);
            continue;
         }
         Task t = mapper.readValue(json, Task.class);
         t = (Task) t.getEqual();
         final List<String> invalid = validateFileName(f, t);
         if (invalid != null) {
            final IOException e = buildException(invalid);
            if (logger.isInfoEnabled()) {
               logger.info(e.toString());
            }
            latestException.set(e);
            continue;
         }

         if (!isFilterConform(t, filter)) {
            if (logger.isTraceEnabled()) {
               logger.trace("Task: " + t.getKey() + " is out of Filterrange");
            }
            continue;
         }

         try {
            queue.put(t);
         } catch (final InterruptedException e) {
            logger.warn("interrupted");
            break;
         }

      }
   }

   private class colorDateList {
      List<colorDateforJson> list;
   }

   private class colorDateforJson {
      private String color;
      private RelativeDate date;
      private String name;
   }

   public List<ColorDateOption> readColorDateOptions() {
      final File colorDateOptions = new File(options.getPath() + File.separator + COLOR_DATE_OPTIONS);
      if (!colorDateOptions.exists() || !colorDateOptions.isFile()) {
         if (logger.isDebugEnabled()) {
            logger.debug("No OptionsFile to read: " + colorDateOptions);
         }
         return null;
      }
      final List<ColorDateOption> ret = new LinkedList<>();
      List<colorDateforJson> list;
      try {
         byte[] json;
         json = FileUtils.readFileToByteArray(colorDateOptions);
         list = mapper.readValue(json, colorDateList.class).list;
      } catch (final IOException e) {
         latestException.set(e);
         logger.error(e.toString());
         return ret;
      }
      for (final colorDateforJson json : list) {
         ret.add(new ColorDateOption(Color.web(json.color), json.date, json.name));
      }
      return ret;
   }

   private boolean isFilterConform(final Task t, final LocalFilter filter) {
      if (t.isFinished() && !filter.showFinished()) {
         return false;
      }
      final LocalDate due = t.getDueDate();
      final LocalDate dueFrom = filter.getDueDateFrom().minus(1, ChronoUnit.DAYS);
      final LocalDate dueTo = filter.getDueDateTo().plus(1, ChronoUnit.DAYS);
      return (due.isAfter(dueFrom) && due.isBefore(dueTo));
   }

   private class jiraOptionsList {
      List<JiraOption> list;
   }

   public List<JiraOption> loadAllJiraOptions() {
      final File jiraOptions = new File(options.getPath() + File.separator + JIRA_OPTIONS);
      if (!jiraOptions.exists() || !jiraOptions.isFile()) {
         if (logger.isDebugEnabled()) {
            logger.debug("No OptionsFile to read: " + jiraOptions);
         }
         return null;
      }
      List<JiraOption> list;
      try {
         byte[] json;
         json = FileUtils.readFileToByteArray(jiraOptions);
         list = mapper.readValue(json, jiraOptionsList.class).list;
      } catch (final IOException e) {
         latestException.set(e);
         logger.error(e.toString());
         return new LinkedList<>();
      }

      return list;
   }

   private class LangOptions {
      String lang;
   }

   public Locale loadCurrentLocale() {
      final File langOptions = new File(options.getPath() + File.separator + LANG_OPTIONS);
      if (!langOptions.exists() || !langOptions.isFile()) {
         if (logger.isDebugEnabled()) {
            logger.debug("No OptionsFile to read: " + langOptions);
         }
         return null;
      }
      Locale current = null;
      byte[] json;
      try {
         json = FileUtils.readFileToByteArray(langOptions);
         final String language = mapper.readValue(json, LangOptions.class).lang;
         if (language == null || language.isEmpty()) {
            return null;
         }
         current = new Locale(language);
      } catch (final IOException e) {
         latestException.set(e);
         logger.error(e.toString());
      }

      return current;
   }
}
