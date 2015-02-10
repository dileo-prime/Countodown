package de.doubleslash.innovationsmanagement.countodown.util;

import java.time.LocalDate;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javafx.util.Pair;

import org.apache.commons.lang3.tuple.MutablePair;

import de.doubleslash.innovationsmanagement.countodown.util.RelativeDate.TimeFunction;
import de.doubleslash.innovationsmanagement.countodown.util.RelativeDate.TimeUnit;

public class DateToValue<V> {

  private static final RelativeDate minDate = new RelativeDate(TimeUnit.DAY, TimeFunction.ADD_X_TO,
      -99);

  private final LocalDate currentDate;

  private final TreeMap<MutablePair<LocalDate, RelativeDate>, V> valueMap = new TreeMap<>();

  public DateToValue(final LocalDate current) {
    this.currentDate = current;
  }

  public void buildMapFrom(final List<Pair<RelativeDate, V>> list) {
    valueMap.clear();
    for (final Pair<RelativeDate, V> pair : list) {
      if (pair == null) {
        continue;
      }
      final MutablePair<LocalDate, RelativeDate> key = MutablePair.of(
          pair.getKey().apply(currentDate), pair.getKey());
      valueMap.put(key, pair.getValue());
    }

  }

  public V getValue(final LocalDate date) {
    final Entry<MutablePair<LocalDate, RelativeDate>, V> entry = valueMap.ceilingEntry(MutablePair
        .of(date, minDate));
    if (entry == null) {
      return null;
    }
    final V v = entry.getValue();
    return v;
  }
}
