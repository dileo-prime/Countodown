package de.doubleslash.innovationsmanagement.countodown.util.Packeting;

import java.util.LinkedList;
import java.util.List;

import javafx.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SquarePacketing<T> {

  private final static Logger logger = LoggerFactory.getLogger(SquarePacketing.class);

  private final static double STD_RATIO = 1.5;

  private volatile ContainerWrapper<T> wrapper;

  private final double ratio; // x/y
  private int currentYSize;
  private int currentXSize;
  private int minX = 0;
  private int minY = 0;

  public SquarePacketing(final int xSize, final int ySize) {
    double ratio = xSize / (double) ySize;
    if (!Double.isFinite(ratio)) {
      ratio = STD_RATIO;
    }
    if (logger.isTraceEnabled()) {
      logger.trace("Calculating Packeting with StartingSize: (" + xSize + ", " + ySize
          + ") and ratio " + ratio);
    }
    this.ratio = ratio;
    this.currentXSize = xSize;
    this.currentYSize = ySize;
    this.wrapper = new ContainerWrapper<T>();
    this.wrapper.resiszeArea(minX, minY, currentXSize, currentYSize);
  }

  public void insert(final T element, final int size) {
    insertR(element, size, 0);
  }

  private void insertR(final T element, final int size, int rek) {

    Container<T> avalibleContainer;
    avalibleContainer = findBestFitContainer(size);
    if (avalibleContainer != null) {
      wrapper.insertIntoContainer(element, avalibleContainer, size);
      return;
    }

    avalibleContainer = findBiggestAvalibleContainerOnOutside();
    if (rek > 2) {
      avalibleContainer = null;
    }

    resizePackage(size, avalibleContainer);

    insertR(element, size, ++rek);
    return;
  }

  private void resizePackage(final int fitSize, final Container<T> outsideContainer) {
    final int oldXSize = currentXSize;
    final int oldYSize = currentYSize;

    if (outsideContainer == null) {
      currentXSize += fitSize;
      currentYSize = (int) (currentXSize / ratio);
    } else {
      if (outsideContainer.getXSize() < fitSize) {
        currentXSize += fitSize - outsideContainer.getXSize();
        currentYSize = (int) (currentXSize / ratio);
      }
      final int outsideY = outsideContainer.getYSize() + currentYSize - oldYSize;
      if (outsideY < fitSize) {
        currentYSize += fitSize - outsideY;
        currentXSize = (int) (currentYSize * ratio);
      }

      if (outsideContainer.getXMin() == minX) {
        minX = minX - (currentXSize - oldXSize);
      }
      if (outsideContainer.getYMin() == minY) {
        minY = minY - (currentYSize - oldYSize);
      }

    }
    wrapper.resiszeArea(minX, minY, currentXSize, currentYSize);

  }

  private Container<T> findBiggestAvalibleContainerOnOutside() {
    int currentBestSize = 0;
    Container<T> currentBest = null;
    for (final Container<T> current : wrapper.getEmptyContainers()) {
      if (!wrapper.containerOnOutside(current)) {
        continue; // Container<T> must be on Outside
      }
      final int currentSize = current.getXSize() * current.getYSize();
      if (currentSize > currentBestSize) {
        currentBestSize = currentSize;
        currentBest = current;
      }
    }
    return currentBest;
  }

  private Container<T> findBestFitContainer(final int size) {
    int minSpare = Integer.MAX_VALUE;
    Container<T> currentBest = null;
    for (final Container<T> c : wrapper.getEmptyContainers()) {
      final int cmp = c.spareSpace(size);
      if (cmp == 0) {
        return c;
      } else if (cmp > 0 && minSpare > cmp) {
        minSpare = cmp;
        currentBest = c;
      }
    }
    return currentBest;
  }

  // private Container<T> findFirstFitContainer(final int size) {
  // for (final Container<T> c : wrapper.getEmptyContainers()) {
  // final int cmp = c.spareSpace(size);
  // if (cmp > 0) {
  // return c;
  // }
  // }
  // return null;
  // }

  public Pair<Integer, Integer> getSize() {
    return new Pair<Integer, Integer>(currentXSize, currentYSize);
  }

  public List<Pair<T, Pair<Integer, Integer>>> getAllElementsWithPosition() {
    final List<Pair<T, Pair<Integer, Integer>>> out = new LinkedList<>();
    for (final Container<T> current : wrapper.getFilledContainers()) {
      out.add(new Pair<T, Pair<Integer, Integer>>(current.getElement(), new Pair<Integer, Integer>(
          current.getXMin() - minX, current.getYMin() - minY)));
    }
    return out;
  }

  public List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> getAllEmptyContainers() {
    final List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> out = new LinkedList<>();
    for (final Container<T> current : wrapper.getEmptyContainers()) {
      final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> pair = new Pair<>(new Pair<>(
          current.getXMin() - minX, current.getYMin() - minY), new Pair<>(current.getXSize(),
          current.getYSize()));
      out.add(pair);
    }
    return out;
  }

  public int getFillPercent() {
    final int ret = (int) wrapper.getFillPercent();
    if (ret < 0) {
      logger.warn("FillPercentage is negative, Wrapper X Size = " + wrapper.getXSize()
          + ", Wrapper Y Size = " + wrapper.getYSize());
    }
    return ret;
  }

  public void cutEdges() {
    final Pair<Integer, Integer> maxToCut = wrapper.getSmallestEmptyOnMAXSite();
    if (maxToCut == null) {
      return;
    }

    int cuttingX = 0;

    final int yToCutRelativeToX = (int) (maxToCut.getKey() / ratio);
    final int xToCutRelativeToY = (int) (maxToCut.getValue() * ratio);

    if (maxToCut.getValue() >= yToCutRelativeToX) {
      cuttingX = maxToCut.getKey();
    }
    if (maxToCut.getKey() >= xToCutRelativeToY) {
      cuttingX = xToCutRelativeToY;
    }

    if (cuttingX > 0) {
      resizePackage(-cuttingX, null);
    }

  }
}
