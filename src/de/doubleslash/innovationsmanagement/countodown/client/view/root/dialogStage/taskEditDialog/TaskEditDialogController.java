package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.taskEditDialog;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import de.doubleslash.innovationsmanagement.countodown.data.Person;
import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.util.DialogHelper;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class TaskEditDialogController implements Initializable {

  // private final static Logger logger = LoggerFactory.getLogger(TaskEditDialogController.class);

  private final static String INVALID_INPUT = "Invalid_Input";
  private final static String CLOSE_MESSAGE = "Close_Message";
  private final static String CLOSE_TASK = "Close_Task";

  private final ObserverableValueImplementation<Task> taskWrapper;
  private Task task;

  private volatile boolean edited = false;

  private final Stage dialogStage;

  private final String userSource;
  private final boolean newTask;
  private final boolean editable;
  private final Function<String, Boolean> keyUniquenessChecker;

  public TaskEditDialogController(final Stage dialogStage,
      final ObserverableValueImplementation<Task> taskWrapper, final String userSource,
      final Function<String, Boolean> keyUniquenessChecker) {
    this.dialogStage = dialogStage;
    this.userSource = userSource;
    this.taskWrapper = taskWrapper;
    this.task = taskWrapper.getValue();
    this.keyUniquenessChecker = keyUniquenessChecker;
    taskWrapper.set(null);

    if (task == null || task.getSource().equals(userSource) && !task.isFinished()) {
      this.editable = true;
    } else {
      this.editable = false;
    }
    if (task == null) {
      this.newTask = true;
    } else {
      this.newTask = false;
    }
  }

  @FXML
  private TextField title;
  @FXML
  private TextArea summary;
  @FXML
  private TextField assigneeFirstName;
  @FXML
  private TextField assigneeLastName;
  @FXML
  private TextField workToDoDays;
  @FXML
  private TextField workToDoHours;
  @FXML
  private TextField workToDoMinutes;
  @FXML
  private DatePicker dueDatePicker;
  @FXML
  private TextArea description;

  @FXML
  private Button editButton;
  @FXML
  private Button finishButton;

  private ResourceBundle resources;

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    this.resources = resources;
    initialize();
  }

  private void initialize() {
    if (!newTask) {
      refreshFromTask();
    } else {
      initializeNewTask();
    }
    ajustEditable();
    dueDatePicker.addEventFilter(MouseEvent.MOUSE_RELEASED, (e) -> {
      if (!editable && e.getTarget() instanceof StackPane) {
        e.consume();
      }
    });
    addChangeListenerToAllFields();
  }

  private void initializeNewTask() {
    dueDatePicker.setValue(LocalDate.now());
    assigneeLastName.setText(userSource);
  }

  private void addChangeListenerToAllFields() {
    final InvalidationListener listener = new InvalidationListener() {

      @Override
      public void invalidated(final Observable observable) {
        edited = true;

      }
    };
    title.textProperty().addListener(listener);
    summary.textProperty().addListener(listener);
    assigneeFirstName.textProperty().addListener(listener);
    assigneeLastName.textProperty().addListener(listener);
    workToDoDays.textProperty().addListener(listener);
    workToDoHours.textProperty().addListener(listener);
    workToDoMinutes.textProperty().addListener(listener);
    description.textProperty().addListener(listener);

    dueDatePicker.valueProperty().addListener((obs, oldV, newV) -> {
      if (!newV.equals(oldV)) {
        listener.invalidated(obs);
      }
    });
  }

  private void ajustEditable() {
    if (newTask) {
      title.setEditable(true);
    }

    if (editable) {
      summary.setEditable(true);
      assigneeFirstName.setEditable(true);
      assigneeLastName.setEditable(true);
      workToDoDays.setEditable(true);
      workToDoHours.setEditable(true);
      workToDoMinutes.setEditable(true);
      dueDatePicker.setEditable(true);
      description.setEditable(true);
      finishButton.setVisible(true);
    } else {
      final String adress = task.getOnlineSource();
      if (adress != null) {
        editButton.setVisible(true);
      }
    }
  }

  private void refreshFromTask() {
    title.setText(task.getTitle());
    summary.setText(task.getSummary());
    if (task.getAssignee() != null) {
      assigneeFirstName.setText(task.getAssignee().getFirstName());
      assigneeLastName.setText(task.getAssignee().getLastName());
    }
    final long mins = task.getWorkToDo();
    final long hours = mins / Task.MINUTES_PER_HOUR;
    final long days = hours / Task.HOURS_PER_DAY;
    workToDoMinutes.setText(Long.toString(mins % Task.MINUTES_PER_HOUR));
    workToDoHours.setText(Long.toString(hours % Task.HOURS_PER_DAY));
    workToDoDays.setText(Long.toString(days));
    dueDatePicker.setValue(task.getDueDate());
    description.setText(task.getDescription());
  }

  private boolean checkIfValid() {
    final String wrongInput = getWrongInputMessage();
    if (wrongInput.isEmpty()) {
      return true;
    } else {
      final Alert alert = DialogHelper.createWarning();
      final String invalid = resources.getString(INVALID_INPUT);
      alert.setHeaderText(invalid);
      alert.setTitle(invalid);
      alert.setContentText(wrongInput);
      alert.showAndWait();
      return false;
    }
  }

  private String getWrongInputMessage() {
    String out = "";
    final char seperator = '\n';

    if (newTask && !keyUniquenessChecker.apply(title.getText())) {
      out += "Title must be unique" + seperator;
    }

    if (title.getText().isEmpty()) {
      out += "Title must contain text" + seperator;
    }
    if (summary.getText().isEmpty()) {
      out += "Summary must contain text" + seperator;
    }
    final String firstName = assigneeFirstName.getText();
    final String lastName = assigneeLastName.getText();
    if ((firstName == null || firstName.isEmpty()) && (lastName == null || lastName.isEmpty())) {
      out += "Assignee must have a Name" + seperator;
    }
    if (workToDoInMins() == 0) {
      out += "Work to do must be longer then 0 minutes" + seperator;
    }
    if (dueDatePicker.getValue() == null) {
      out += "There must be a DueDate" + seperator;
    }
    return out;
  }

  private long workToDoInMins() {
    long out = 0L;
    out += parseLong(workToDoDays.getText());
    out *= Task.HOURS_PER_DAY;
    out += parseLong(workToDoHours.getText());
    out *= Task.MINUTES_PER_HOUR;
    out += parseLong(workToDoMinutes.getText());
    return out;
  }

  private long parseLong(final String s) {
    if (s == null || s.isEmpty()) {
      return 0L;
    }
    return Long.parseLong(s);
  }

  @FXML
  private void checkIfNumKey(final KeyEvent e) {
    if (!e.getCharacter().matches("[0-9]")) {
      e.consume();
    }
  }

  private void saveTask() {
    if (newTask) {
      task = new Task(userSource, title.getText(), summary.getText());
    } else {
      task.setSummary(summary.getText());
    }
    String firstName = assigneeFirstName.getText();
    if (firstName == null) {
      firstName = "";
    }
    String lastName = assigneeLastName.getText();
    if (lastName == null) {
      lastName = "";
    }
    Person assignee = task.getAssignee();
    if (assignee == null) {
      assignee = new Person(firstName, lastName);
      task.setAssignee(assignee);
    } else {
      assignee.setFirstName(firstName);
      assignee.setLastName(lastName);
    }
    task.setDueDate(dueDatePicker.getValue());
    task.setWorkToDo(workToDoInMins());
    task.setDescription(description.getText());
    taskWrapper.set(task);
  }

  @FXML
  private void handleOK() {
    if (!edited) {
      dialogStage.close();
      return;
    }
    if (editable) {
      if (checkIfValid()) {
        saveTask();
      } else {
        return;
      }
    } else {
      taskWrapper.set(task);
    }

    dialogStage.close();
  }

  @FXML
  private void handleShortcuts(final KeyEvent event) {
    if (event.isShortcutDown() && event.getCode().equals(KeyCode.S)) {
      handleOK();
    }
  }

  @FXML
  private void handleFinished() {
    final Alert conf = DialogHelper.createConfirmation();
    conf.setTitle(resources.getString(CLOSE_TASK));
    conf.setHeaderText(null);
    conf.setContentText(String.format(resources.getString(CLOSE_MESSAGE), task.getTitle()));

    final Optional<ButtonType> ret = conf.showAndWait();

    if (!ret.isPresent() || !ret.get().equals(ButtonType.OK)) {
      return;
    }

    if (checkIfValid()) {
      edited = true;
      task.setFinished();
      handleOK();
    }
  }

  @FXML
  private void handleCancel() {
    dialogStage.close();
  }

  @FXML
  private void handleEdit() {
    edited = true;
    final Application a = new Application() {
      @Override
      public void start(final Stage primaryStage) throws Exception {}
    };
    a.getHostServices().showDocument(task.getOnlineSource());
  }
}
