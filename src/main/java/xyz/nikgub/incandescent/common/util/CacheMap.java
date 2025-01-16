package xyz.nikgub.incandescent.common.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of linked hash map with entry limit
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class CacheMap<K, V> extends LinkedHashMap<K, V>
{
    /**
     * The maximum size of a cache map
     */
    private final long maxCacheSize;

    /**
     * Constructor that provides the maximum size of a cache map
     * @param maxCacheSize The maximum size of a cache map
     */
    public CacheMap(long maxCacheSize)
    {
        this.maxCacheSize = maxCacheSize;
    }

    /**
     * Validates whether the entry is to be removed from the map, based
     * on the {@code maxCacheSize}
     * @param eldest The least recently inserted entry in the map, or if
     *           this is an access-ordered map, the least recently accessed
     *           entry.  This is the entry that will be removed it this
     *           method returns {@code true}.  If the map was empty prior
     *           to the {@code put} or {@code putAll} invocation resulting
     *           in this invocation, this will be the entry that was just
     *           inserted; in other words, if the map contains a single
     *           entry, the eldest entry is also the newest.
     * @return   {@code true} if the eldest entry should be removed from the map; {@code false} if it should be retained
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > this.maxCacheSize;
    }
}
