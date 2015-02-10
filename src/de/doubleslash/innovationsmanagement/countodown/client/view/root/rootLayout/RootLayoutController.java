package de.doubleslash.innovationsmanagement.countodown.client.view.root.rootLayout;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.OptionDialogLoader;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.taskEditDialog.TaskEditDialogLoader;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.FilterSideBarController;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.FilterSideBarLoader;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.jiraFilter.JiraFilterLoader;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.task.taskLayout.TaskLayoutController;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.task.taskLayout.TaskLayoutLoader;
import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraOption;
import de.doubleslash.innovationsmanagement.countodown.data.filter.TaskFilter;
import de.doubleslash.innovationsmanagement.countodown.util.DialogHelper;
import de.doubleslash.innovationsmanagement.countodown.util.Occurrence;

public class RootLayoutController implements Initializable, EventHandler<WindowEvent> {

   private final static Logger logger = LoggerFactory.getLogger(RootLayoutController.class);

   private final static String LOGIN = "LOGIN";
   private final static String LOGOUT = "LOGOUT";
   private final static String KEY_NOTHING_TO_LOG_IN = "Nothing_to_Log_in";
   private final static String EXIT = "Exit";
   private final static String EXIT_MESSAGE = "Exit_Message";
   private final static String KEY_NEW_TAKS = "New_Task";

   private MenuItem NOTHING_TO_LOG_IN;
   private ContextMenu newTaskMenu;

   private ResourceBundle resources;

   private final Stage primaryStage;

   @FXML
   BorderPane parent;

   @FXML
   ToggleButton filterButton;

   @FXML
   MenuBar filterButtonMenu;

   @FXML
   Pane centerPane;

   @FXML
   Menu connection;

   private final FilterSideBarLoader filterLoader;
   private final EntryPointBackend backend;
   private final TaskLayoutLoader taskLayout;

   private final String userName;

   private final ChangeListener<ExecutionException> backendErrorHandler;

   public RootLayoutController(final Stage stage, final EntryPointBackend backend) {

      this.primaryStage = stage;
      this.backend = backend;
      this.backendErrorHandler = new ExceptionWrapperListener();
      backend.setExceptionHandler(backendErrorHandler);

      this.taskLayout = new TaskLayoutLoader(backend, (task) -> {
         showTaskEditDialog(task);
      });
      this.filterLoader = new FilterSideBarLoader(backend, (querylist, viewDate) -> {
         return showTasks(querylist, viewDate);
      });
      this.userName = backend.getUserName();

   }

   @Override
   public void initialize(final URL location, final ResourceBundle resources) {
      this.resources = resources;

      MenuItem newTask;
      newTask = new MenuItem(resources.getString(KEY_NEW_TAKS));
      newTask.setOnAction((event) -> {
         handleNewTask();
      });
      newTaskMenu = new ContextMenu(newTask);
      NOTHING_TO_LOG_IN = new MenuItem(resources.getString(KEY_NOTHING_TO_LOG_IN));

      logger.debug("initalized Rootlayout");

      final Pane taskLayoutView = taskLayout.getView();
      taskLayoutView.prefHeightProperty().bind(centerPane.heightProperty());
      taskLayoutView.prefWidthProperty().bind(centerPane.widthProperty());
      centerPane.getChildren().add(taskLayoutView);
      // taskLayoutView.

      RemoveBackgroundFromFilterButton();

      filterLoader.getView(); // initializes filterLoader;
   }

   private Occurrence showTasks(final List<TaskFilter> queryList, final LocalDate currentDate) {
      final Occurrence finished = new Occurrence();
      final BlockingQueue<Task> bq = backend.loadAllTasks(queryList);

      final TaskLayoutController taskController = taskLayout.getController();
      taskController.setAllFromQueue(bq, currentDate, finished);

      return finished;
   }

