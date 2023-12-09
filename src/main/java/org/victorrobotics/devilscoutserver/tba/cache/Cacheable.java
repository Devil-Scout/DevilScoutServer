package org.victorrobotics.devilscoutserver.tba.cache;

public interface Cacheable<T> {
  boolean update(T data);
}
