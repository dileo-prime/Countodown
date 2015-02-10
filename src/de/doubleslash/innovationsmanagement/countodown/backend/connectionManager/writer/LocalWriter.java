package de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.writer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.LocalConnector;
import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.data.filter.ColorDateOption;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraOption;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class LocalWriter extends LocalConnector implements Writer {
   private final static Logger logger = LoggerFactory.getLogger(LocalWriter.class);

   public LocalWriter(final ObserverableValueImplementation<Throwable> latestException) {
      super(latestException);
   }

   public void writeTaskToFile(final Task task, final File toSave) {
      task.doReadLocked((t) -> {
         writeJsonToFile(toSave, t);
      });
   }

   private void writeJsonToFile(final File toSave, final Object obj) {
      final byte[] json = mapper.writeValueAsBytes(obj);
      try {
         FileUtils.writeByteArrayToFile(toSave, json);
      } catch (final IOException e) {
         latestException.set(e);
      }
   }

   @Override
   public void saveTask(final Task task) throws IOException {
      final File f = getFile(task);
      writeTaskToFile(task, f);
   }

   class ColorDateListContainer {
      List<ColorDateOption> list;

      public ColorDateListContainer(final List<ColorDateOption> list) {
         this.list = list;
      }
   }

   public void saveColorDateOptions(final List<ColorDateOption> value) {
      final File colorDateOptions = new File(options.getPath() + File.separator + COLOR_DATE_OPTIONS);
      try {
         colorDateOptions.createNewFile();
      } catch (final IOException e) {
         final String message = "Options File could not be created. " + colorDateOptions.getName();
         logger.error(message);
         latestException.set(e);
         return;
      }
      writeJsonToFile(colorDateOptions, new ColorDateListContainer(value));
   }

   class JiraListContainer {
      List<JiraOption> list;

      public JiraListContainer(final List<JiraOption> jiraList) {
         this.list = jiraList;
      }
   }

   public void saveJiraOptions(final List<JiraOption> jiraList) {
      final File jiraOptions = new File(options.getPath() + File.separator + JIRA_OPTIONS);
      try {
         jiraOptions.createNewFile();
      } catch (final IOException e) {
         final String message = "Options File could not be created. " + jiraOptions.getName();
         logger.error(message);
         latestException.set(e);
         return;
      }
      writeJsonToFile(jiraOptions, new JiraListContainer(jiraList));
   }

   class LangOptions {
      final Locale lang;

      public LangOptions(final Locale lang) {
         this.lang = lang;
      }
   }

   public void saveCurrentLocale(final Locale current) {
      final File langOptions = new File(options.getPath() + File.separator + LANG_OPTIONS);
      try {
         langOptions.createNewFile();
      } catch (final IOException e) {
         final String message = "Options File could not be created. " + langOptions.getName();
         logger.error(message);
         latestException.set(e);
         return;
      }
      writeJsonToFile(langOptions, new LangOptions(current));
   }

}
