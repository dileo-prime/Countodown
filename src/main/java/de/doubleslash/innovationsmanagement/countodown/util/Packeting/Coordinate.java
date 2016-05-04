package de.doubleslash.innovationsmanagement.countodown.util.Packeting;

enum Coordinate {
  XMIN("XMIN"),
  XMAX("XMAX"),
  YMIN("YMIN"),
  YMAX("YMAX");

  String name;

  Coordinate(final String name) {
    this.name = name;
  }
}