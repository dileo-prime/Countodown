package de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar;

import java.net.URL;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import javafx.application.Platform;
import javafx.css.StyleableObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.AbstractFilterLoader;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.notepadFilter.NotepadFilterLoader;
import de.doubleslash.innovationsmanagement.countodown.data.filter.TaskFilter;
import de.doubleslash.innovationsmanagement.countodown.util.DialogHelper;
import de.doubleslash.innovationsmanagement.countodown.util.Occurrence;

public class FilterSideBarController implements Initializable {

  private final static Logger logger = LoggerFactory.getLogger(FilterSideBarController.class);

  private final static String KEY_RELOAD_BUTTON_RELOADING = "Reloading";
  private final static String KEY_RELOAD_BUTTON_READY = "Reload_Tasks";
  private String RELOAD_BUTTON_RELOADING;
  private String RELOAD_BUTTON_READY;

  private final AbstractFilterLoader superFilterLoader;

  // used to log the time passed during reloading
  private long currentReloadingTime = 0;

  @FXML
  Button reloadButton;

  @FXML
  VBox box;

  private final List<AbstractFilterLoader> spezifiedLoaders = new LinkedList<>();

  private final AtomicBoolean currentlyReloading = new AtomicBoolean(false);
  private final BiFunction<List<TaskFilter>, LocalDate, Occurrence> reloader;

  public FilterSideBarController(
      final BiFunction<List<TaskFilter>, LocalDate, Occurrence> reloader,
      final EntryPointBackend backend) {
    this.reloader = reloader;
    this.superFilterLoader = new NotepadFilterLoader(backend);
  }

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    RELOAD_BUTTON_READY = resources.getString(KEY_RELOAD_BUTTON_READY);
    RELOAD_BUTTON_RELOADING = resources.getString(KEY_RELOAD_BUTTON_RELOADING);

    initialize();
  }

  private void initialize() {
    setFilter(superFilterLoader);
  }

  @FXML
  private void handleReload() {
    reload();
  }

  public void setAndInitializeFilter(final AbstractFilterLoader spezifiedFilterLoader) {
    spezifiedFilterLoader.initialize(superFilterLoader.getFilterController());
    setFilter(spezifiedFilterLoader);
  }

  public void removeFilter(final AbstractFilterLoader spezifiedFilterLoader) {
    final Parent spezifiedFilter = spezifiedFilterLoader.getView();
    final int idx = box.getChildren().indexOf(spezifiedFilter);
    if (idx < 0) {
      return;
    }
    box.getChildren().remove(idx);
    if (idx != 0) {
      box.getChildren().remove(idx - 1);
    }
    spezifiedLoaders.remove(spezifiedFilterLoader);
    reload();
  }

  private void setFilter(final AbstractFilterLoader spezifiedFilterLoader) {
    spezifiedLoaders.add(spezifiedFilterLoader);
    addFilter(spezifiedFilterLoader.getView());
    handleReload();
  }

  private void addFilter(final Parent spezifiedFilter) {
    if (spezifiedLoaders.size() > 0) {

      final Separator horizontalSeperator = new Separator(Orientation.HORIZONTAL);
      horizontalSeperator.setMinHeight(20);
      box.getChildren().add(horizontalSeperator);
    }
    box.getChildren().add(spezifiedFilter);
  }

  public void reload() {

    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(() -> {
        reload();
      });
      return;
    }

    if (superFilterLoader.getFilterController().getMaxDate()
        .isBefore(superFilterLoader.getFilterController().getMinDate())) {
      final Alert alert = DialogHelper.createWarning();
      alert.setTitle("Warning");
      alert.setHeaderText(null);
      alert.setContentText("Counldn't reload, MaxDate is greater than ViewDate");
      alert.show();
      return;
    }
    if (currentlyReloading.getAndSet(true)) {
      if (logger.isDebugEnabled()) {
        logger.debug("Already Reloading");
      }
      return;
    }

    if (logger.isDebugEnabled()) {
      currentReloadingTime = System.currentTimeMillis();
    }

    startRealoading();
    final List<TaskFilter> filterList = new LinkedList<>();
    for (final AbstractFilterLoader loader : spezifiedLoaders) {
      filterList.add(loader.getFilterController().getTaskFilter());
    }
    final Occurrence finished = reloader.apply(filterList, superFilterLoader.getFilterController()
        .getViewDate());

    new Thread(() -> {
      try {
        finished.await();
      } catch (final InterruptedException e) {
        logger.warn("Interrupted while waiting for Backend during reloading");
      }
      finishReloading();
    }).start();
  }

  private void startRealoading() {
    reloadButton.setDisable(true);
    reloadButton.setText(RELOAD_BUTTON_RELOADING);
    reloadButton.setBackground(null);
  }

  private void finishReloading() {
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(() -> {
        finishReloading();
      });
      return;
    }

    currentlyReloading.set(false);

    final StyleableObjectProperty<Background> bgproperty = (StyleableObjectProperty<Background>) reloadButton
        .backgroundProperty();
    bgproperty.applyStyle(null, null);
    reloadButton.setDisable(false);
    reloadButton.setText(RELOAD_BUTTON_READY);

    if (logger.isDebugEnabled()) {
      currentReloadingTime = System.currentTimeMillis() - currentReloadingTime;
      logger.debug("Finished reloading after: " + currentReloadingTime / 1000 + " seconds and "
          + currentReloadingTime % 1000 + " millis");
    }

  }
}
