package de.doubleslash.innovationsmanagement.countodown.util.Packeting;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class ContainerTest {

  @Test
  public void test() {
    final Container<Object> c1 = new Container<>(1, 1, 1, 1);

    final Set<Container<Object>> set1 = new HashSet<Container<Object>>();

    set1.add(c1);
    set1.add(c1);
    System.out.println(c1);
    Assert.assertTrue(set1.remove(c1));
    Assert.assertTrue(set1.isEmpty());

  }
}
