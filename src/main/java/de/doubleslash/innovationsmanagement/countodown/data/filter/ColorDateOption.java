package de.doubleslash.innovationsmanagement.countodown.data.filter;

import javafx.scene.paint.Color;
import de.doubleslash.innovationsmanagement.countodown.util.RelativeDate;

public class ColorDateOption {

  private final Color color;
  private final RelativeDate date;
  private final String name;

  public ColorDateOption(final Color color, final RelativeDate date, final String name) {
    this.color = color;
    this.date = date;

    if (name == null) {
      this.name = "";
    } else {
      this.name = name;
    }
  }

  public Color getColor() {
    return color;
  }

  public RelativeDate getDate() {
    return date;
  }

  public String getName() {
    return name;
  }

}
