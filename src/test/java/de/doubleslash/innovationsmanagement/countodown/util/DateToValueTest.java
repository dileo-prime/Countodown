package de.doubleslash.innovationsmanagement.countodown.util;

import java.time.LocalDate;

import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.Test;

import de.doubleslash.innovationsmanagement.countodown.util.RelativeDate.TimeFunction;
import de.doubleslash.innovationsmanagement.countodown.util.RelativeDate.TimeUnit;

public class DateToValueTest {

   @Test
   public void test() {
      System.out.println(MutablePair.of(LocalDate.now(), TimeUnit.DAY).compareTo(
            MutablePair.of(LocalDate.now(), TimeUnit.MONTH)));
   }

   @Test
   public void testTimeUnit() {
      final RelativeDate date = new RelativeDate(TimeUnit.MONTH, TimeFunction.ADD_X_TO, 1, new RelativeDate(
            TimeUnit.MONTH, TimeFunction.GET_LAST_DAY_OF, new RelativeDate(TimeUnit.WEEK,
                  TimeFunction.GET_FIRST_DAY_OF, new RelativeDate(TimeUnit.DAY, TimeFunction.ADD_X_TO, 2))));
      System.err.println(date.apply(LocalDate.now()));
      for (final TimeUnit unit : TimeUnit.values()) {
         System.out.println(unit);
      }

   }
}
