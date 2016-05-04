package de.doubleslash.innovationsmanagement.countodown.client.view.root.task.notepad.label;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class LabelController implements Initializable {

  private final SimpleStringProperty titleProperty;
  private final SimpleStringProperty summaryProperty;
  private final SimpleStringProperty dueDateProperty;
  private final ObserverableValueImplementation<Color> textColorProperty;

  @FXML
  Label title;
  @FXML
  Label summary;

  @FXML
  Label dueDate;

  @FXML
  Label dueDateLabel;

  public LabelController(final SimpleStringProperty titleProperty,
      final SimpleStringProperty summaryProperty, final SimpleStringProperty dueDateProperty,
      final ObserverableValueImplementation<Color> textColorProperty) {

    this.titleProperty = titleProperty;
    this.summaryProperty = summaryProperty;
    this.textColorProperty = textColorProperty;
    this.dueDateProperty = dueDateProperty;
  }

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    initialize();
  }
  private void initialize() {
    title.textProperty().bind(titleProperty);
    summary.textProperty().bind(summaryProperty);
    dueDate.textProperty().bind(dueDateProperty);
    title.textFillProperty().bind(textColorProperty);
    summary.textFillProperty().bind(textColorProperty);
    dueDate.textFillProperty().bind(textColorProperty);
    dueDateLabel.textFillProperty().bind(textColorProperty);

  }

}
