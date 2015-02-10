package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.jiraOptions;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.jiraOptions.jiraName.JiraNameController;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.jiraOptions.jiraName.JiraNameLoader;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraOption;
import de.doubleslash.innovationsmanagement.countodown.util.DialogHelper;

public class JiraOptionsController implements Initializable {

   private final EntryPointBackend backend;

   public JiraOptionsController(final EntryPointBackend backend) {
      this.backend = backend;
   }

   List<JiraNameLoader> jiraNameList = new LinkedList<>();

   @FXML
   VBox jiraNameContainer;

   @FXML
   ScrollPane rootPane;

   @Override
   public void initialize(final URL location, final ResourceBundle resources) {
      initialize();
   }

   private void initialize() {
      rootPane.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
         handleKeyEvent(event);
      });
      final List<JiraOption> nameAdressList = backend.getAllJiraServer();
      if (nameAdressList != null) {
         for (final JiraOption nameAdress : nameAdressList) {
            handleNew(nameAdress);
         }
      }
      handleNew();
   }

   private void handleKeyEvent(final KeyEvent event) {
      if (event.isShortcutDown() && event.getCode().equals(KeyCode.S)) {
         handleSave();
      }
   }

   @FXML
   private void handleSave() {

      final List<JiraOption> nameAdressList = new LinkedList<>();
      for (final JiraNameLoader loader : jiraNameList) {
         final JiraNameController controller = loader.getController();
         final JiraOption nameAdress = controller.getJiraOption();

         if (nameAdress == null) {
            continue;
         }
         if (!adressIsValid(nameAdress.getAdress())) {
            return;
         }
         nameAdressList.add(nameAdress);
      }

      final Alert alert = DialogHelper.createInformation();
      alert.setTitle("Saving");
      alert.setHeaderText(null);
      alert.setContentText("Saving Jira Options");
      alert.show();

      backend.saveAllJiraServer(nameAdressList);
   }

   private boolean adressIsValid(final String jiraAdress) {
      if (backend.checkIfValidJira(jiraAdress)) {
         return true;
      }
      final Alert alert = DialogHelper.createWarning();
      alert.setTitle("Invalid Server");
      alert.setHeaderText(null);
      alert.setContentText("Invalid Server: " + jiraAdress);
      alert.show();
      return false;
   }

   @FXML
   private void handleNew() {
      handleNew(null);
   }

   private void handleNew(final JiraOption nameAdress) {
      final JiraNameLoader loader = new JiraNameLoader();
      jiraNameContainer.getChildren().add(loader.getView());
      jiraNameList.add(loader);
      final JiraNameController controller = loader.getController();

      if (nameAdress != null) {
         controller.setJiraOption(nameAdress);
      }

      controller.addListener((ignore) -> {
         handleInvalid(loader);
      });
   }

   private void handleInvalid(final JiraNameLoader loader) {
      jiraNameList.remove(loader);
      jiraNameContainer.getChildren().remove(loader.getView());
   }

}
