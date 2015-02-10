package de.doubleslash.innovationsmanagement.countodown.client.view.util.multiValues;

import javafx.fxml.Initializable;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;

public class MultiValuesLoader extends MVCLoader {

  @Override
  protected Initializable controllerInstance() {
    return new MultiValuesController();
  }

}
