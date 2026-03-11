package org.victorrobotics.devilscoutserver.cache;

public interface Cacheable<T> {
  boolean update(T data);
}
