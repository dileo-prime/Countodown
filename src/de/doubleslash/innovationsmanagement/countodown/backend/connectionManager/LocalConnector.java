package de.doubleslash.innovationsmanagement.countodown.backend.connectionManager;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class LocalConnector {
  private final static Logger logger = LoggerFactory.getLogger(LocalConnector.class);
  protected final ObserverableValueImplementation<Throwable> latestException;
  protected final static String COUNTODOWN_FOLDER = "CountodownData" + File.separatorChar;
  protected final static String COUNTODOWN_SAVE_FOLDER = COUNTODOWN_FOLDER + "save";
  protected final static String COUNTODOWN_OPTIONS_FOLDER = COUNTODOWN_FOLDER + "options";
  protected final static String COLOR_DATE_OPTIONS = "ColorDateOptions.opt";
  protected final static String JIRA_OPTIONS = "JiraOptions.opt";
  protected final static String LANG_OPTIONS = "LangOptions.opt";
  protected final static String JSON = ".json";
  protected final ObjectMapper mapper = JsonFactory.createUseAnnotations(true);

  protected final File home;
  protected final File save;
  protected final File options;
  protected final File optionsBackup;
  protected final String saveDirectory;

  public LocalConnector(final ObserverableValueImplementation<Throwable> latestException) {
    this.latestException = latestException;
    // final String userHome = System.getProperty("user.home") + File.separatorChar;
    final String userHome = ".." + File.separatorChar + ".." + File.separatorChar;
    home = new File(userHome + COUNTODOWN_FOLDER);
    save = new File(userHome + COUNTODOWN_SAVE_FOLDER);
    options = new File(userHome + COUNTODOWN_OPTIONS_FOLDER);
    this.optionsBackup = new File(COUNTODOWN_OPTIONS_FOLDER);
    if (logger.isDebugEnabled()) {
      logger.debug("SaveFolder for Optionsettings is: " + options);
      logger.debug("SaveFolder for Tasks is: " + save);
    }

    try {
      createDirectoryIfDoestExist(optionsBackup);
    } catch (final IOException ignore) {
    }
    if (!options.exists() || options.list().length == 0) {
      try {
        FileUtils.copyDirectory(optionsBackup, options);
      } catch (final IOException e) {
        logger.error("Couldn't create Options\n" + e);
      }
    }
    try {
      createDirectoryIfDoestExist(save);
    } catch (final IOException ignore) {
    }

    saveDirectory = save.getPath() + File.separatorChar;
  }

  private void createDirectoryIfDoestExist(final File newDirectory) throws IOException {
    if (!newDirectory.exists()) {
      if (newDirectory.mkdirs()) {
        logger.debug("Created directory: " + newDirectory.getName());
      } else {
        final String message = "directory could not be created: " + newDirectory.getCanonicalPath();
        logger.warn(message);
        throw new IOException(message);
      }
    }
  }

  protected File getFile(final Task task) throws IOException {
    final String saveDirectory = this.saveDirectory + task.getSource();
    createDirectoryIfDoestExist(new File(saveDirectory));
    final File location = new File(saveDirectory + File.separatorChar + task.getKey() + JSON);
    if (!location.exists() && !location.createNewFile()) {
      final String message = "Save File could not be created. " + location.getName();
      logger.error(message);
      throw new IOException(message);
    }
    return location;
  }

  protected File[] getAllSavedSources() {
    return save.listFiles((f) -> {
      return f.isDirectory();
    });
  }

  protected List<String> validateFileName(final File file, final Task task) {
    List<String> exceptionMessages = null;
    if (!file.getName().equals(task.getKey() + JSON)) {
      exceptionMessages = returnNewListIfNullOrParameter(exceptionMessages);
      exceptionMessages.add("File: " + file.getName() + " should be named: " + task.getKey());
    }
    if (!file.getParentFile().getName().equals(task.getSource())) {
      exceptionMessages = returnNewListIfNullOrParameter(exceptionMessages);
      exceptionMessages.add("File: " + file.getName() + " should be in directory "
          + task.getSource());
    }
    return exceptionMessages;
  }

  private <T> List<T> returnNewListIfNullOrParameter(final List<T> list) {
    if (list == null) {
      return new LinkedList<T>();
    }
    return list;
  }

  public String getUserName() {
    return System.getProperty("user.name");
  }

  protected IOException buildException(final List<String> exceptions) {

    if (exceptions == null) {
      return null;
    }
    final StringBuilder message = new StringBuilder("The following IOExceptions happened:");
    for (final String s : exceptions) {
      message.append('\n').append(s);
    }
    return new IOException(message.toString());
  }
}
