package de.doubleslash.innovationsmanagement.countodown.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SynchronizedIterator<E> implements Iterator<E> {
  Iterator<E> iter;

  public SynchronizedIterator(final Iterator<E> iter) {
    this.iter = iter;
  }

  /**
   * @return next Element or null if none left, ignores null Elements in Iterable
   */
  public E nextOrNull() {
    synchronized (iter) {
      E next;
      if (!iter.hasNext()) {
        return null;
      }
      next = iter.next();
      try {
        while (next == null) {
          next = iter.next();
        }
      } catch (final NoSuchElementException nse) {
        return null;
      }
      return next;
    }
  }

  @Override
  public boolean hasNext() {
    synchronized (iter) {
      return iter.hasNext();
    }
  }

  @Override
  public E next() {
    synchronized (iter) {
      return iter.next();
    }
  }
}
