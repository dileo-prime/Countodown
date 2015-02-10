package de.doubleslash.innovationsmanagement.countodown.util;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.function.Function;

public class RelativeDate implements Function<LocalDate, LocalDate>, Comparable<RelativeDate> {

  final static LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);
  final static LocalDate MIN_DATE = LocalDate.of(0001, 1, 1);

  public final static RelativeDate Max = new RelativeDate() {
    @Override
    public LocalDate apply(final LocalDate ignore) {
      return MAX_DATE;
    };
  };
  public final static RelativeDate Min = new RelativeDate() {
    @Override
    public LocalDate apply(final LocalDate ignore) {
      return MIN_DATE;
    };
  };

  private final InnerDate inner;
  private final RelativeDate next;

  private RelativeDate() {
    this(null, null, null, null);
  }

  public RelativeDate(final TimeUnit unit, final TimeFunction function, final Integer value) {
    this(unit, function, value, null);
  }

  public RelativeDate(final TimeUnit unit, final TimeFunction function) {
    this(unit, function, null, null);
  }

  public RelativeDate(final TimeUnit unit, final TimeFunction function, final RelativeDate next) {
    this(unit, function, null, next);
  }

  public RelativeDate(final TimeUnit unit, final TimeFunction function, final Integer value,
      final RelativeDate next) {
    this(new InnerDate(unit, function, value), next);
  }

  private RelativeDate(final InnerDate inner, final RelativeDate next) {
    this.inner = inner;
    this.next = next;

  }

  @Override
  public LocalDate apply(final LocalDate date) {
    final LocalDate returndate = inner.apply(date);
    if (next == null) {
      return returndate;
    }
    return next.apply(returndate);
  }

  TimeUnit getBiggestTimeUnit() {
    if (next == null) {
      return inner.unit;
    } else {
      final TimeUnit other = next.getBiggestTimeUnit();
      return (inner.unit.compareTo(other) > 0 ? inner.unit : other);
    }
  }

  /**
   * @return null if nothing found
   */
  public Integer getValueFor(final TimeUnit unit, final TimeFunction fnc) {
    boolean appeared = false;
    int ret = 0;
    RelativeDate parent = this;
    while (parent != null) {
      if (parent.inner.function.equals(fnc) && parent.inner.unit.equals(unit)) {
        appeared = true;
        ret += parent.inner.value;
      }
      parent = parent.next;
    }
    if (!appeared) {
      return null;
    }
    return ret;

  }

  @Override
  public int compareTo(final RelativeDate o) {
    int ret = 0;
    final TimeUnit[] values = TimeUnit.values();
    for (int i = values.length - 1; i >= 0; i--) {
      Integer self = getValueFor(values[i], TimeFunction.ADD_X_TO);
      Integer other = o.getValueFor(values[i], TimeFunction.ADD_X_TO);
      if (self == null) {
        self = 0;
      }
      if (other == null) {
        other = 0;
      }
      ret = self.compareTo(other);

      if (ret != 0) {
        return ret;
      }
    }

    return ret;
  }

  public static RelativeDate combine(final RelativeDate first, final RelativeDate second) {
    if (first == null) {
      return second;
    }
    if (second == null) {
      return first;
    }
    RelativeDate out = second;
    RelativeDate parent = first;
    while (parent != null) {
      out = new RelativeDate(parent.inner, out);
      parent = parent.next;
    }
    return out;
  }

  private static class InnerDate {
    private final TimeUnit unit;
    private final TimeFunction function;
    private final Integer value;

    private InnerDate(final TimeUnit unit, final TimeFunction function, final Integer value) {
      this.unit = unit;
      this.function = function;
      this.value = value;
    }

    public LocalDate apply(final LocalDate date) {
      return function.apply(unit, date, value);
    }
  }

  @FunctionalInterface
  private interface TriFunction<R, A, B, C> {
    R apply(A a, B b, C c);
  }

  public enum TimeFunction implements TriFunction<LocalDate, TimeUnit, LocalDate, Integer> {
    GET_FIRST_DAY_OF((unit, date, value) -> {
      return unit.getFirstDay(date);
    }),
    GET_LAST_DAY_OF((unit, date, value) -> {
      return unit.getLastDayOf(date);
    }),
    ADD_X_TO((unit, date, value) -> {
      return unit.addXTo(date, value);
    }),
    SUBSTRACT_X_FROM((unit, date, value) -> {
      return unit.substractXFrom(date, value);
    });

    private final TriFunction<LocalDate, TimeUnit, LocalDate, Integer> function;

    private TimeFunction(final TriFunction<LocalDate, TimeUnit, LocalDate, Integer> function) {
      this.function = function;
    }

    @Override
    public LocalDate apply(final TimeUnit unit, final LocalDate date, final Integer value) {
      return function.apply(unit, date, value);
    }
  }

  public enum TimeUnit {
    DAY(null, ChronoUnit.DAYS),
    WEEK(ChronoField.DAY_OF_WEEK, ChronoUnit.WEEKS),
    MONTH(ChronoField.DAY_OF_MONTH, ChronoUnit.MONTHS),
    YEAR(ChronoField.DAY_OF_YEAR, ChronoUnit.YEARS);

    private final TemporalField correnspondingField;
    private final TemporalUnit correndpsondingUnit;

    private TimeUnit(final TemporalField correnspondingField, final TemporalUnit correndpsondingUnit) {
      this.correnspondingField = correnspondingField;
      this.correndpsondingUnit = correndpsondingUnit;
    }

    public LocalDate getFirstDay(final LocalDate current) {
      return getDayX(current, 1);
    }

    public LocalDate getLastDayOf(final LocalDate date) {
      LocalDate current = getFirstDay(date);
      current = addXTo(current, 1);
      return DAY.substractXFrom(current, 1);
    }

    public int getValueof(final LocalDate current) {
      return current.get(correnspondingField);
    }

    private LocalDate getDayX(final LocalDate current, final int day) {
      if (correnspondingField == null) {
        return current;
      }
      return current.with(correnspondingField, day);
    }

    public LocalDate addXTo(final LocalDate current, final int amountToAdd) {
      return current.plus(amountToAdd, correndpsondingUnit);
    }

    public LocalDate substractXFrom(final LocalDate current, final long amountToSubstract) {
      return current.minus(amountToSubstract, correndpsondingUnit);
    }
  }
}