package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.colorOptions;

import javafx.fxml.Initializable;
import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;

public class ColorOptionsLoader extends MVCLoader {

  private final EntryPointBackend backend;

  public ColorOptionsLoader(final EntryPointBackend backend) {
    this.backend = backend;
  }

  @Override
  protected Initializable controllerInstance() {
    return new ColorOptionsController(backend);
  }
}
