package de.doubleslash.innovationsmanagement.countodown.data.filter;

public class JiraOption {

   String name;
   String adress;
   String filter;

   public String getName() {
      return name;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public String getAdress() {
      return adress;
   }

   public void setAdress(final String adress) {
      this.adress = adress;
   }

   public String getPresetFilter() {
      return filter;
   }

   public void setPresetFilter(final String presetFilter) {
      this.filter = presetFilter;
   }

   public JiraOption(final String name, final String adress, final String presetFilter) {
      this.name = name;
      this.adress = adress;
      this.filter = presetFilter;
   }

}
