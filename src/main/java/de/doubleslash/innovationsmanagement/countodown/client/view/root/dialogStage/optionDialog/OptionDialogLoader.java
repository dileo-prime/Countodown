package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog;

import javafx.fxml.Initializable;
import javafx.stage.Stage;
import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.DialogStageLoader;

public class OptionDialogLoader extends DialogStageLoader {

  private static final String OPTIONS = "Options";
  private final EntryPointBackend backend;

  public OptionDialogLoader(final Stage primaryStage, final EntryPointBackend backend) {
    super(primaryStage);
    this.backend = backend;
    stage.setTitle(getResources().getString(OPTIONS));
  }

  @Override
  protected Initializable controllerInstance() {
    return new OptionDialogController(backend);
  }
}
