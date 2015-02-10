package de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.notepadFilter;

import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.AbstractFilterController;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.AbstractFilterLoader;

public class NotepadFilterLoader extends AbstractFilterLoader {

  private final EntryPointBackend backend;

  public NotepadFilterLoader(final EntryPointBackend backend) {
    this.backend = backend;
  }

  @Override
  protected AbstractFilterController getAbstractFilterControllerInstance() {
    return new NotepadFilterController(backend);
  }

}
