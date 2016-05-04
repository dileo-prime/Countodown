package de.doubleslash.innovationsmanagement.countodown.util.Packeting;

import java.util.LinkedList;
import java.util.List;

import javafx.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ContainerWrapper<T> {

  private final static Logger logger = LoggerFactory.getLogger(ContainerWrapper.class);

  private int minX = 0;
  private int minY = 0;
  private int currentXSize = 0;
  private int currentYSize = 0;

  protected double getXSize() {
    return currentXSize;
  }

  protected double getYSize() {
    return currentYSize;
  }

  protected final LinkedList<Container<T>> onOutsideList = new LinkedList<>();

  private final LinkedList<Container<T>> emptyContainers = new LinkedList<>();
  private final List<Container<T>> filledContainers = new LinkedList<>();

  private final LinkedList<Container<T>> unMergedContainers = new LinkedList<>();

  // ListWrapper listWrapperCache = new ListWrapper();

  protected ContainerWrapper() {
    handleNewContainer(new Container<T>(0, 0, 0, 0));
  }

  protected List<Container<T>> getEmptyContainers() {
    // Collections.sort(emptyContainers);
    // Collections.reverse(emptyContainers);
    return emptyContainers;
  }

  protected List<Container<T>> getFilledContainers() {
    return filledContainers;
  }

  protected void resiszeArea(final int minX, final int minY, final int currentXSize,
      final int currentYSize) {

    final int minXOld = this.minX;
    final int minYOld = this.minY;
    final int oldXSize = this.currentXSize;
    final int oldYSize = this.currentYSize;

    this.minX = minX;
    this.minY = minY;
    this.currentXSize = currentXSize;
    this.currentYSize = currentYSize;

    resiszeAllContainersOnOutside(minXOld, minYOld, oldXSize, oldYSize);
    mergeAll();
  }

  protected Pair<Integer, Integer> getSmallestEmptyOnMAXSite() {
    int minXSize = Integer.MAX_VALUE;
    int minYSize = Integer.MAX_VALUE;
    for (final Container<T> current : onOutsideList) {
      if (current.getXMax() == minX + currentXSize) {
        minXSize = getSmaller(minXSize, current.getXSize());
        if (!current.isEmpty()) {
          return null;
        }
      }
      if (current.getYMax() == minY + currentYSize) {
        minYSize = getSmaller(minYSize, current.getYSize());
        if (!current.isEmpty()) {
          return null;
        }
      }
    }
    return new Pair<>(minXSize, minYSize);
  }

  private void resiszeAllContainersOnOutside(final int minXOld, final int minYOld,
      final int oldXSize, final int oldYSize) {

    final int maxXOld = minXOld + oldXSize;
    final int maxYOld = minYOld + oldYSize;
    final int maxX = minX + currentXSize;
    final int maxY = minY + currentYSize;

    @SuppressWarnings("unchecked")
    final List<Container<T>> allContainerOnOutside = (List<Container<T>>) onOutsideList.clone();

    for (final Container<T> current : allContainerOnOutside) {

      final int oldContainerSize = current.getXSize();

      if (current.getXMin() == minXOld) {
        setContainerXMinAndKeepMax(current, minX);
      }
      if (current.getXMax() == maxXOld) {
        setContainerXSize(current, current.getXSize() + maxX - maxXOld);
      }
      if (current.getYMin() == minYOld) {
        setContainerYMinAndKeepMax(current, minY);
      }
      if (current.getYMax() == maxYOld) {
        setContainerYSize(current, current.getYSize() + maxY - maxYOld);
      }
      if (!current.isEmpty()) {
        handleNewContainers(current.setElementAndResizeIfNecessary(current.getElement(),
            oldContainerSize));
        if (!containerOnOutside(current)) {
          onOutsideList.remove(current);
        }
      } else {
        unMergedContainers.addLast(current);
      }
    }
  }

  private void setContainerXMinAndKeepMax(final Container<T> current, final int newMin) {
    if (current.getXMin() == newMin) {
      return;
    }

    current.setXMin(newMin);

  }

  private void setContainerXSize(final Container<T> current, final int newValue) {
    if (newValue == current.getXSize()) {
      return;
    }
    current.setXSize(newValue);
  }

  private void setContainerYMinAndKeepMax(final Container<T> current, final int newMin) {
    if (newMin == current.getYMin()) {
      return;
    }

    current.setYMin(newMin);

  }

  private void setContainerYSize(final Container<T> current, final int newValue) {
    if (newValue == current.getYSize()) {
      return;
    }
    current.setYSize(newValue);
  }

  private void handleNewContainer(final Container<T> current) {

    if (containerOnOutside(current)) {
      onOutsideList.add(current);
    }

    emptyContainers.add(current);
  }

  protected boolean containerOnOutside(final Container<T> c) {
    return (c.getXMin() == minX || c.getYMin() == minY || c.getXMax() == minX + currentXSize || c
        .getYMax() == minY + currentYSize);
  }

  protected long getFillPercent() {
    if (currentXSize == 0 || currentYSize == 0) {
      return -1;
    }
    long area = 0;

    for (final Container<T> c : filledContainers) {
      area += c.getXSize() * c.getYSize();
    }
    area *= 100;
    return area / (currentXSize * currentYSize);
  }

  protected void insertIntoContainer(final T element, final Container<T> container,
      final int elmentSize) {

    removeEmptyContainer(container);

    handleNewContainers(container.setElementAndResizeIfNecessary(element, elmentSize));

    mergeAll();

    filledContainers.add(container);
    if (containerOnOutside(container)) {
      onOutsideList.add(container);
    }

  }

  private void removeEmptyContainer(final Container<T> container) {
    if (containerOnOutside(container)) {
      onOutsideList.remove(container);
    }
    if (!container.isAliveAndEmpty() && logger.isInfoEnabled()) {
      logger.info("removing dead container: " + container);
    }
    container.kill();

    emptyContainers.remove(container);
  }

  private boolean tryMerge(final Container<T> cont1, final Container<T> cont2) {
    if (cont1 == cont2) {
      return false;
    }
    // find biggest MIN and smalles MAX
    int minXNew = getBigger(cont1.getXMin(), cont2.getXMin());
    int minYNew = getBigger(cont1.getYMin(), cont2.getYMin());
    int maxXNew = getSmaller(cont1.getXMax(), cont2.getXMax());
    int maxYNew = getSmaller(cont1.getYMax(), cont2.getYMax());
    // find equal axis and expand point
    if (minXNew == maxXNew) {
      if (maxYNew < minYNew) {
        return false;
      }
      minXNew = getSmaller(cont1.getXMin(), cont2.getXMin());
      maxXNew = getBigger(cont1.getXMax(), cont2.getXMax());

    } else if (minYNew == maxYNew) {
      if (maxXNew < minXNew) {
        return false;
      }
      minYNew = getSmaller(cont1.getYMin(), cont2.getYMin());
      maxYNew = getBigger(cont1.getYMax(), cont2.getYMax());
    } else {
      return false;
    }
    final Container<T> newCont = Container.containerFromFourPoints(minXNew, minYNew, maxXNew,
        maxYNew);

    int areaNew = getSmaller(newCont.getXSize(), newCont.getYSize());
    int area1 = getSmaller(cont1.getXSize(), cont1.getYSize());
    int area2 = getSmaller(cont2.getXSize(), cont2.getYSize());

    areaNew *= areaNew;
    area1 *= area1;
    area2 *= area2;

    if (areaNew <= area1 || areaNew <= area2) {
      return false;
    }

    final List<Container<T>> list = cutFrom(cont1, newCont);
    list.addAll(cutFrom(cont2, newCont));
    list.add(newCont);

    removeEmptyContainer(cont1);
    removeEmptyContainer(cont2);

    handleNewContainers(list);
    return true;
  }

  private int getSmaller(final int i1, final int i2) {
    if (i1 < i2) {
      return i1;
    } else {
      return i2;
    }
  }

  private int getBigger(final int i1, final int i2) {
    if (i1 > i2) {
      return i1;
    } else {
      return i2;
    }
  }

  private List<Container<T>> cutFrom(final Container<T> outer, final Container<T> inner) {

    final List<Container<T>> out = new LinkedList<>();
    if (outer.getXMin() < inner.getXMin()) {
      final Container<T> cont = Container.containerFromFourPoints(outer.getXMin(), outer.getYMin(),
          inner.getXMin(), outer.getYMax());
      if (cont.getArea() > 0) {
        out.add(cont);
      }
    }
    if (outer.getYMin() < inner.getYMin()) {
      final Container<T> cont = Container.containerFromFourPoints(outer.getXMin(), outer.getYMin(),
          outer.getXMax(), inner.getYMin());
      if (cont.getArea() > 0) {
        out.add(cont);
      }
    }
    if (outer.getXMax() > inner.getXMax()) {
      final Container<T> cont = Container.containerFromFourPoints(inner.getXMax(), outer.getYMin(),
          outer.getXMax(), outer.getYMax());
      if (cont.getArea() > 0) {
        out.add(cont);
      }
    }
    if (outer.getYMax() > inner.getYMax()) {
      final Container<T> cont = Container.containerFromFourPoints(outer.getXMin(), inner.getYMax(),
          outer.getXMax(), outer.getYMax());
      if (cont.getArea() > 0) {
        out.add(cont);
      }
    }
    return out;
  }

  private void handleNewContainers(final List<Container<T>> list) {
    if (list == null) {
      return;
    }
    for (final Container<T> container : list) {
      handleNewContainer(container);
      unMergedContainers.addLast(container);
    }
  }

  private void mergeAll() {
    // unMergedContainers.clear();
    // unMergedContainers.addAll(emptyContainers);

    while (unMergedContainers.size() > 0) {
      final Container<T> current = unMergedContainers.poll();
      if (!current.isAliveAndEmpty()) {
        continue;
      }
      mergeWithNeighbors(current);
    }
  }

  private void mergeWithNeighbors(final Container<T> container) {

    for (final Container<T> current : getAllPossibleEmptyNeighborsToContainer(container)) {

      if (!current.isAliveAndEmpty() && logger.isInfoEnabled()) {
        logger.info("dead Entry: " + current);
        continue;
      }
      if (tryMerge(container, current)) {
        return;
      }
    }
  }

  @SuppressWarnings("unchecked")
  private List<Container<T>> getAllPossibleEmptyNeighborsToContainer(final Container<T> current) {
    return (List<Container<T>>) emptyContainers.clone();
  }

}
