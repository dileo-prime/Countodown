package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.jiraOptions.jiraName;

import javafx.fxml.Initializable;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;

public class JiraNameLoader extends MVCLoader {

   @Override
   protected Initializable controllerInstance() {
      return new JiraNameController();
   }

}
