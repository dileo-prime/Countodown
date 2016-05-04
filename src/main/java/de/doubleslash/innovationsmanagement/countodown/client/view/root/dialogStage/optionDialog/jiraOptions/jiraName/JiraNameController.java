package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.jiraOptions.jiraName;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraOption;

public class JiraNameController implements Initializable, Observable {

   private final String ERROR = "error";

   private final List<InvalidationListener> observer = new LinkedList<>();

   public JiraNameController() {}

   @FXML
   private TextField nameField;
   @FXML
   private TextField adressField;
   @FXML
   private TextField filterField;

   @Override
   public void initialize(final URL location, final ResourceBundle resources) {
      initialize();
   }

   private void initialize() {
      nameField.getStyleClass().add(ERROR);
      adressField.getStyleClass().add(ERROR);
      addValidationChecker(nameField);
      addValidationChecker(adressField);
   }

   private void addValidationChecker(final TextField tf) {
      final ObservableList<String> styleClass = tf.getStyleClass();
      tf.textProperty().addListener((obs, oldV, newV) -> {
         checkIfValid(newV, styleClass);
      });
   }

   private void checkIfValid(final String text, final ObservableList<String> styleClass) {
      if (text == null || text.trim().isEmpty()) {
         if (!styleClass.contains(ERROR)) {
            styleClass.add(ERROR);
         }
      } else {
         if (styleClass.contains(ERROR)) {
            styleClass.remove(ERROR);
         }

      }
   }

   @FXML
   private void handleRemove() {
      notifyObservers();
   }

   @Override
   public void addListener(final InvalidationListener listener) {
      observer.add(listener);
   }

   @Override
   public void removeListener(final InvalidationListener listener) {
      observer.remove(listener);
   }

   private void notifyObservers() {
      for (final InvalidationListener listener : observer) {
         listener.invalidated(this);
      }
   }

   public void setJiraOption(final JiraOption preset) {
      String name = preset.getName();
      String adress = preset.getAdress();
      String filter = preset.getPresetFilter();
      if (name == null) {
         name = "";
      }
      if (adress == null) {
         adress = "";
      }
      if (filter == null) {
         filter = "";
      }
      nameField.setText(name);
      adressField.setText(adress);
      filterField.setText(filter);
   }

   public JiraOption getJiraOption() {
      final String name = nameField.getText().trim();
      final String adress = adressField.getText().trim();
      final String filter = filterField.getText().trim();
      if (name.isEmpty() || adress.isEmpty()) {
         return null;
      } else {
         return new JiraOption(name, adress, filter); // FIXME: add Filter
      }
   }
}
