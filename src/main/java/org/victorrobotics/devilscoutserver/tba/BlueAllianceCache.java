package org.victorrobotics.devilscoutserver.tba;

import org.victorrobotics.bluealliance.Endpoint;
import org.victorrobotics.devilscoutserver.cache.Cacheable;
import org.victorrobotics.devilscoutserver.cache.OnDemandCache;

public abstract class BlueAllianceCache<K, D, V extends Cacheable<D>>
    extends OnDemandCache<K, D, V> {
  protected BlueAllianceCache(long purgeTime) {
    super(purgeTime);
  }

  protected abstract Endpoint<D> getEndpoint(K key);

  @Override
  protected D getData(K key) {
    return getEndpoint(key).refresh();
  }
}
