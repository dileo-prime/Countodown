package de.doubleslash.innovationsmanagement.countodown.client.view.root.task.taskLayout;

import java.util.function.Consumer;

import javafx.fxml.Initializable;
import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;
import de.doubleslash.innovationsmanagement.countodown.data.Task;

public class TaskLayoutLoader extends MVCLoader {

  Consumer<Task> showTaskDialog;
  EntryPointBackend backend;

  public TaskLayoutLoader(final EntryPointBackend backend, final Consumer<Task> showTaskDialog) {
    this.showTaskDialog = showTaskDialog;
    this.backend = backend;

  }

  @Override
  protected Initializable controllerInstance() {
    return new TaskLayoutController(showTaskDialog, backend);
  }
}
