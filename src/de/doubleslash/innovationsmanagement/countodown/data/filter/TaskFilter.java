package de.doubleslash.innovationsmanagement.countodown.data.filter;

import java.time.LocalDate;

public abstract class TaskFilter {
  protected final LocalDate dueDateFrom;
  protected final LocalDate dueDateTo;

  protected final boolean showFinished;

  public TaskFilter(final LocalDate dueDateFrom, final LocalDate dueDateTo,
      final boolean showFinished) {
    this.dueDateFrom = dueDateFrom;
    this.dueDateTo = dueDateTo;
    this.showFinished = showFinished;
  }

}
