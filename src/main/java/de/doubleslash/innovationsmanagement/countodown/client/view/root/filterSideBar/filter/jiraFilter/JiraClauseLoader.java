package de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.jiraFilter;

import javafx.fxml.Initializable;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;

public class JiraClauseLoader extends MVCLoader {

  @Override
  protected Initializable controllerInstance() {
    return new JiraClauseController();
  }

}
