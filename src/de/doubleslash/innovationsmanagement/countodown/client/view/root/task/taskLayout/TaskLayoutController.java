package de.doubleslash.innovationsmanagement.countodown.client.view.root.task.taskLayout;

import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.task.notepad.NotepadController;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.task.notepad.NotepadLoader;
import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.data.filter.ColorDateOption;
import de.doubleslash.innovationsmanagement.countodown.util.DateToValue;
import de.doubleslash.innovationsmanagement.countodown.util.Occurrence;
import de.doubleslash.innovationsmanagement.countodown.util.RelativeDate;
import de.doubleslash.innovationsmanagement.countodown.util.Packeting.SquarePacketing;

public class TaskLayoutController implements Initializable {

  private final static Logger logger = LoggerFactory.getLogger(TaskLayoutController.class);

  private final EntryPointBackend backend;

  private final static int MIN_FILL_PERCENTAGE = 70;

  private volatile List<NotepadLoader> taskList = null;

  @FXML
  Pane parent;

  double calculatedWidth;
  double calculatedHight;

  private final SimpleDoubleProperty scale;
  private final Consumer<Task> showTaskDialog;

  public TaskLayoutController(final Consumer<Task> showTaskDialog, final EntryPointBackend backend) {
    this.showTaskDialog = showTaskDialog;
    scale = new SimpleDoubleProperty(1);
    this.backend = backend;
  }

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {

    initialize();
  }

  private void initialize() {
    calculatedHight = parent.getPrefHeight();
    calculatedWidth = parent.getPrefWidth();
    parent.heightProperty().addListener((a, b, c) -> {
      setScale();
    });
    parent.widthProperty().addListener((a, b, c) -> {
      setScale();
    });
  }

  public void setAllFromQueue(final BlockingQueue<Task> bq, final LocalDate currentDate,
      final Occurrence finished) {
    final Task endQ;
    try {
      endQ = bq.take();
    } catch (final InterruptedException ignore) {
      logger.warn("interrupted");
      finished.signal();
      return;
    }

    new Thread(() -> {
      long currentTime = 0;
      if (logger.isTraceEnabled()) {
        currentTime = System.currentTimeMillis();
      }
      final List<NotepadLoader> list = getAllFromQ(bq, endQ, currentDate);
      showAllTasks(list, finished);
      taskList = list;
      if (logger.isTraceEnabled()) {
        currentTime = System.currentTimeMillis() - currentTime;
        logger.trace("finished building Layout packeting after: " + currentTime / 1000
            + " seconds and " + currentTime % 1000 + " millis");
      }
    }).start();
  }

  private void showAllTasks(final List<NotepadLoader> list, final Occurrence finished) {
    long currentTime = 0;
    if (logger.isTraceEnabled()) {
      currentTime = System.currentTimeMillis();
    }
    SquarePacketing<NotepadLoader> layout = calculateLayout(list, 0, 0);
    layout = getBetterLayout(layout, list);
    Collections.reverse(list);
    layout = getBetterLayout(layout, list);

    final SquarePacketing<NotepadLoader> layoutFinal = layout;
    Platform.runLater(() -> {
      showLayout(layoutFinal);
      finished.signal();
    });
    if (logger.isTraceEnabled()) {
      currentTime = System.currentTimeMillis() - currentTime;
      logger.trace("finished calculating packeting after: " + currentTime / 1000 + " seconds and "
          + currentTime % 1000 + " millis");
    }
  }

  private SquarePacketing<NotepadLoader> getBetterLayout(
      final SquarePacketing<NotepadLoader> oldLayout, final List<NotepadLoader> list) {
    if (oldLayout.getFillPercent() > MIN_FILL_PERCENTAGE) {
      return oldLayout;
    }
    final Pair<Integer, Integer> size = oldLayout.getSize();
    final SquarePacketing<NotepadLoader> newLayout = calculateLayout(list, size.getKey(),
        size.getValue());
    if (newLayout.getFillPercent() > oldLayout.getFillPercent()) {
      return newLayout;
    } else {
      return oldLayout;
    }

  }

  private SquarePacketing<NotepadLoader> calculateLayout(final List<NotepadLoader> list,
      final int xSize, final int ySize) {
    final SquarePacketing<NotepadLoader> layout = new SquarePacketing<>(xSize, ySize);

    for (final NotepadLoader notepad : list) {
      final NotepadController notepadController = notepad.getController();
      layout.insert(notepad, (int) notepadController.getCurrentSize());
    }

    layout.cutEdges();
    return layout;
  }