   @FXML
   private void handleClose() {
      Event.fireEvent(primaryStage, new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
   }

   @Override
   public void handle(final WindowEvent event) {

      final Alert alert = DialogHelper.createConfirmation();
      alert.setTitle(resources.getString(EXIT));
      alert.setHeaderText(null);
      alert.setContentText(resources.getString(EXIT_MESSAGE));

      final Optional<ButtonType> result = alert.showAndWait();
      if (result.get() != ButtonType.OK) {
         event.consume();
      }
   }

   // key(Username, adress), value=loader
   private final Map<ImmutablePair<String, String>, JiraFilterLoader> allJiraConnections = new TreeMap<>();

   @FXML
   private void updateConnections() {
      connection.getItems().clear();

      final List<JiraOption> nameAdressList = backend.getAllJiraServer();
      for (final JiraOption nameAdress : nameAdressList) {
         final String login_str = String.format(resources.getString(LOGIN), nameAdress.getName());
         final MenuItem login = new MenuItem(login_str);
         login.setOnAction((event) -> {
            handleLogInJira(nameAdress);
         });
         connection.getItems().add(login);
      }
      for (final Entry<ImmutablePair<String, String>, JiraFilterLoader> entry : allJiraConnections.entrySet()) {
         final String logout_str = String.format(resources.getString(LOGOUT), entry.getKey().getKey(), entry.getValue()
               .getJiraName());
         final MenuItem logout = new MenuItem(logout_str);
         logout.setOnAction((event) -> {
            handleLogOutJira(entry.getKey(), entry.getValue());
         });
         connection.getItems().add(logout);
      }

      if (connection.getItems().isEmpty()) {
         connection.getItems().add(NOTHING_TO_LOG_IN);
      }

   }

   private void handleLogInJira(final JiraOption nameAdress) {

      final Dialog<Pair<String, String>> login = DialogHelper.createLogInDialog();
      final Optional<Pair<String, String>> userPass = login.showAndWait();
      if (!userPass.isPresent()) {
         return;
      }
      final ImmutablePair<String, String> keyPair = new ImmutablePair<>(userPass.get().getKey(), nameAdress.getAdress());
      if (allJiraConnections.containsKey(keyPair)) {
         final Alert invalid = DialogHelper.createWarning();
         invalid.setContentText("User: " + keyPair.getKey() + " is already logged in to " + nameAdress.getName() + ".");
         invalid.setHeaderText("LogIn failed!");
         invalid.showAndWait();
         return;
      }
      try {
         backend.logInJira(userPass.get().getKey(), userPass.get().getValue(), nameAdress.getAdress());
      } catch (final IllegalArgumentException e) {
         final Alert invalid = DialogHelper.createWarning();
         invalid.setContentText(e.getMessage());
         invalid.setHeaderText("LogIn failed!");
         invalid.showAndWait();
         return;
      }
      logger.info("LogIn Jira, user: " + userPass.get().getKey());

      final JiraFilterLoader loader = new JiraFilterLoader(userPass.get().getKey(), nameAdress);
      allJiraConnections.put(keyPair, loader);
      final FilterSideBarController filterSideBarController = filterLoader.getController();
      filterSideBarController.setAndInitializeFilter(loader);

   }

   private void handleLogOutJira(final ImmutablePair<String, String> keyPair, final JiraFilterLoader loader) {

      allJiraConnections.remove(keyPair);

      final FilterSideBarController filterController = filterLoader.getController();
      filterController.removeFilter(loader);

      backend.logOutJira(keyPair.getKey(), keyPair.getValue());
   }

   /*
    * will be lazy initialised so i need to do the logic in listeners
    */
   private void RemoveBackgroundFromFilterButton() {

      final ObservableList<Node> filterButtonList = filterButtonMenu.getChildrenUnmodifiable();
      final ListChangeListener<Node> listenFilter = new ListChangeListener<Node>() {

         ObservableList<Node> hboxList;

         @Override
         public void onChanged(final javafx.collections.ListChangeListener.Change<? extends Node> c) {
            filterButtonList.removeListener(this);
            hboxList = ((HBox) filterButtonList.get(0)).getChildren();
            hboxList.addListener(listenHBox);

         }

         final ListChangeListener<Node> listenHBox = new ListChangeListener<Node>() {

            @Override
            public void onChanged(final javafx.collections.ListChangeListener.Change<? extends Node> c) {

               hboxList.removeListener(listenHBox);

               final MenuButton wrappingButton = (MenuButton) hboxList.get(0);
               wrappingButton.setBackground(null);
            }
         };
      };
      filterButtonList.addListener(listenFilter);

   }

   @FXML
   private void handleFilter() {
      if (filterButton.isSelected()) {
         filterButton.setUnderline(true);
         parent.setRight(filterLoader.getView());
      } else {
         filterButton.setUnderline(false);
         parent.setRight(null);
      }
   }

   @FXML
   private void handleNewTask() {
      showTaskEditDialog(null);
   }

   @FXML
   private void handleShowOptions() {
      new OptionDialogLoader(primaryStage, backend).showDialogStage();
   }

   private void showTaskEditDialog(final Task task) {

      final TaskEditDialogLoader dialogLoader = new TaskEditDialogLoader(task, userName, primaryStage, (s) -> {
         return !backend.checkIfKeyExists(s);
      });
      final Task changed = dialogLoader.showDialogStage();
      if (changed != null) {
         Occurrence finishedSaving;
         if (changed.getSource().equals(userName)) {
            finishedSaving = backend.saveTask(changed);
         } else {
            finishedSaving = new Occurrence();
            finishedSaving.signal();
         }

         final FilterSideBarController controller = filterLoader.getController();
         new Thread(() -> {
            try {
               finishedSaving.await();
            } catch (final InterruptedException e) {
               logger.warn("interrupted");
            }
            controller.reload();
         }).start();
      }
   }

   @FXML
   private void showContextMenu(final ContextMenuEvent e) {
      newTaskMenu.show(primaryStage, e.getScreenX(), e.getScreenY());
   }

   class ExceptionWrapperListener implements ChangeListener<ExecutionException> {
      final Lock conditionLock = new ReentrantLock();
      final Condition exceptionShown = conditionLock.newCondition();

      @Override
      public void changed(final ObservableValue<? extends ExecutionException> observable,
            final ExecutionException oldValue, final ExecutionException newValue) {
         conditionLock.lock();
         Platform.runLater(() -> {
            final Alert error = DialogHelper.createError();
            Throwable cause = newValue.getCause();
            String additionalMessage;
            if (cause == null) {
               cause = newValue;
               additionalMessage = "";
            } else {
               additionalMessage = cause.getClass().getSimpleName();
            }
            error.setTitle(cause.getClass().getName());
            String headerText = "Exception in Backend";
            if (!additionalMessage.isEmpty()) {
               headerText += '\n' + additionalMessage;
            }
            final String message = cause.getMessage();
            if (message != null) {
               final TextArea textArea = new TextArea(message);
               textArea.setEditable(false);
               textArea.setWrapText(true);
               final Pane pane = new Pane(textArea);
               error.getDialogPane().setContent(pane);
            }

            error.setHeaderText(headerText);
            conditionLock.lock();
            error.showAndWait();
            exceptionShown.signal();
            conditionLock.unlock();
         });
         try {
            exceptionShown.await();
         } catch (final InterruptedException e) {
            logger.warn("interrupted");
         }
         conditionLock.unlock();
      }
   }
}
