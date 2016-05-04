package de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.jiraFilter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import de.doubleslash.innovationsmanagement.countodown.client.view.util.multiValues.MultiValuesController;
import de.doubleslash.innovationsmanagement.countodown.client.view.util.multiValues.MultiValuesLoader;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraQueryBuilder;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraQueryBuilder.Field;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraQueryBuilder.Operator;

public class JiraClauseController implements Initializable {

  private InvalidationListener toRemove;

  @FXML
  VBox criteria;

  @FXML
  ComboBox<JiraQueryBuilder.Field> fields;
  @FXML
  ComboBox<JiraQueryBuilder.Operator> operators;

  MultiValuesLoader multibox = new MultiValuesLoader();

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    initialize();
  }
  private void initialize() {
    criteria.getChildren().add(multibox.getView());
    fields.setItems(FXCollections.observableArrayList(JiraQueryBuilder.Field.values()));
    operators.setItems(FXCollections.observableArrayList(JiraQueryBuilder.Operator.values()));

  }

  public void setValidationListener(final InvalidationListener listener) {
    toRemove = listener;
  }

  @FXML
  private void handleRemove() {
    toRemove.invalidated(null);
  }

  protected void addCurrentClauseToJiraQuery(final JiraQueryBuilder query) {
    final MultiValuesController controller = multibox.getController();

    final Field field = fields.getValue();
    final Operator op = operators.getValue();
    final CharSequence[] parameter = controller.getAllStrings().toArray(new String[0]);
    if (field != null && op != null && parameter.length > 0) {
      query.addCriteria(field, op, parameter);
    }
  }
}
