package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.colorOptions.ColorOptionsLoader;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.jiraOptions.JiraOptionsLoader;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.languageOptions.LanguageOptionsLoader;

public class OptionDialogController implements Initializable {

  // private final EntryPointBackend backend;
  private final ColorOptionsLoader colorOptionsloader;
  private final JiraOptionsLoader jiraOptionsloader;
  private final LanguageOptionsLoader languageOptionsLoader;

  public OptionDialogController(final EntryPointBackend backend) {
    // this.backend = backend;
    this.colorOptionsloader = new ColorOptionsLoader(backend);
    this.jiraOptionsloader = new JiraOptionsLoader(backend);
    this.languageOptionsLoader = new LanguageOptionsLoader(backend);
  }

  @FXML
  Tab colorOptionsTab;
  @FXML
  Tab jiraOptionsTab;
  @FXML
  Tab languageOptionsTab;

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    initialize();
  }

  private void initialize() {
    colorOptionsTab.setContent(colorOptionsloader.getView());
    jiraOptionsTab.setContent(jiraOptionsloader.getView());
    languageOptionsTab.setContent(languageOptionsLoader.getView());
  }

}
