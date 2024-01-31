package org.victorrobotics.devilscoutserver.cache;

import static org.victorrobotics.devilscoutserver.EncodingUtil.jsonEncode;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;

public abstract class Cache<K, D, V extends Cacheable<D>> implements Map<K, Cache.Value<D, V>> {
  protected final ConcurrentMap<K, Value<D, V>> cacheMap;

  private TrackingView<Entry<K, Value<D, V>>> entrySet;
  private TrackingView<K>                     keySet;
  private TrackingView<Value<D, V>>           values;

  private long lastModified;

  protected Cache() {
    this.cacheMap = new ConcurrentHashMap<>();
    lastModified = System.currentTimeMillis();
  }

  public abstract void refresh();

  protected abstract Value<D, V> getValue(K key);

  public long lastModified() {
    return lastModified;
  }

  protected void modified() {
    lastModified = System.currentTimeMillis();
  }

  protected void updateValue(Value<D, V> value, D data) {
    value.update(data);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Value<D, V> get(Object key) {
    try {
      return getValue((K) key);
    } catch (ClassCastException e) {
      return null;
    }
  }

  @Override
  public boolean containsKey(Object key) {
    return cacheMap.containsKey(key);
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public int size() {
    return cacheMap.size();
  }

  @Override
  public boolean containsValue(Object value) {
    return cacheMap.containsValue(value);
  }

  @Override
  public Value<D, V> remove(Object key) {
    Value<D, V> value = cacheMap.remove(key);
    if (value != null) {
      modified();
    }
    return value;
  }

  @Override
  public void clear() {
    boolean mod = !isEmpty();
    cacheMap.clear();
    if (mod) {
      modified();
    }
  }

  @Override
  public Set<K> keySet() {
    if (keySet == null) {
      keySet = new TrackingView<>(cacheMap.keySet());
    }
    return keySet;
  }

  @Override
  public Collection<Value<D, V>> values() {
    if (values == null) {
      values = new TrackingView<>(cacheMap.values());
    }
    return values;
  }

  @Override
  public Set<Entry<K, Value<D, V>>> entrySet() {
    if (entrySet == null) {
      entrySet = new TrackingView<>(cacheMap.entrySet());
    }
    return entrySet;
  }

  @Override
  public Value<D, V> put(K key, Value<D, V> value) {
    throw new UnsupportedOperationException("Caches are read-only");
  }

  @Override
  public void putAll(Map<? extends K, ? extends Value<D, V>> m) {
    throw new UnsupportedOperationException("Caches are read-only");
  }

  private class TrackingView<T> extends AbstractSet<T> {
    private final Collection<T> source;

    TrackingView(Collection<T> source) {
      this.source = source;
    }

    @Override
    public Iterator<T> iterator() {
      return new TrackingIterator<>(source);
    }

    @Override
    public int size() {
      return source.size();
    }

    @Override
    public boolean isEmpty() {
      return source.isEmpty();
    }

    @Override
    public void clear() {
      boolean mod = size() != 0;
      source.clear();
      if (mod) {
        modified();
      }
    }

    @Override
    public boolean contains(Object v) {
      return source.contains(v);
    }

    @Override
    public boolean remove(Object o) {
      if (source.remove(o)) {
        modified();
        return true;
      }
      return false;
    }
  }

  private class TrackingIterator<T> implements Iterator<T> {
    private final Iterator<T> iterator;

    TrackingIterator(Collection<T> collection) {
      iterator = collection.iterator();
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public T next() {
      return iterator.next();
    }

    @Override
    public void remove() {
      iterator.remove();
      modified();
    }
  }

  public static class Value<D, V extends Cacheable<D>> implements Comparable<Value<?, V>> {
    private final V        val;
    private final Runnable onModification;

    private volatile long lastModified;
    private volatile long lastAccess;

    private String jsonCache;

    public Value(V value, Runnable onModification) {
      this.val = value;
      this.onModification = onModification;
    }

    void update(D data) {
      if (val.update(data)) {
        onModification.run();
      }
    }

    public V value() {
      lastAccess = System.currentTimeMillis();
      return val;
    }

    public long lastModified() {
      return lastModified;
    }

    public long lastAccess() {
      return lastAccess;
    }

    @Override
    @SuppressWarnings({ "unchecked", "java:S3740", "java:S1210" })
    public int compareTo(Value<?, V> o) {
      if (val instanceof Comparable v) {
        return ((Comparable<V>) v).compareTo(o.val);
      }
      return 0;
    }

    @JsonRawValue
    @JsonValue
    String toJson() {
      if (jsonCache == null) {
        jsonCache = jsonEncode(value());
      }
      return jsonCache;
    }
  }
}
