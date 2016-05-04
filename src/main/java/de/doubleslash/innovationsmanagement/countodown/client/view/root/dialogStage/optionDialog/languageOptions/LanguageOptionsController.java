package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.languageOptions;

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;

public class LanguageOptionsController implements Initializable {

  private final static String DEFAULT = "Default";

  private final static Locale[] supportedLangs = {
      Locale.GERMAN, Locale.ENGLISH
  };

  private final Map<String, Locale> localName2Local;

  @FXML
  private Label showReload;

  @FXML
  private ComboBox<String> combobox;

  private final EntryPointBackend backend;
  private final Locale using;
  private final Locale def;

  public LanguageOptionsController(final EntryPointBackend backend) {
    this.backend = backend;
    final Locale def = Locale.getDefault();
    if (arrayContains(supportedLangs, def)) {
      this.def = def;
    } else {
      this.def = null;
    }
    this.using = backend.getCurrentLanguage();

    this.localName2Local = new HashMap<>(supportedLangs.length + 1, 1);
  }

  private <E> boolean arrayContains(final E[] array, final E elem) {
    for (final E current : array) {
      if (current != null && current.equals(elem)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    showReload.setVisible(false);
    for (final Locale current : supportedLangs) {
      localName2Local.put(current.getDisplayLanguage(), current);
    }
    final String defaultLang = resources.getString(DEFAULT);
    localName2Local.put(defaultLang, null);
    combobox.getItems().addAll(localName2Local.keySet());

    if (using == null) {
      combobox.setValue(defaultLang);
    } else {
      combobox.setValue(using.getDisplayLanguage());
    }

    setReloadMessage(using);

    combobox.valueProperty().addListener((obs, oldV, newV) -> {
      handleValueChanged(newV);
    });
  }

  private void handleValueChanged(final String newV) {
    final Locale current = localName2Local.get(newV);
    setReloadMessage(current);
    backend.saveCurrentLanguage(current);
  }

  private void setReloadMessage(final Locale current) {
    if (current == def || (current != null && current.equals(def))) {
      showReload.setVisible(false);
    } else {
      showReload.setVisible(true);
    }
  }
}
