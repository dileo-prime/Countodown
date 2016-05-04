package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.jiraOptions;

import javafx.fxml.Initializable;
import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;

public class JiraOptionsLoader extends MVCLoader {

  private final EntryPointBackend backend;

  public JiraOptionsLoader(final EntryPointBackend backend) {
    this.backend = backend;
  }

  @Override
  protected Initializable controllerInstance() {
    return new JiraOptionsController(backend);
  }

}
