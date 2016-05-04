package de.doubleslash.innovationsmanagement.countodown.client.view.root.task.notepad;

import java.util.function.Consumer;

import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;
import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.util.DateToValue;

public class NotepadLoader extends MVCLoader {

  public final Task task;
  private final DateToValue<Color> colorPicker;
  private final Consumer<Task> showTaskDialog;

  public NotepadLoader(final Task task, final DateToValue<Color> currentDate,
      final Consumer<Task> showTaskDialog) {
    this.task = task;
    this.colorPicker = currentDate;
    this.showTaskDialog = showTaskDialog;
  }

  @Override
  protected Initializable controllerInstance() {
    return new NotepadController(task, colorPicker, showTaskDialog);
  }
}
