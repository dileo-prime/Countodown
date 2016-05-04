package de.doubleslash.innovationsmanagement.countodown.client.view.util.comboDatePicker;

import javafx.fxml.Initializable;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;

public class ComboDatePickerLoader extends MVCLoader {

  public ComboDatePickerLoader() {}

  @Override
  protected Initializable controllerInstance() {
    return new ComboDatePickerController();
  }

}
