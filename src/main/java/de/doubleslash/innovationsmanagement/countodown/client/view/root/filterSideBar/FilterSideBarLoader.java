package de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiFunction;

import javafx.fxml.Initializable;
import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;
import de.doubleslash.innovationsmanagement.countodown.data.filter.TaskFilter;
import de.doubleslash.innovationsmanagement.countodown.util.Occurrence;

public class FilterSideBarLoader extends MVCLoader {

  BiFunction<List<TaskFilter>, LocalDate, Occurrence> reloader;
  EntryPointBackend backend;

  public FilterSideBarLoader(final EntryPointBackend backend,
      final BiFunction<List<TaskFilter>, LocalDate, Occurrence> reloader) {
    this.reloader = reloader;
    this.backend = backend;
  }

  @Override
  protected Initializable controllerInstance() {
    return new FilterSideBarController(reloader, backend);
  }

}
