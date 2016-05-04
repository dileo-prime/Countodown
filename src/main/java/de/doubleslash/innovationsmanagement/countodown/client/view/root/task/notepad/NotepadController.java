package de.doubleslash.innovationsmanagement.countodown.client.view.root.task.notepad;

import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.CacheHint;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import org.apache.commons.lang3.Validate;

import de.doubleslash.innovationsmanagement.countodown.client.view.root.task.notepad.label.LabelLoader;
import de.doubleslash.innovationsmanagement.countodown.data.Task;
import de.doubleslash.innovationsmanagement.countodown.util.DateToValue;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;

public class NotepadController implements Initializable, Observable {

  private final static ColorAdjust MONOCHROME = new ColorAdjust(0.0, -1.0, 0.0, 0.0);
  private final static int MIN_SIZE_BIG_LABEL = 175;
  private final static int MIN_SIZE_SMALL_LABEL = 75;
  private final static long MAX_MINS_TO_WORK_DAYLI = 480L;
  private final static long SIZEFACTOR = 150L; // gets added to the size to increase the
                                               // standardsize

  @FXML
  StackPane rootPane;

  private final SimpleStringProperty title;
  private final SimpleStringProperty summary;
  private final SimpleStringProperty dueDate;
  private final ObserverableValueImplementation<Color> textColor;
  private final DateToValue<Color> colorPicker;
  private final Consumer<Task> showTaskDialog;
  private final List<InvalidationListener> observers;

  private double outerScale = 1.0;

  private LabelLoader currentLabel = null;
  private LabelLoader bigLabel = null;
  private LabelLoader smallLabel = null;

  @FXML
  ImageView notepad;
  ImageView notepadClip;

  final Task task;

  private double currentScaledSize = 0;
  private double currentSize = 0;
  private Color currentColor = null;
  private double imageSize;
  private double currentScaleOffset = 0;

  private double preScaleXPos = 0;
  private double preScaleYPos = 0;

  private double mouseXPos;
  private double mouseYPos;

