package de.doubleslash.innovationsmanagement.countodown.client.view.root.rootLayout;

import javafx.fxml.Initializable;
import javafx.stage.Stage;
import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;

public class RootLayoutLoader extends MVCLoader {

  private final Stage stage;
  private final EntryPointBackend backend;

  public RootLayoutLoader(final Stage stage, final EntryPointBackend backend) {
    this.stage = stage;
    this.backend = backend;
  }

  @Override
  protected Initializable controllerInstance() {
    return new RootLayoutController(stage, backend);
  }
}
