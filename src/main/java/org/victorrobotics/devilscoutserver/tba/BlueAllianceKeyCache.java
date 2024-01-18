package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.devilscoutserver.cache.Cacheable;
import org.victorrobotics.devilscoutserver.cache.OnDemandCache;

import java.util.Collection;
import java.util.List;

public abstract class BlueAllianceKeyCache<K, I, D extends Cacheable<I>, V extends Cacheable<Collection<D>>>
    extends OnDemandCache<K, Collection<D>, V> {
  protected BlueAllianceKeyCache(long purgeTime) {
    super(purgeTime);
  }

  protected abstract Endpoint<List<String>> getEndpoint(K key);

  protected abstract D sourceData(String key);

  @Override
  public List<D> getData(K key) {
    return getEndpoint(key).refresh()
                           .stream()
                           .map(this::sourceData)
                           .toList();
  }
}
