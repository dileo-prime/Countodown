package de.doubleslash.innovationsmanagement.countodown.data.filter;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraQueryBuilder extends TaskFilter {

   private final static Logger logger = LoggerFactory.getLogger(JiraQueryBuilder.class);

   private final static String DUE = "Due";
   private final static String AND = " AND ";
   private final static String FINISHED = "(6,5)";

   private final List<Criteria> fields = new LinkedList<>();
   private final String username;
   private final String jiraAdress;

   public JiraQueryBuilder(final LocalDate dueDateFrom, final LocalDate dueDateTo, final boolean showFinished,
         final String username, final String jiraAdress) {
      super(dueDateFrom, dueDateTo, showFinished);
      this.username = username;
      this.jiraAdress = jiraAdress;

   }

   public enum Field {
      Affected_Version("Affected Version"),
      Assignee("Assignee"),
      Category("Category"),
      Comment("Comment"),
      Component("Component"),
      Created("Created"),
      Custom_Field("Custom Field"),
      Description("Description"),

      Environment("Environment"),
      Epic_Link("Epic Link"),
      Filter("Filter"),
      Fix_Version("Fix Version"),
      Issue_Key("Issue Key"),
      LastViewed("LastViewed"),
      Level("Level"),
      Original_Estimate("Original Estimate"),
      Parent("Parent"),
      Priority("Priority"),
      Project("Project"),
      Remaining_Estimate("Remaining Estimate"),
      Reporter("Reporter"),
      Resolution("Resolution"),
      Resolved("Resolved"),
      Sprint("Sprint"),
      Status("Status"),
      Summary("Summary"),
      Text("Text"),
      Type("Type"),
      Time_Spent("Time Spent"),
      Updated("Updated"),
      Voter("Voter"),
      Votes("Votes"),
      Watcher("Watcher"),
      Work_Ratio("Work Ratio");

      private final String representingString;

      private Field(final String representingString) {
         this.representingString = representingString;
      }

      @Override
      public String toString() {
         return representingString;
      }

      String getRepresentingString() {
         return representingString.replace(" ", "");
      }
   }

   public enum Operator {
      EQUALS("="),
      NOT_EQUALS("!="),
      GREATER_THAN(">"),
      GREATER_THAN_EQUALS(">="),
      LESS_THAN("<"),
      LESS_THAN_EQUALS("<="),
      IN(" IN", true),
      NOT_IN(" NOT IN", true),
      CONTAINS("~"),
      DOES_NOT_CONTAIN("!~");

      private final String representingString;
      private final boolean multiple;

      private Operator(final String representingString) {
         this(representingString, false);
      }

      private Operator(final String representingString, final boolean multiple) {
         this.representingString = representingString;
         this.multiple = multiple;
      }

      @Override
      public String toString() {
         return representingString;
      }
   }

   public JiraQueryBuilder addCriteria(final Field field, final Operator op, final CharSequence... parameter) {

      this.fields.add(new Criteria(field, op, parameter));
      return this;
   }

   private class Criteria {
      final Field field;
      final Operator operator;
      final CharSequence[] parameter;

      public Criteria(final Field field, final Operator operator, final CharSequence[] parameter) {
         this.field = field;
         this.operator = operator;
         this.parameter = parameter;
      }

      @Override
      public String toString() {
         final StringBuilder out = new StringBuilder();
         out.append(field.getRepresentingString());
         out.append(operator);
         if (operator.multiple) {
            out.append("(");
            for (final CharSequence para : parameter) {
               out.append("\"").append(para).append("\",");
            }
            out.setCharAt(out.length() - 1, ')');
         } else {
            out.append("\"").append(parameter[0]).append("\"");
         }
         return out.toString();
      }
   }

   public boolean hasEntries() {
      return !fields.isEmpty();
   }

   @Override
   public String toString() {
      final StringBuilder out = new StringBuilder();
      for (final Criteria clause : fields) {
         out.append(clause);
         out.append(AND);
      }
      out.append(DUE).append(Operator.GREATER_THAN_EQUALS).append(dueDateFrom);
      out.append(AND);
      out.append(DUE).append(Operator.LESS_THAN_EQUALS).append(dueDateTo);
      if (!showFinished) {
         out.append(AND);
         out.append(Field.Status).append(Operator.NOT_IN).append(FINISHED);
      }
      if (logger.isInfoEnabled()) {
         logger.info("Jira Filter is: " + out);
      }
      return out.toString();
   }

   public String getJiraAdress() {
      return jiraAdress;
   }

   public String getUserName() {
      return username;
   }
}