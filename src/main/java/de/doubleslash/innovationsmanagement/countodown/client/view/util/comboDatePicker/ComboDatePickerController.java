package de.doubleslash.innovationsmanagement.countodown.client.view.util.comboDatePicker;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import org.apache.commons.lang3.Validate;

import de.doubleslash.innovationsmanagement.countodown.util.RelativeDate;

public class ComboDatePickerController implements Initializable {

  private final SimpleObjectProperty<LocalDate> currentDate;
  private final HashMap<String, RelativeDate> relativeDateFromName = new HashMap<>();

  public ComboDatePickerController() {
    this.currentDate = new SimpleObjectProperty<LocalDate>(LocalDate.now());
  }

  @FXML
  DatePicker picker;
  @FXML
  ComboBox<String> combobox;

  @FXML
  TextField show;

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    initialize();
  }

  private void initialize() {

    show.setEditable(false);
    picker.setEditable(false);

    combobox.valueProperty().addListener((obs, oldV, newV) -> {
      showCombobox(newV);
    });

    picker.valueProperty().addListener((obs, oldV, newV) -> {
      showDatePicker(newV);
    });

  }

  private void showCombobox(final String newV) {
    final RelativeDate relDate = relativeDateFromName.get(newV);
    final LocalDate newDate = relDate.apply(LocalDate.now());
    currentDate.set(newDate);
    picker.setValue(newDate);
    show.setText(newV);
  }

  private void showDatePicker(final LocalDate newV) {
    if (newV == null || newV.equals(currentDate.get())) {
      return;
    }
    final String dateAsString = newV.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
    show.setText(dateAsString);
    currentDate.set(newV);
    combobox.setValue(null);
  }

  public void addAllItemsToComboBox(final List<DateNamePair> list) {
    for (final DateNamePair pair : list) {
      if (pair == null) {
        continue;
      }
      addItemToComboBox(pair);
    }
  }

  public void addItemToComboBox(final DateNamePair datenamepair) {
    combobox.getItems().add(datenamepair.name);
    relativeDateFromName.put(datenamepair.name, datenamepair.relativeDate);
  }

  public void addChangeListener(final ChangeListener<? super LocalDate> listener) {
    currentDate.addListener(listener);

  }

  public void setValue(final LocalDate date) {
    picker.setValue(date);
  }

  public void setValue(final String relativeDate) {
    if (relativeDateFromName.containsKey(relativeDate)) {
      show.setText(relativeDate);
      showCombobox(relativeDate);
    }

  }

  public static class DateNamePair implements Comparable<DateNamePair> {
    final RelativeDate relativeDate;
    final String name;

    public DateNamePair(final RelativeDate dependingDate, final String name) {
      this.relativeDate = dependingDate;
      this.name = name;
      Validate.isTrue(dependingDate != null);
    }

    public LocalDate getDateRelativeToToday() {
      return relativeDate.apply(LocalDate.now());
    }

    public String getName() {
      return name;
    }

    @Override
    public int compareTo(final DateNamePair o) {
      return relativeDate.compareTo(o.relativeDate);
    }
  }

  @FXML
  private void comboboxAction() {
    showCombobox(combobox.getValue());
  }

  @FXML
  private void datePickerAction() {
    showDatePicker(picker.getValue());
  }

  public LocalDate getDate() {
    return currentDate.get();
  }

  public void clearComboBox() {
    combobox.getItems().clear();
    relativeDateFromName.clear();

  }

}
