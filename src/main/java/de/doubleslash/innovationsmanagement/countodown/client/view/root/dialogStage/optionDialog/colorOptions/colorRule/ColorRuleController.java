package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage.optionDialog.colorOptions.colorRule;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import de.doubleslash.innovationsmanagement.countodown.data.filter.ColorDateOption;
import de.doubleslash.innovationsmanagement.countodown.util.RelativeDate;
import de.doubleslash.innovationsmanagement.countodown.util.RelativeDate.TimeFunction;
import de.doubleslash.innovationsmanagement.countodown.util.RelativeDate.TimeUnit;

public class ColorRuleController implements Observable, Comparable<ColorRuleController>,
    Initializable {

  private final List<InvalidationListener> observer = new LinkedList<>();

  final ColorDateOption option;

  public ColorRuleController(final ColorDateOption option) {
    this.option = option;
  }

  @FXML
  TextField name;
  @FXML
  TextField month;
  @FXML
  TextField week;
  @FXML
  TextField day;

  @FXML
  ColorPicker color;

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    if (option == null) {
      return;
    }
    name.setText(option.getName());
    color.setValue(option.getColor());

    final RelativeDate rel = option.getDate();
    Integer month = rel.getValueFor(TimeUnit.MONTH, TimeFunction.ADD_X_TO);
    Integer week = rel.getValueFor(TimeUnit.WEEK, TimeFunction.ADD_X_TO);
    Integer day = rel.getValueFor(TimeUnit.DAY, TimeFunction.ADD_X_TO);
    if (day == null) {
      day = 0;
    }
    if (month == null) {
      month = 0;
    } else {
      month++;
    }
    if (week == null) {
      week = 0;
    } else {
      week++;
    }

    this.month.setText(month.toString());
    this.week.setText(week.toString());
    this.day.setText(day.toString());
  }

  @FXML
  private void handleRemove() {
    notifyObservers();
  }

  @Override
  public void addListener(final InvalidationListener listener) {
    observer.add(listener);
  }

  @Override
  public void removeListener(final InvalidationListener listener) {
    observer.remove(listener);
  }

  private void notifyObservers() {
    for (final InvalidationListener listener : observer) {
      listener.invalidated(this);
    }
  }

  @FXML
  private void checkIfNumKey(final KeyEvent e) {
    final TextField textField = (TextField) e.getTarget();
    if (textField.getText().length() >= 3) {
      e.consume();
      return;
    }
    if (e.getCharacter().matches("[0-9]")
        || (e.getCharacter().equals("-") && textField.getCaretPosition() == 0 && !textField
            .getText().startsWith("-"))) {
      // accept
    } else {
      e.consume();
    }
  }

  public ColorDateOption getColorDateOption() {

    final int day = getInt(this.day.getText());
    int week = getInt(this.week.getText());
    int month = getInt(this.month.getText());
    RelativeDate dateMonth = null;
    RelativeDate dateWeek = null;
    if (month != 0) {
      if (month > 0) {
        month--;
      }
      dateMonth = new RelativeDate(TimeUnit.MONTH, TimeFunction.ADD_X_TO, month, new RelativeDate(
          TimeUnit.MONTH, TimeFunction.GET_LAST_DAY_OF));
    }
    if (week != 0) {
      if (week > 0) {
        week--;
      }
      dateWeek = new RelativeDate(TimeUnit.WEEK, TimeFunction.ADD_X_TO, week, new RelativeDate(
          TimeUnit.WEEK, TimeFunction.GET_LAST_DAY_OF));
    }
    final RelativeDate dateDay = new RelativeDate(TimeUnit.DAY, TimeFunction.ADD_X_TO, day,
        new RelativeDate(TimeUnit.DAY, TimeFunction.GET_LAST_DAY_OF, RelativeDate.combine(dateWeek,
            dateMonth)));

    return new ColorDateOption(color.getValue(), dateDay, name.getText());
  }

  private int getInt(final String s) {
    if (s == null) {
      return 0;
    }
    final String nm = s.trim();
    if (nm.isEmpty()) {
      return 0;
    }
    try {
      return Integer.decode(nm);
    } catch (final Exception e) {
      return 0;
    }
  }

  @Override
  public int compareTo(final ColorRuleController o) {
    int c = Integer.compare(getInt(month.getText()), getInt(o.month.getText()));
    if (c != 0) {
      return c;
    }
    c = Integer.compare(getInt(week.getText()), getInt(o.week.getText()));
    if (c != 0) {
      return c;
    }
    c = Integer.compare(getInt(day.getText()), getInt(o.day.getText()));
    return c;
  }
}
