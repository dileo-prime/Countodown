package de.doubleslash.innovationsmanagement.countodown.backend.connectionManager.writer;

import java.io.IOException;

import de.doubleslash.innovationsmanagement.countodown.data.Task;

public interface Writer {

  public void saveTask(Task task) throws IOException;

}
