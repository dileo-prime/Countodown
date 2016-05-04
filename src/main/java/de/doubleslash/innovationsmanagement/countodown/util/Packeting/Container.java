package de.doubleslash.innovationsmanagement.countodown.util.Packeting;

import java.util.LinkedList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;

import org.apache.commons.lang3.Validate;

class Container<T> implements Comparable<Container<T>> {

  public T getElement() {
    return element;
  }

  public int getXMin() {
    return xMin.get();
  }

  public int getYMin() {
    return yMin.get();
  }

  public int getXMax() {
    return xMax.get();
  }

  public int getYMax() {
    return yMax.get();
  }

  public int getXSize() {
    return xMax.get() - xMin.get();
  }

  public int getYSize() {
    return yMax.get() - yMin.get();
  }

  public void setXMin(final int xMin) {
    this.xMin.set(xMin);
  }

  public void setYMin(final int yMin) {
    this.yMin.set(yMin);
  }

  public void setXSize(final int xSize) {
    this.xMax.set(xMin.get() + xSize);
  }

  public void setYSize(final int ySize) {
    this.yMax.set(yMin.get() + ySize);
  }

  public void setXMax(final int max) {
    this.xMax.set(max);
  }

  public void setYMax(final int max) {
    this.yMax.set(max);
  }

  private final InvalidationListener invalid = new InvalidationListener() {

    @Override
    public void invalidated(final Observable observable) {
      System.out.println(this + " is invalid");

    }
  };
  private T element;
  private SimpleIntegerProperty xMin = new SimpleIntegerProperty();
  private SimpleIntegerProperty yMin = new SimpleIntegerProperty();
  private SimpleIntegerProperty xMax = new SimpleIntegerProperty();
  private SimpleIntegerProperty yMax = new SimpleIntegerProperty();

  private boolean aliveAndEmpty = true;

  public Container(final int xMin, final int yMin, final int xSize, final int ySize) {
    this.xMin.set(xMin);
    this.yMin.set(yMin);
    setXSize(xSize);
    setYSize(ySize);

    try {
      Validate.isTrue(xSize >= 0);
      Validate.isTrue(ySize >= 0);
    } catch (final Exception e) {
      System.out.println("CAUSE " + this);
      throw e;
    }
  }

  protected static <T> Container<T> containerFromFourPoints(final int xMin, final int yMin,
      final int xMax, final int yMax) {
    return new Container<T>(xMin, yMin, xMax - xMin, yMax - yMin);
  }

  public List<Container<T>> setElementAndResizeIfNecessary(final T element, final int size) {
    if (element == null) {
      throw new NullPointerException("Cannot insert null");
    }
    if (spareSpace(size) < 0) {
      throw new IllegalArgumentException("Size smaller then Container");
    }
    this.element = element;
    return resize(size);

  }

  private List<Container<T>> resize(final int newSize) {

    List<Container<T>> other = null;
    final int xdiff = getXSize() - newSize;
    if (xdiff > 0) {
      if (other == null) {
        other = new LinkedList<>();
      }
      other.add(new Container<T>(xMin.get() + newSize, yMin.get(), xdiff, getYSize()));
    }
    final int ydiff = getYSize() - newSize;
    if (ydiff > 0) {
      if (other == null) {
        other = new LinkedList<>();
      }
      other.add(new Container<T>(xMin.get(), yMin.get() + newSize, newSize, ydiff));
    }

    setYSize(newSize);
    setXSize(newSize);

    return other;
  }

  public int spareSpace(final int size) {

    if (size > getXSize() || size > getYSize()) {
      return -1;
    }
    return (getXSize() * getYSize()) - (size * size);
  }

  public boolean isEmpty() {
    return element == null;
  }

  public int getArea() {
    return (getXSize() * getYSize());
  }

  @Override
  public String toString() {
    return "[" + (isEmpty() ? "Empty" : "Filled") + ", XMIN: " + xMin.get() + ", XMAX: "
        + getXMax() + ", YMIN: " + yMin.get() + ", YMAX: " + getYMax() + "]";
  }

  public void kill() {
    aliveAndEmpty = false;
    xMax = new SimpleIntegerProperty(xMax.get());
    xMin = new SimpleIntegerProperty(xMin.get());
    yMax = new SimpleIntegerProperty(yMax.get());
    yMin = new SimpleIntegerProperty(yMin.get());
  }

  public boolean isAliveAndEmpty() {
    return aliveAndEmpty;
  }

  public void addXMaxListener(final ChangeListener<Number> listener) {
    xMax.addListener(listener);
  }

  public void addYMaxListener(final ChangeListener<Number> listener) {
    yMax.addListener(listener);
  }

  public void addXMinListener(final ChangeListener<Number> listener) {
    xMin.addListener(listener);
  }

  public void addYMinListener(final ChangeListener<Number> listener) {
    yMin.addListener(listener);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((xMax == null) ? 0 : xMax.get());
    result = prime * result + ((xMin == null) ? 0 : xMin.get());
    result = prime * result + ((yMax == null) ? 0 : yMax.get());
    result = prime * result + ((yMin == null) ? 0 : yMin.get());
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj.getClass().equals(getClass())) {
      return compareTo((Container<T>) obj) == 0;
    }
    return false;

  }

  @Override
  public int compareTo(final Container<T> other) {
    if (other == this) {
      return 0;
    }
    int out;
    out = xMin.getValue().compareTo(other.xMin.getValue());
    if (out != 0) {
      return out;
    }
    out = yMin.getValue().compareTo(other.yMin.getValue());
    if (out != 0) {
      return out;
    }
    out = xMax.getValue().compareTo(other.xMax.getValue());
    if (out != 0) {
      return out;
    }
    out = yMax.getValue().compareTo(other.yMax.getValue());

    return out;
  }

  void addValidationLissener() {
    xMax.addListener(invalid);
    yMax.addListener(invalid);
    xMin.addListener(invalid);
    yMin.addListener(invalid);
  }

  void removeValidationLissener() {
    xMax.removeListener(invalid);
    yMax.removeListener(invalid);
    xMin.removeListener(invalid);
    yMin.removeListener(invalid);
  }
}