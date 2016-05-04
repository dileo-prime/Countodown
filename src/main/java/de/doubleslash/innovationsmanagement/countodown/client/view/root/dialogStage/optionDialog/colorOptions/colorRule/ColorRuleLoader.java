package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.colorOptions.colorRule;

import javafx.fxml.Initializable;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;
import de.doubleslash.innovationsmanagement.countodown.data.filter.ColorDateOption;

public class ColorRuleLoader extends MVCLoader implements Comparable<ColorRuleLoader> {

  final ColorDateOption option;

  public ColorRuleLoader(final ColorDateOption option) {
    this.option = option;
  }

  @Override
  protected Initializable controllerInstance() {
    return new ColorRuleController(option);
  }

  @Override
  public int compareTo(final ColorRuleLoader o) {
    final ColorRuleController myController = getController();
    final ColorRuleController otherController = o.getController();
    return myController.compareTo(otherController);
  }

}
