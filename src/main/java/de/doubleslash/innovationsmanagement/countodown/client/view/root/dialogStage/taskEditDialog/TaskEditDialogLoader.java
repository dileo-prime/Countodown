package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.taskEditDialog;

import java.util.ResourceBundle;
import java.util.function.Function;

import javafx.fxml.Initializable;
import javafx.stage.Stage;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.DialogStageLoader;
import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class TaskEditDialogLoader extends DialogStageLoader {

  private final static String Create_Task = "Create_Task";
  private final static String Show_Task = "Show_Task";
  private final static String Edit_Task = "Edit_Task";

  private final String userSource;
  private final Function<String, Boolean> keyUniquenessChecker;
  private final ObserverableValueImplementation<Task> taskWrapper;

  public TaskEditDialogLoader(final Task task, final String userSource, final Stage primaryStage,
      final Function<String, Boolean> keyUniquenessChecker) {
    super(primaryStage);
    this.userSource = userSource;
    this.keyUniquenessChecker = keyUniquenessChecker;
    this.taskWrapper = new ObserverableValueImplementation<Task>(task);
    setTitle(task);
  }

  @Override
  protected Initializable controllerInstance() {
    return new TaskEditDialogController(stage, taskWrapper, userSource, keyUniquenessChecker);
  }

  @Override
  public Task showDialogStage() {
    super.showDialogStage();
    return taskWrapper.getValue();
  }

  private void setTitle(final Task task) {
    final ResourceBundle bundle = getResources();
    if (task == null) {
      stage.setTitle(bundle.getString(Create_Task));
    } else {
      if (task == null || task.getSource().equals(userSource) && !task.isFinished()) {
        stage.setTitle(bundle.getString(Edit_Task));
      } else {
        stage.setTitle(bundle.getString(Show_Task));
      }
    }
  }
}