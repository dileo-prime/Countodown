package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.colorOptions;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.colorOptions.colorRule.ColorRuleController;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.colorOptions.colorRule.ColorRuleLoader;
import de.doubleslash.innovationsmanagement.countodown.data.filter.ColorDateOption;
import de.doubleslash.innovationsmanagement.countodown.util.DialogHelper;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class ColorOptionsController implements Initializable {

  private final static String HELP_TEXT = "Help_Text";

  private final EntryPointBackend backend;

  private final List<ColorRuleLoader> colorRules;

  public ColorOptionsController(final EntryPointBackend backend) {
    this.colorRules = new LinkedList<>();
    this.backend = backend;
  }

  @FXML
  ScrollPane rootPane;
  @FXML
  Button helpButton;

  @FXML
  VBox ruleConainer;

  private Tooltip helpText;
  private double helpTextWidth = -1;

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    this.helpText = new Tooltip(resources.getString(HELP_TEXT));
    initialize();
  }

  private void initialize() {
    rootPane.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
      handleKeyEvent(event);
    });
    final ObserverableValueImplementation<List<ColorDateOption>> colorDateOptions = backend
        .getColorDateOptions();
    if (colorDateOptions.getValue() == null) {
      handleNewColorRule();
      return;
    }
    for (final ColorDateOption opt : colorDateOptions.getValue()) {
      handleNewColorRule(opt);
    }
    sortColorRules();
    helpButton.setOnMouseExited((e) -> {
      helpText.hide();
    });

  }

  private void handleKeyEvent(final KeyEvent event) {
    if (event.isShortcutDown() && event.getCode().equals(KeyCode.S)) {
      handleSortAndSave();
    }
  }

  @FXML
  private void handleHelp(final ActionEvent ignore) {
    final Window window = rootPane.getScene().getWindow();
    final Bounds button = helpButton.getBoundsInParent();

    if (helpTextWidth < 0) {
      final Tooltip outerSize = new Tooltip("");
      helpTextWidth = 0; // we don't want to enter this if clause again;
      handleHelp(ignore);
      helpTextWidth = helpText.getWidth();
      outerSize.show(window);
      helpTextWidth -= outerSize.getWidth() / 2;
      outerSize.hide();

    }

    helpText.show(window, window.getX() + button.getMaxX() - helpTextWidth,
        window.getY() + button.getMaxY() + rootPane.getParent().getBoundsInParent().getMinY()
            + helpButton.getHeight());
  }

  @FXML
  private void handleNewColorRule() {
    handleNewColorRule(null);
  }

  private void handleNewColorRule(final ColorDateOption option) {
    final ColorRuleLoader loader = new ColorRuleLoader(option);
    ruleConainer.getChildren().add(loader.getView());
    colorRules.add(loader);
    final ColorRuleController controller = loader.getController();
    controller.addListener((ignore) -> {
      handleInvalid(loader);
    });
  }

  private void handleInvalid(final ColorRuleLoader loader) {
    colorRules.remove(loader);
    ruleConainer.getChildren().remove(loader.getView());
  }

  @FXML
  private void handleSortAndSave() {
    final Alert alert = DialogHelper.createInformation();
    alert.setTitle("Saving");
    alert.setHeaderText(null);
    alert.setContentText("Saving Color Options");
    alert.show();

    sortColorRules();
    saveColorRules();
  }

  private void saveColorRules() {
    final List<ColorDateOption> list = new LinkedList<>();
    for (final ColorRuleLoader loader : colorRules) {
      final ColorRuleController cont = loader.getController();
      final ColorDateOption colorDateOption = cont.getColorDateOption();
      if (colorDateOption != null) {
        list.add(colorDateOption);
      }
    }
    backend.getColorDateOptions().set(list);
  }

  private void sortColorRules() {
    Collections.sort(colorRules);
    ruleConainer.getChildren().clear();
    for (final ColorRuleLoader loader : colorRules) {
      ruleConainer.getChildren().add(loader.getView());
    }
  }
}
