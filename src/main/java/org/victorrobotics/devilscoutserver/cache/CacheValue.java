package org.victorrobotics.devilscoutserver.cache;

import org.victorrobotics.bluealliance.Endpoint;

import java.util.concurrent.CompletableFuture;

public class CacheValue<D, V extends Cacheable<D>> {
  private final V value;

  private volatile long timestamp;

  public CacheValue(V value) {
    this.value = value;
  }

  public boolean refresh(Endpoint<D> endpoint) {
    return startRefresh(endpoint).join();
  }

  public CompletableFuture<Boolean> startRefresh(Endpoint<D> endpoint) {
    return endpoint.request()
                   .thenApply(this::update);
  }

  public boolean update(D data) {
    if (value.update(data)) {
      timestamp = System.currentTimeMillis();
      return true;
    }
    return false;
  }

  public V value() {
    return value;
  }

  public long timestamp() {
    return timestamp;
  }
}
