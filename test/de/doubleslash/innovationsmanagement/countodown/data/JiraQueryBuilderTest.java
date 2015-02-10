package de.doubleslash.innovationsmanagement.countodown.data;

import java.time.LocalDate;

import org.junit.Test;

import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraQueryBuilder;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraQueryBuilder.Field;
import de.doubleslash.innovationsmanagement.countodown.data.filter.JiraQueryBuilder.Operator;

public class JiraQueryBuilderTest {

  @Test
  public void simpleTest() {
    System.out.println(Long.parseLong(""));
  }

  @Test
  public void test() {
    final boolean showFinished = false;
    final JiraQueryBuilder query = new JiraQueryBuilder(LocalDate.now(), LocalDate.now(),
        showFinished, null, null);
    query.addCriteria(Field.Reporter, Operator.IN, "ich", "Stefan petry");
    query.addCriteria(Field.Project, Operator.IN, "Promt");
    System.out.println(query);
  }

  @Test
  public void allValues() {

    final JiraQueryBuilder.Field[] possibleValues = JiraQueryBuilder.Field.values();

    for (final Field f : possibleValues) {
      System.out.println(f);
    }
  }
}
