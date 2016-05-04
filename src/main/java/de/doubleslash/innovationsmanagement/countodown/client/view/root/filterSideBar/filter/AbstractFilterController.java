package de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter;

import java.time.LocalDate;

import javafx.fxml.Initializable;

import org.apache.commons.lang3.Validate;

import de.doubleslash.innovationsmanagement.countodown.data.filter.TaskFilter;

public abstract class AbstractFilterController implements Initializable {

  private final AbstractFilterController superFilter;

  public AbstractFilterController(final AbstractFilterController superFilter) {
    this.superFilter = superFilter;
  }

  public LocalDate getMinDate() {
    Validate.notNull(superFilter);
    return superFilter.getMinDate();
  }

  public LocalDate getViewDate() {
    Validate.notNull(superFilter);
    return superFilter.getViewDate();
  }

  public LocalDate getMaxDate() {
    Validate.notNull(superFilter);
    return superFilter.getMaxDate();
  }

  public boolean getShowFinished() {
    Validate.notNull(superFilter);
    return superFilter.getShowFinished();
  }

  public abstract TaskFilter getTaskFilter();

}
