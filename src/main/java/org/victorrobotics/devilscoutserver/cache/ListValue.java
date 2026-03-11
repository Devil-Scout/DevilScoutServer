package org.victorrobotics.devilscoutserver.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.fasterxml.jackson.annotation.JsonValue;

public abstract class ListValue<K, D, L, V extends Cacheable<L>> implements Cacheable<D> {
  private final ConcurrentNavigableMap<K, V> map;
  private final Collection<V>                values;

  protected ListValue(Comparator<K> sortOrder) {
    this.map = new ConcurrentSkipListMap<>(sortOrder);
    this.values = Collections.unmodifiableCollection(map.values());
  }

  protected abstract V createValue(K key, L data);

  protected abstract K getKey(L item);

  protected abstract List<L> getList(D data);

  @Override
  public boolean update(D data) {
    boolean change = false;

    List<L> list = getList(data);
    Collection<K> keys = new ArrayList<>();
    for (L item : list) {
      K key = getKey(item);
      keys.add(key);

      V info = map.get(key);
      if (info == null) {
        map.put(key, createValue(key, item));
        change = true;
      } else {
        change |= info.update(item);
      }
    }

    change |= map.keySet()
                 .retainAll(keys);
    return change;
  }

  @JsonValue
  public Collection<V> values() {
    return values;
  }

  public V get(K key) {
    return map.get(key);
  }
}
