package de.doubleslash.innovationsmanagement.countodown.data;

import java.time.LocalDate;

import org.apache.commons.lang3.Validate;

public class Task extends ReadWriteLocked {

  public final static long HOURS_PER_DAY = 8L;
  public final static long MINUTES_PER_HOUR = 60L;

  // i need some way to make a Task Instance unambiguous
  // source + ID of Task in source??

  private final String source;
  private final String key;

  private final String title;
  private String summary = null;

  private Person assignee = null;
  // NOTE: USE LOCALDATE
  private String dueDate;
  private long workToDo;
  private String description;
  private String onlineSource = null;

  private boolean finished;

  // ?????
  // private int importance;

  public Task(final String key, final String source, final String title, final String summary) {
    this(key, source, title, summary, null, null, 0L, "");
  }

  public Task(final String source, final String title, final String summary) {
    this(null, source, title, summary, null, null, 0L, "");
  }

  public Task(final String key, final String source, final String title, final String summary,
      final Person assignee, final LocalDate dueDate, final long workToDo, final String description) {
    this(key, source, title, summary, assignee, dueDate, workToDo, description, false);
  }

  public Task(final String key, final String source, final String title, final String summary,
      final Person assignee, final LocalDate dueDate, final long workToDo,
      final String description, final boolean finished) {
    this.source = source;
    if (key == null) {
      this.key = title;
    } else {
      this.key = key;
    }
    this.title = title;

    this.summary = summary;
    this.assignee = assignee;
    if (dueDate != null) {
      this.dueDate = dueDate.toString();
    } else {
      this.dueDate = LocalDate.now().plusDays(1).toString();// tomorrow
    }
    this.workToDo = workToDo;
    if (this.assignee != null) {
      this.assignee.addParent(this);
    }
    if (description != null) {
      this.description = description;
    } else {
      this.description = "";
    }

    this.finished = finished;

    Validate.notEmpty(this.key);
    Validate.notEmpty(this.source);
  }

  @SuppressWarnings("unused")
  // is called in JsonSerializer
  private Task() {
    this.title = null;
    this.key = null;
    this.source = null;
  }

  private Task(final Task old) {
    this(old.key, old.source, old.title, old.summary, (old.assignee != null ? (Person) old.assignee
        .getEqual() : null), (old.dueDate != null ? LocalDate.parse(old.dueDate) : null),
        old.workToDo, old.description, old.finished);
  }

  @Override
  public void mutateTo(final ReadWriteLocked other) throws IllegalArgumentException {
    if (other instanceof Task) {
      mutateTo((Task) other);
    } else {
      throw new IllegalArgumentException("Argument mus be instance of Task");
    }
  }

  private void mutateTo(final Task other) throws IllegalArgumentException {
    if (this.key != other.key || this.source != other.source) {
      throw new IllegalArgumentException("Cannot mutate this: " + this + " to " + other
          + "! Both Objects must discribe the same Task");
    }
    this.summary = other.summary;
    this.dueDate = other.dueDate;
    this.workToDo = other.workToDo;

    if (this.assignee != null) {
      this.assignee.removeParent(this);
    }
    if (other.assignee != null) {
      this.assignee = (Person) other.assignee.getEqual();

      this.assignee.addParent(this);
    }

  }

  public String getTitle() {
    readLock();
    try {
      return title;
    } finally {
      readUnLock();
    }
  }

  public String getSummary() {
    readLock();
    try {
      return summary;
    } finally {
      readUnLock();
    }
  }

  public Person getAssignee() {
    readLock();
    try {
      return assignee;
    } finally {
      readUnLock();
    }
  }

  public LocalDate getDueDate() {
    readLock();
    try {
      return LocalDate.parse(dueDate);
    } finally {
      readUnLock();
    }
  }

  public String getSource() {
    readLock();
    try {
      return source;
    } finally {
      readUnLock();
    }
  }

  /**
   * @return the key of this Task, note that the key might not be unique, use {@link #getFileName()}
   *         for a unique Name
   */
  public String getKey() {
    readLock();
    try {
      return key;
    } finally {
      readUnLock();
    }
  }

  public long getWorkToDo() {
    readLock();
    try {
      return workToDo;
    } finally {
      readUnLock();
    }
  }

  public String getDescription() {
    readLock();
    try {
      return description;
    } finally {
      readUnLock();
    }
  }

  public String getOnlineSource() {
    readLock();
    try {
      return onlineSource;
    } finally {
      readUnLock();
    }
  }

  public boolean isFinished() {
    readLock();
    try {
      return finished;
    } finally {
      readUnLock();
    }
  }

  public void setOnlineSource(final String onlineSource) {
    writeLock();
    try {

      if (this.onlineSource == onlineSource
          || (onlineSource != null && onlineSource.equals(this.onlineSource))) {
        return; // String is immutable so test for equal, test for same for both might be null
      }
      this.onlineSource = onlineSource;
    } finally {
      unlockWriteLockAndInformObservers();
    }
  }

  public void setSummary(final String summary) {
    writeLock();
    try {
      if (this.summary == summary || (summary != null && summary.equals(this.summary))) {
        return; // String is immutable so test for equal, test for same for both might be null
      }
      this.summary = summary;
    } finally {
      unlockWriteLockAndInformObservers();
    }
  }

  public void setAssignee(final Person assignee) {
    writeLock();
    try {
      if (this.assignee == assignee) {
        return; // test for Same object, not equal object
      }
      if (this.assignee != null) {
        this.assignee.removeParent(this);
      }
      this.assignee = assignee;
      if (assignee != null) {
        assignee.addParent(this);
      }
    } finally {
      unlockWriteLockAndInformObservers();
    }
  }

  public void setDueDate(final LocalDate dueDate) {
    writeLock();
    try {
      if ((dueDate != null && dueDate.toString().equals(this.dueDate))) {
        return; // LocalDate is immutable so test for equal, test for same for both might be null
      }
      this.dueDate = dueDate.toString();
    } finally {
      unlockWriteLockAndInformObservers();
    }
  }

  public void setWorkToDo(final long workToDo) {
    writeLock();
    try {
      if (this.workToDo == workToDo) {
        return;
      }
      this.workToDo = workToDo;
    } finally {
      unlockWriteLockAndInformObservers();
    }
  }

  public void setDescription(final String description) {
    writeLock();
    try {
      if (this.description.equals(description)) {
        return;
      }
      this.description = description;
    } finally {
      unlockWriteLockAndInformObservers();
    }
  }

  public void setFinished() {
    writeLock();
    try {
      finished = true;
    } finally {
      unlockWriteLockAndInformObservers();
    }
  }

  @Override
  public ReadWriteLocked getEqual() {
    return new Task(this);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("title: " + title).append('\n').append("key: " + key).append('\n')
        .append("source: " + source).append('\n').append("summary: " + summary);

    return sb.toString();
  }

}
