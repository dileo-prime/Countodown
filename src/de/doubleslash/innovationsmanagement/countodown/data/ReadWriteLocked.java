package de.doubleslash.innovationsmanagement.countodown.data;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import org.apache.commons.lang3.Validate;
import org.boon.json.annotations.JsonIgnore;

public abstract class ReadWriteLocked implements Observable {

  // Note: Json serializes an equals field due to the getEquals method, this field is then filled
  // with itself, thus leading to an infinite loop
  @SuppressWarnings("unused")
  private final Object equal = null;

  @JsonIgnore
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  @JsonIgnore
  private final List<InvalidationListener> observers = new LinkedList<InvalidationListener>();

  @JsonIgnore
  private final InvalidationListener assigneeListener = (o) -> {
    informObservers();
  };

  @JsonIgnore
  private final LinkedHashSet<ReadWriteLocked> parents = new LinkedHashSet<ReadWriteLocked>();

  protected void addParent(final ReadWriteLocked parent) {
    Validate.notNull(parent);
    lock.writeLock().lock();
    parents.add(parent);
    lock.writeLock().unlock();
  }

  protected void removeParent(final ReadWriteLocked parent) {
    Validate.notNull(parent);
    boolean removed = false;
    lock.writeLock().lock();
    try {
      removed = parents.remove(parent);
    } finally {
      lock.writeLock().unlock();
      Validate.isTrue(removed, "No Parent to remove");
    }

  }

  protected void unlockWriteLockAndInformObservers() {
    writeUnLock();
    informObservers();
    for (final ReadWriteLocked parent : parents) {
      parent.informObservers();
    }
  }

  private void informObservers() {
    observers.forEach((il) -> {
      il.invalidated(this);
    });
  }

  @Override
  public void addListener(final InvalidationListener listener) {
    observers.add(listener);
  }

  @Override
  public void removeListener(final InvalidationListener listener) {
    observers.remove(listener);
  }

  public void doReadLocked(final Consumer<ReadWriteLocked> c) {
    try {
      readLock();
      c.accept(this);
    } finally {
      readUnLock();
    }
  }

  abstract public ReadWriteLocked getEqual();

  abstract public void mutateTo(final ReadWriteLocked other) throws IllegalArgumentException;

  protected void readLock() {
    for (final ReadWriteLocked parent : parents) {
      parent.readLock();
    }
    lock.readLock().lock();
  }

  protected void writeLock() {
    for (final ReadWriteLocked parent : parents) {
      parent.writeLock();
    }
    lock.writeLock().lock();
  }

  protected void readUnLock() {
    lock.readLock().unlock();
    for (final ReadWriteLocked parent : parents) {
      parent.readUnLock();
    }
  }

  private void writeUnLock() {
    lock.writeLock().unlock();
    for (final ReadWriteLocked parent : parents) {
      parent.writeUnLock();
    }
  }

}
