package de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.jiraFilter;

import de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.AbstractFilterController;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.filterSideBar.filter.AbstractFilterLoader;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraOption;

public class JiraFilterLoader extends AbstractFilterLoader {

   private final String username;
   private final JiraOption nameAdress;

   public JiraFilterLoader(final String username, final JiraOption nameAdress) {
      this.username = username;
      this.nameAdress = nameAdress;
   }

   @Override
   protected AbstractFilterController getAbstractFilterControllerInstance() {

      return new JiraFilterController(username, nameAdress, superFilter);
   }

   public String getJiraName() {
      return nameAdress.getName();
   }

}
