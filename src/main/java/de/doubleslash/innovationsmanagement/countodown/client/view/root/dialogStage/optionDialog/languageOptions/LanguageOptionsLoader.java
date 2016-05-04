package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.languageOptions;

import javafx.fxml.Initializable;
import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;

public class LanguageOptionsLoader extends MVCLoader {

  private final EntryPointBackend backend;

  public LanguageOptionsLoader(final EntryPointBackend backend) {
    this.backend = backend;
  }

  @Override
  protected Initializable controllerInstance() {
    return new LanguageOptionsController(backend);
  }

}
