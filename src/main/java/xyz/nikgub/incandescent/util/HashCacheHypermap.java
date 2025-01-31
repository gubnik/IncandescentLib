package xyz.nikgub.incandescent.util;

import java.util.*;

public class HashCacheHypermap<AK, SK, V> implements Hypermap<AK, SK, V, CacheMap<SK, V>, HashMap<AK, CacheMap<SK, V>>>
{
    private final HashMap<AK, CacheMap<SK, V>> storage = new HashMap<>();

    private final int maxCacheSize;

    public HashCacheHypermap (int maxCacheSize)
    {
        this.maxCacheSize = maxCacheSize;
    }

    public int getMaxCacheSize ()
    {
        return maxCacheSize;
    }

    @Override
    public Optional<V> get (AK absoluteKey, SK smallerKey)
    {
        CacheMap<SK, V> cacheMap = storage.get(absoluteKey);
        if (cacheMap == null)
        {
            return Optional.empty();
        }
        return Optional.ofNullable(cacheMap.get(smallerKey));
    }

    @Override
    public boolean put (AK absoluteKey, SK smallerKey, V value)
    {
        storage.putIfAbsent(absoluteKey, new CacheMap<>(maxCacheSize));
        CacheMap<SK, V> cacheMap = storage.get(absoluteKey);
        return cacheMap.putIfAbsent(smallerKey, value) == null;
    }

    @Override
    public Optional<V> remove (AK absoluteKey, SK smallerKey)
    {
        CacheMap<SK, V> cacheMap = storage.get(absoluteKey);
        if (cacheMap == null)
        {
            return Optional.empty();
        }
        return Optional.ofNullable(cacheMap.remove(smallerKey));
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
    public boolean containsValue (V value)
    {
        return submaps().stream().anyMatch(skvCacheMap -> skvCacheMap.containsValue(value));
    }

    @Override
    public boolean containsValue (SK smallerKey, V value)
    {
        for (var submap : submaps())
        {
            if (submap.containsKey(smallerKey) && submap.get(smallerKey).equals(value))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue (AK absoluteKey, SK smallerKey, V value)
    {
        CacheMap<SK, V> submap = storage.get(absoluteKey);
        return submap != null && submap.containsKey(smallerKey) && submap.get(smallerKey).equals(value);
    }

    @Override
    public HashMap<AK, CacheMap<SK, V>> raw ()
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
