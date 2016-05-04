package de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.notepadFilter;

import java.net.URL;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.AbstractFilterController;
import de.doubleslash.innovationsmanagement.countodown.client.view.util.comboDatePicker.ComboDatePickerController;
import de.doubleslash.innovationsmanagement.countodown.client.view.util.comboDatePicker.ComboDatePickerController.DateNamePair;
import de.doubleslash.innovationsmanagement.countodown.client.view.util.comboDatePicker.ComboDatePickerLoader;
import de.doubleslash.innovationsmanagement.countodown.data.filter.ColorDateOption;
import de.doubleslash.innovationsmanagement.countodown.data.filter.LocalFilter;
import de.doubleslash.innovationsmanagement.countodown.data.filter.TaskFilter;
import de.doubleslash.innovationsmanagement.countodown.util.ObserverableValueImplementation;
import de.doubleslash.innovationsmanagement.countodown.util.RelativeDate;

public class NotepadFilterController extends AbstractFilterController implements Initializable {

  private final ComboDatePickerLoader viewDateLoader;
  private final ComboDatePickerLoader maxDateLoader;
  private final ComboDatePickerController viewDateController;
  private final ComboDatePickerController maxDateController;
  private final EntryPointBackend backend;

  public NotepadFilterController(final EntryPointBackend backend) {
    super(null);
    this.viewDateLoader = new ComboDatePickerLoader();
    this.maxDateLoader = new ComboDatePickerLoader();
    this.viewDateController = viewDateLoader.getController();
    this.maxDateController = maxDateLoader.getController();
    this.backend = backend;
  }

  @FXML
  private AnchorPane viewDateContainer;
  @FXML
  private AnchorPane maxDateContainer;

  @FXML
  private CheckBox showOverdue;
  @FXML
  private CheckBox showFinished;

  @Override
  public TaskFilter getTaskFilter() {
    return new LocalFilter(getMinDate(), getMaxDate(), getShowFinished());
  }

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    initialize();
  }
  private void initialize() {
    final ObserverableValueImplementation<List<ColorDateOption>> colorDateOptions = backend
        .getColorDateOptions();
    colorDateOptions.addListener((obs) -> {
      setDateControllerList();
    });
    addChildrenToAnchorPane(viewDateContainer, viewDateLoader.getView());
    addChildrenToAnchorPane(maxDateContainer, maxDateLoader.getView());
    setDateControllerList();
  }

  private void setDateControllerList() {
    final List<ColorDateOption> colorDateOptions = backend.getColorDateOptions().getValue();
    if (colorDateOptions == null) {
      return;
    }

    final List<DateNamePair> list = new LinkedList<>();

    for (final ColorDateOption option : colorDateOptions) {
      if (option.getName() == null || option.getName().isEmpty()) {
        continue;
      }
      list.add(new DateNamePair(option.getDate(), option.getName()));
    }

    viewDateController.clearComboBox();
    maxDateController.clearComboBox();

    viewDateController.addAllItemsToComboBox(list);
    maxDateController.addAllItemsToComboBox(list);

    final LocalDate today = LocalDate.now();

    if (list.isEmpty()) {
      viewDateController.setValue(today);
      maxDateController.setValue(today);
      return;
    }

    DateNamePair current = null;
    for (final DateNamePair pair : list) {
      if (pair.getDateRelativeToToday().equals(today)) {
        if (current == null || pair.compareTo(current) < 0) {
          current = pair;
        }
      }
    }
    if (current == null) {
      current = list.get(0);
    }
    viewDateController.setValue(current.getName());
    maxDateController.setValue(list.get(list.size() - 1).getName());
  }

  private void addChildrenToAnchorPane(final AnchorPane pane, final Node child) {
    pane.getChildren().add(child);
    AnchorPane.setLeftAnchor(child, 0.0);
    AnchorPane.setRightAnchor(child, 0.0);
  }

  public LocalDate getCurrentDate() {
    return viewDateController.getDate();
  }

  @Override
  public LocalDate getMinDate() {
    if (showOverdue.isSelected()) {
      return RelativeDate.Min.apply(null);
    } else {
      return getViewDate();
    }
  }

  @Override
  public LocalDate getViewDate() {
    return viewDateController.getDate();
  }

  @Override
  public LocalDate getMaxDate() {
    return maxDateController.getDate();
  }

  @Override
  public boolean getShowFinished() {
    return showFinished.isSelected();
  }
}