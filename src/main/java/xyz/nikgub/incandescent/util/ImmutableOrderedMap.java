package xyz.nikgub.incandescent.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class ImmutableOrderedMap<K, V> extends LinkedHashMap<K, V>
{

    private ImmutableOrderedMap (LinkedHashMap<? extends K, ? extends V> map)
    {
        super(map);
    }

    public static  <K, V> ImmutableOrderedMap<K, V> of (LinkedHashMap<? extends K, ? extends V> map)
    {
        return new ImmutableOrderedMap<>(map);
    }

    @Override
    public V put (K key, V value)
    {
        throw new UnsupportedOperationException("This map is immutable and cannot be modified.");
    }

    @Override
    public void putAll (Map<? extends K, ? extends V> m)
    {
        throw new UnsupportedOperationException("This map is immutable and cannot be modified.");
    }

    @Override
    public V remove (Object key)
    {
        throw new UnsupportedOperationException("This map is immutable and cannot be modified.");
    }

    @Override
    public void clear ()
    {
        throw new UnsupportedOperationException("This map is immutable and cannot be modified.");
    }


}
