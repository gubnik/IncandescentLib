package xyz.nikgub.incandescent.util;

import java.util.*;

public class LimitedHypermap<AK, SK, V> implements Hypermap<AK, SK, V>
{
    private final CacheMap<AK, CacheMap<SK, V>> storage;

    private final int outerCapacity;
    private final int innerCapacity;

    public LimitedHypermap (int outerCapacity, int innerCapacity)
    {
        this.storage = new CacheMap<>(outerCapacity);
        this.outerCapacity = outerCapacity;
        this.innerCapacity = innerCapacity;
    }

    public int getOuterCapacity ()
    {
        return outerCapacity;
    }

    public int getInnerCapacity ()
    {
        return innerCapacity;
    }

    @Override
    public Optional<V> get (AK absoluteKey, SK subKey)
    {
        CacheMap<SK, V> cacheMap = storage.get(absoluteKey);
        if (cacheMap == null)
        {
            return Optional.empty();
        }
        return Optional.ofNullable(cacheMap.get(subKey));
    }

    @Override
    public boolean put (AK absoluteKey, SK subKey, V value)
    {
        storage.putIfAbsent(absoluteKey, new CacheMap<>(outerCapacity));
        CacheMap<SK, V> cacheMap = storage.get(absoluteKey);
        return cacheMap.putIfAbsent(subKey, value) == null;
    }

    @Override
    public Optional<V> remove (AK absoluteKey, SK subKey)
    {
        CacheMap<SK, V> cacheMap = storage.get(absoluteKey);
        if (cacheMap == null)
        {
            return Optional.empty();
        }
        return Optional.ofNullable(cacheMap.remove(subKey));
    }

    @Override
    public Optional<CacheMap<SK, V>> removeAll (AK absoluteKey)
    {
        return Optional.ofNullable(storage.remove(absoluteKey));
    }

    @Override
    public boolean containsKey (AK absoluteKey)
    {
        return storage.containsKey(absoluteKey);
    }

    @Override
    public boolean containsKey (AK absoluteKey, SK subKey)
    {
        return storage.containsKey(absoluteKey) && storage.get(absoluteKey).containsKey(subKey);
    }

    @Override
    public boolean containsValue (V value)
    {
        return submaps().stream().anyMatch(skvCacheMap -> skvCacheMap.containsValue(value));
    }

    @Override
    public boolean containsValue (SK subKey, V value)
    {
        for (var submap : submaps())
        {
            if (submap.containsKey(subKey) && submap.get(subKey).equals(value))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue (AK absoluteKey, SK subKey, V value)
    {
        CacheMap<SK, V> submap = storage.get(absoluteKey);
        return submap != null && submap.containsKey(subKey) && submap.get(subKey).equals(value);
    }

    @Override
    public CacheMap<AK, CacheMap<SK, V>> raw ()
    {
        return storage;
    }

    @Override
    public Set<Map.Entry<AK, CacheMap<SK, V>>> entrySet ()
    {
        return storage.entrySet();
    }

    @Override
    public Set<AK> keySet ()
    {
        return storage.keySet();
    }

    @Override
    public Collection<CacheMap<SK, V>> submaps ()
    {
        return storage.values();
    }
}