  private List<NotepadLoader> getAllFromQ(final BlockingQueue<Task> bq, final Task endQ,
      final LocalDate currentDate) {

    final DateToValue<Color> colorPicker = new DateToValue<>(currentDate);
    final List<Pair<RelativeDate, Color>> colorList = new LinkedList<>();
    final List<ColorDateOption> colorOptionsList = backend.getColorDateOptions().getValue();

    if (colorOptionsList != null) {
      for (final ColorDateOption option : colorOptionsList) {
        colorList.add(new Pair<RelativeDate, Color>(option.getDate(), option.getColor()));
      }
      colorPicker.buildMapFrom(colorList);
    }

    final List<NotepadLoader> list = new LinkedList<>();
    boolean alive = true;
    while (alive) {
      NotepadLoader notepad;
      try {
        final Task current = bq.take();
        if (current == endQ) {
          alive = false;
          continue;
        }
        notepad = new NotepadLoader(current, colorPicker, showTaskDialog);
      } catch (final InterruptedException e) {
        logger.warn("interrupted");
        alive = false;
        continue;
      }
      list.add(notepad);
    }
    for (final NotepadLoader loader : list) {
      final NotepadController controller = loader.getController();
      controller.addListener(new MovedListener(loader.getView()));
    }

    return list;

  }

  public void showLayout(final SquarePacketing<NotepadLoader> layout) {
    long currentTime = 0;
    if (logger.isTraceEnabled()) {
      currentTime = System.currentTimeMillis();
    }

    parent.getChildren().clear();
    final Pair<Integer, Integer> size = layout.getSize();
    setScale(size.getKey(), size.getValue());

    for (final Pair<NotepadLoader, Pair<Integer, Integer>> loaderLayoutPair : layout
        .getAllElementsWithPosition()) {

      parent.getChildren().add(loaderLayoutPair.getKey().getView());

      final NotepadController controller = loaderLayoutPair.getKey().getController();
      controller.setPreScalePosition(loaderLayoutPair.getValue().getKey(), loaderLayoutPair
          .getValue().getValue());
      controller.setScale(scale.get());

      // final Rectangle r = new Rectangle(controller.getScaledSize(), controller.getScaledSize());
      // r.setFill(Color.TRANSPARENT);
      // r.setStroke(Color.BLACK);
      //
      // r.setLayoutX(loaderLayoutPair.getValue().getKey() * scale.get());
      // r.setLayoutY(loaderLayoutPair.getValue().getValue() * scale.get());
      // r.setMouseTransparent(true);
      // parent.getChildren().add(r);

    }

    // for (final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> empty : layout
    // .getAllEmptyContainers()) {
    //
    // final Rectangle r = new Rectangle(empty.getValue().getKey() * scale.get(), empty.getValue()
    // .getValue() * scale.get());
    // r.setFill(Color.TRANSPARENT);
    // r.setStroke(Color.RED);
    //
    // r.setLayoutX(empty.getKey().getKey() * scale.get());
    // r.setLayoutY(empty.getKey().getValue() * scale.get());
    // parent.getChildren().add(r);
    // }

    if (logger.isInfoEnabled()) {
      logger.info("Finnished Displaying Tasks");
      logger.info("Packeting Fill: " + layout.getFillPercent() + "%");
    }
    if (logger.isTraceEnabled()) {
      currentTime = System.currentTimeMillis() - currentTime;
      logger.trace("Displaying tasks took: " + currentTime / 1000 + " seconds and " + currentTime
          % 1000 + " millis");
    }

  }

  private void setScale(final double width, final double height) {

    this.calculatedHight = height;
    this.calculatedWidth = width;
    setScale();
  }

  private void setScale() {

    final double xScale = parent.getWidth() / calculatedWidth;
    final double yScale = parent.getHeight() / calculatedHight;
    if (xScale > yScale) {
      scale.set(yScale);
    } else {
      scale.set(xScale);
    }
    if (taskList == null) {
      return;
    }
    for (final NotepadLoader loader : taskList) {
      final NotepadController controller = loader.getController();
      controller.setScale(scale.get());
    }
    parent.setClip(new Rectangle(parent.getWidth(), parent.getHeight()));
  }

  private class MovedListener implements InvalidationListener {

    private final Node view;

    public MovedListener(final Node view) {
      this.view = view;
    }

    @Override
    public void invalidated(final Observable observable) {
      final ObservableList<Node> ch = parent.getChildren();
      // Set on first place
      if (!Platform.isFxApplicationThread()) {
        Platform.runLater(() -> {
          invalidated(observable);
        });
        return;
      }
      ch.remove(view);
      ch.add(view);
    }
  }
}
