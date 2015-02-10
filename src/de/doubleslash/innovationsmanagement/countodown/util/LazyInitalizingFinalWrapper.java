package de.doubleslash.innovationsmanagement.countodown.util;

import org.apache.commons.lang3.Validate;

public class LazyInitalizingFinalWrapper<T> {

  private T value = null;

  private final Initalizer<T> init;

  private boolean initialized = false;

  @FunctionalInterface
  public interface Initalizer<T> {
    T initialize();
  }

  public LazyInitalizingFinalWrapper(final Initalizer<T> initializer) {
    this.init = initializer;
  }

  public T get() {
    if (!initialized) {
      value = init.initialize();
      initialized = true;
      Validate.notNull(value);
    }
    return value;
  }

}
