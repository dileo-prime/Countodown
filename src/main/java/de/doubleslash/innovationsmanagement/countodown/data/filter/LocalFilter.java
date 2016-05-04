package de.doubleslash.innovationsmanagement.countodown.data.filter;

import java.time.LocalDate;

public class LocalFilter extends TaskFilter {

  public LocalFilter(final LocalDate dueDateFrom, final LocalDate dueDateTo,
      final boolean showFinished) {
    super(dueDateFrom, dueDateTo, showFinished);
  }

  public LocalDate getDueDateFrom() {
    return dueDateFrom;
  }

  public LocalDate getDueDateTo() {
    return dueDateTo;
  }

  public boolean showFinished() {
    return showFinished;
  }

}
