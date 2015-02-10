package de.doubleslash.innovationsmanagement.countodown.util.Packeting;

import org.junit.Test;

public class ContainerWrapperTest {

  ContainerWrapper<Object> wrapper = new ContainerWrapper<Object>();

  @Test
  public void test() {
    wrapper.resiszeArea(0, 0, 100, 100);
    wrapper.insertIntoContainer(new Object(), wrapper.getEmptyContainers().get(0), 10);
    wrapper.insertIntoContainer(new Object(), wrapper.getEmptyContainers().get(0), 10);
    wrapper.insertIntoContainer(new Object(), wrapper.getEmptyContainers().get(0), 10);
    wrapper.insertIntoContainer(new Object(), wrapper.getEmptyContainers().get(0), 10);

  }

}