  public NotepadController(final Task task, final DateToValue<Color> currentDate,
      final Consumer<Task> showTaskDialog) {
    Validate.notNull(task);
    this.task = task;
    this.colorPicker = currentDate;
    this.showTaskDialog = showTaskDialog;

    this.title = new SimpleStringProperty();
    this.summary = new SimpleStringProperty();
    this.dueDate = new SimpleStringProperty();
    this.textColor = new ObserverableValueImplementation<>();
    this.observers = new LinkedList<>();

  }

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    initialize();
  }
  private void initialize() {
    final Image image = notepad.getImage();
    imageSize = image.getHeight();
    notepadClip = new ImageView(image);
    notepadClip.setPreserveRatio(true);
    notepadClip.setCache(true);
    notepadClip.setCacheHint(CacheHint.SPEED);
    notepad.setClip(notepadClip);

    reloadFromTask();

  }

  @FXML
  protected void openEditTaskDialog(final MouseEvent e) {
    if (e.getClickCount() == 2) {
      showTaskDialog.accept(task);
    }
  }

  private void adjustLabel() {
    final double size = currentScaledSize;
    final LabelLoader oldLabel = currentLabel;
    if (size < MIN_SIZE_SMALL_LABEL) { // -> no label
      currentLabel = null;
    } else if (size < MIN_SIZE_BIG_LABEL) { // small label
      if (smallLabel == null) {
        smallLabel = new LabelLoader("Small", title, summary, dueDate, textColor);
      }
      currentLabel = smallLabel;
    } else { // big label
      if (bigLabel == null) {
        bigLabel = new LabelLoader("Big", title, summary, dueDate, textColor);
      }
      currentLabel = bigLabel;
    }

    if (oldLabel == currentLabel) { // no changeouterScale
      return;
    } else if (oldLabel == null) {
      rootPane.getChildren().add(currentLabel.getView());
    } else if (currentLabel == null) {
      rootPane.getChildren().remove(rootPane.getChildren().size() - 1);
    } else {
      rootPane.getChildren().set(rootPane.getChildren().size() - 1, currentLabel.getView());
    }

  }

  public void setScale(final double scale) {
    this.outerScale = scale;
    adjustScale();
  }

  private void reloadFromTask() {

    title.set(task.getTitle());
    summary.set(task.getSummary());
    dueDate.set(task.getDueDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));

    adjustColor();
    adjustSize();
    // adjustLabel(); will be called in adjustSize
  }

  private void adjustColor() {
    final LocalDate dueDate = task.getDueDate().minus(
        Period.ofDays((int) (task.getWorkToDo() / Task.MINUTES_PER_HOUR / Task.HOURS_PER_DAY)));

    currentColor = colorPicker.getValue(dueDate);

    final ColorInput colorInp = new ColorInput(0, 0, imageSize, imageSize, currentColor);
    final Blend colorNoSaturation = new Blend(BlendMode.MULTIPLY, MONOCHROME, colorInp); // remove
    // saturation
    notepad.setEffect(colorNoSaturation);

    textColor.set(calculateTextColor(currentColor)); // setTextFill
  }

  private void adjustSize() {
    long workToDo = task.getWorkToDo();
    if (workToDo > MAX_MINS_TO_WORK_DAYLI) {
      workToDo = MAX_MINS_TO_WORK_DAYLI;
    }  
    
    currentSize = workToDo + SIZEFACTOR;

    adjustScale();
  }

  private void adjustScale() {
    // will be set to Quality at the end of updatePosition
    rootPane.setCacheHint(CacheHint.SPEED);
    currentScaledSize = currentSize * outerScale;
  
    final double scale = currentScaledSize / imageSize;

    rootPane.setScaleX(scale);
    rootPane.setScaleY(scale);
    currentScaleOffset = (currentScaledSize - imageSize) / 2;
 
    updatePosition();
    adjustLabel();

  }

  private void updatePosition() {
    rootPane.setCacheHint(CacheHint.SPEED);
    final double postScaleXPos = preScaleXPos * outerScale;
    final double postScaleYPos = preScaleYPos * outerScale;

    rootPane.setLayoutX(postScaleXPos + currentScaleOffset);
    rootPane.setLayoutY(postScaleYPos + currentScaleOffset);
    rootPane.setCacheHint(CacheHint.QUALITY);
  }

  public void setPostScalePosition(final double x, final double y) {
    setPreScalePosition(x / outerScale, y / outerScale);
  }

  public void setPreScalePosition(final double x, final double y) {
    this.preScaleXPos = x;
    this.preScaleYPos = y;
    updatePosition();
  }

  private static Color calculateTextColor(final Color color) {

    if (color == null) {
      return Color.BLACK;
    }

    final double rgb = (color.getRed() + color.getBlue() + color.getGreen());
    if (rgb > 1.5) {
      return Color.BLACK;
    } else {
      return Color.WHITE;
    }
  }

  public double getCurrentSize() {
    return currentSize;
  }

  public double getScaledSize() {
    return currentScaledSize;
  }

  @FXML
  protected void handleDragging(final MouseEvent e) {
    final double xPos = mouseXPos + e.getSceneX();
    final double yPos = mouseYPos + e.getSceneY();

    double postScaleXPos = xPos - currentScaleOffset;
    double postScaleYPos = yPos - currentScaleOffset;

    if (rootPane.getParent() != null) {
      final Bounds b = rootPane.getParent().getBoundsInParent();
      if (postScaleXPos + currentScaledSize > b.getMaxX() + 1.0) {
        postScaleXPos = b.getMaxX() - currentScaledSize;
      } else if (postScaleXPos < b.getMinX() - 1.0) {
        postScaleXPos = b.getMinX();
      }
      if (postScaleYPos + currentScaledSize > b.getMaxY() + 1.0) {
        postScaleYPos = b.getMaxY() - currentScaledSize;
      } else if (postScaleYPos < b.getMinY() - 1.0) {
        postScaleYPos = b.getMinY();
      }
    }

    setPostScalePosition(postScaleXPos, postScaleYPos);

  }

  @FXML
  protected void handleMousePressed(final MouseEvent e) {
    invalidate();
    mouseXPos = rootPane.getLayoutX() - e.getSceneX();
    mouseYPos = rootPane.getLayoutY() - e.getSceneY();
  }

  private void invalidate() {
    for (final InvalidationListener listener : observers) {
      listener.invalidated(this);
    }
  }

  @Override
  public void addListener(final InvalidationListener listener) {
    observers.add(listener);
  }

  @Override
  public void removeListener(final InvalidationListener listener) {
    observers.remove(listener);
  }
}
