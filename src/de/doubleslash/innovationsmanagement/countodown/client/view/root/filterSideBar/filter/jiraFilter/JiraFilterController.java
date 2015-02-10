package de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.jiraFilter;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.AbstractFilterController;
import de.doubleslash.innovationsmanagement.countodown.client.view.util.multiValues.MultiValuesController;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraOption;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraQueryBuilder;
import de.doubleslash.innovationsmanagement.countodown.data.filter.TaskFilter;

public class JiraFilterController extends AbstractFilterController implements Initializable {

   private final List<JiraClauseLoader> clauseList = new LinkedList<>();
   private final String username;
   private final JiraOption nameAdress;

   public JiraFilterController(final String username, final JiraOption nameAdress,
         final AbstractFilterController superFilter) {
      super(superFilter);
      this.username = username;
      this.nameAdress = nameAdress;
   }

   @FXML
   Label jiraLabel;
   @FXML
   Label userLabel;

   @FXML
   VBox allClauses;

   @Override
   public void initialize(final URL location, final ResourceBundle resources) {
      initialize();
   }

   private void initialize() {
      jiraLabel.setText(nameAdress.getName());
      userLabel.setText(username);
      handleAddClause();

      final JiraClauseController clauseController = clauseList.get(0).getController();
      final String presetFilter = nameAdress.getPresetFilter();
      if (presetFilter != null && !presetFilter.isEmpty()) {
         setFilterClause(clauseController);
      } else {
         setNameClause(clauseController);
      }
   }

   private void setNameClause(final JiraClauseController clauseController) {
      clauseController.fields.setValue(JiraQueryBuilder.Field.Assignee);
      clauseController.operators.setValue(JiraQueryBuilder.Operator.EQUALS);
      final MultiValuesController multi = clauseController.multibox.getController();
      multi.setString(username);
   }

   private void setFilterClause(final JiraClauseController clauseController) {
      clauseController.fields.setValue(JiraQueryBuilder.Field.Filter);
      clauseController.operators.setValue(JiraQueryBuilder.Operator.EQUALS);
      final MultiValuesController multi = clauseController.multibox.getController();
      multi.setString(nameAdress.getPresetFilter());
   }

   @FXML
   private void handleAddClause() {
      final JiraClauseLoader newClause = new JiraClauseLoader();
      final JiraClauseController controller = newClause.getController();
      controller.setValidationListener((ignore) -> {
         removeClause(newClause);
      });
      clauseList.add(newClause);
      allClauses.getChildren().add(newClause.getView());
   }

   private void removeClause(final JiraClauseLoader clause) {
      allClauses.getChildren().remove(clause.getView());
      clauseList.remove(clause);
   }

   @Override
   public TaskFilter getTaskFilter() {
      final JiraQueryBuilder query = new JiraQueryBuilder(getMinDate(), getMaxDate(), getShowFinished(), username,
            nameAdress.getAdress());
      for (final JiraClauseLoader clauseLoader : clauseList) {
         final JiraClauseController controller = clauseLoader.getController();
         controller.addCurrentClauseToJiraQuery(query);
      }

      if (query.hasEntries()) {
         return query;
      } else {
         return null;
      }
   }

}
