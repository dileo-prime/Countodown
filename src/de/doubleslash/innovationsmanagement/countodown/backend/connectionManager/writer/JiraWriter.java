package de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.writer;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.NotImplementedException;

import de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.JiraConnector;
import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class JiraWriter extends JiraConnector implements Writer {

  public JiraWriter(final ObserverableValueImplementation<Throwable> latestException,
      final ThreadPoolExecutor threadPool) {
    super(latestException, threadPool);
  }

  @Override
  public void saveTask(final Task task) throws IOException {
    throw new NotImplementedException("This won't be implemented in this Prototype");
  }

}
