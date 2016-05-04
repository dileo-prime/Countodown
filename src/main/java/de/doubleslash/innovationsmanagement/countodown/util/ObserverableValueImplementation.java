package de.doubleslash.innovationsmanagement.countodown.util;

import javafx.beans.value.ObservableValueBase;

public class ObserverableValueImplementation<V> extends ObservableValueBase<V> {

  V value;

  public ObserverableValueImplementation() {
    this(null);
  }

  public ObserverableValueImplementation(final V value) {
    this.value = value;
  }

  public synchronized void set(final V value) {
    this.value = value;
    fireValueChangedEvent();
  }

  @Override
  public V getValue() {
    return value;
  }

}
