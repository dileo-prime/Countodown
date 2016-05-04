package de.doubleslash.innovationsmanagement.countodown.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Pair;

public class DialogHelper {

  private static Image IMAGE = new Image(DialogHelper.class.getResource("login.png").toString());

  public static Alert createInformation() {
    return new Alert(AlertType.INFORMATION);
  }

  public static Alert createWarning() {
    return new Alert(AlertType.WARNING);
  }

  public static Alert createError() {
    return new Alert(AlertType.ERROR);
  }

  public static Alert createConfirmation() {
    return new Alert(AlertType.CONFIRMATION);
  }

  public static Alert createException(final Exception ex) {

    final Alert alert = createError();
    // Create expandable Exception.
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw);
    ex.printStackTrace(pw);
    final String exceptionText = sw.toString();

    final Label label = new Label("The exception stacktrace was:");

    final TextArea textArea = new TextArea(exceptionText);
    textArea.setEditable(false);
    textArea.setWrapText(true);

    textArea.setMaxWidth(Double.MAX_VALUE);
    textArea.setMaxHeight(Double.MAX_VALUE);
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    final GridPane expContent = new GridPane();
    expContent.setMaxWidth(Double.MAX_VALUE);
    expContent.add(label, 0, 0);
    expContent.add(textArea, 0, 1);

    // Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(expContent);

    return alert;
  }

  public static Dialog<Pair<String, String>> createLogInDialog() {
    final Dialog<Pair<String, String>> dialog = new Dialog<>();

    dialog.setTitle("Login");
    final ImageView image = new ImageView(IMAGE);
    image.setPreserveRatio(true);
    image.setFitHeight(80);
    dialog.setGraphic(image);

    // Set the button types.
    final ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

    // Create the username and password labels and fields.
    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    final TextField username = new TextField();
    username.setPromptText("Username");
    final PasswordField password = new PasswordField();
    password.setPromptText("Password");

    grid.add(new Label("Username:"), 0, 0);
    grid.add(username, 1, 0);
    grid.add(new Label("Password:"), 0, 1);
    grid.add(password, 1, 1);

    // Enable/Disable login button depending on whether a username was entered.
    final Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
    loginButton.setDisable(true);

    username.textProperty().addListener((observable, oldValue, newValue) -> {
      loginButton.setDisable(newValue.trim().isEmpty());
    });

    password.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
      if (event.isShortcutDown() && event.getCode().equals(KeyCode.BACK_SPACE)) {
        password.clear();
      }
    });
    username.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
      if (event.isShortcutDown() && event.getCode().equals(KeyCode.BACK_SPACE)) {
        password.clear();
      }
    });

    dialog.getDialogPane().setContent(grid);

    // Request focus on the username field by default.
    Platform.runLater(() -> username.requestFocus());

    // Convert the result to a username-password-pair when the login button is clicked.
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == loginButtonType) {
        return new Pair<>(username.getText(), password.getText());
      }
      return null;
    });
    return dialog;

  }
}
