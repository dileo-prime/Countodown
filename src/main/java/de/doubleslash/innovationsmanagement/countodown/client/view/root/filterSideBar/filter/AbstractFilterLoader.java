package de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter;

import javafx.fxml.Initializable;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;

public abstract class AbstractFilterLoader extends MVCLoader {

  protected AbstractFilterController superFilter = null;

  public void initialize(final AbstractFilterController superFilter) {
    this.superFilter = superFilter;
  }

  public <T extends AbstractFilterController> T getFilterController() {
    return super.getController();
  }

  abstract protected AbstractFilterController getAbstractFilterControllerInstance();

  @Override
  protected final Initializable controllerInstance() {
    return getAbstractFilterControllerInstance();
  }

}
