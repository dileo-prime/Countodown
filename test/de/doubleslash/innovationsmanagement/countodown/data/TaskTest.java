package de.doubleslash.innovationsmanagement.countodown.data;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import org.boon.core.reflection.fields.FieldAccess;
import org.boon.json.serializers.impl.JsonSimpleSerializerImpl;
import org.junit.Test;

public class TaskTest {

  @Test
  public void testObserver() {

    final BooleanProperty executed = new SimpleBooleanProperty(false);

    final Task t = new Task("Jira", "title", "summary");
    t.addListener((o) -> {
      assertTrue(o == t);
      executed.set(true);
    });

    t.setSummary("test");

    assertTrue(executed.get());

  }

  @Test
  public void getAllFields() {

    final JsonSimpleSerializerImpl serializer = new JsonSimpleSerializerImpl();

    Class<?> clazz = ReadWriteLocked.class;
    final Map<String, FieldAccess> map = serializer.getFields(clazz);
    System.out.println(map.entrySet());
    while (clazz != null) {
      final Field[] fields = clazz.getDeclaredFields();
      for (final Field f : fields) {
        System.out.println(f.getName());
      }
      clazz = clazz.getSuperclass();
    }
  }
}
