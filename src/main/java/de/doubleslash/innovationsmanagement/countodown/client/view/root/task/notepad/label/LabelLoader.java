package de.doubleslash.innovationsmanagement.countodown.client.view.root.task.notepad.label;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class LabelLoader extends MVCLoader {

  private final String prefix;
  private final SimpleStringProperty titleProperty;
  private final SimpleStringProperty summaryProperty;
  private final SimpleStringProperty dueDateProperty;
  private final ObserverableValueImplementation<Color> textColorProperty;

  public LabelLoader(final String prefix, final SimpleStringProperty titleProperty,
      final SimpleStringProperty summaryProperty, final SimpleStringProperty dueDateProperty,
      final ObserverableValueImplementation<Color> textColorProperty) {

    this.prefix = prefix;
    this.titleProperty = titleProperty;
    this.summaryProperty = summaryProperty;
    this.dueDateProperty = dueDateProperty;
    this.textColorProperty = textColorProperty;
  }

  @Override
  protected Initializable controllerInstance() {
    return new LabelController(titleProperty, summaryProperty, dueDateProperty, textColorProperty);
  }

  @Override
  protected String getFXMLSimpleName() {
    final String fxmlName = super.getFXMLSimpleName();
    return prefix + fxmlName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public LabelController getController() {
    final LabelController out = super.getController();
    return out;
  }
}
