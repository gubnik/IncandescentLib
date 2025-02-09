/*
    Incandescent Lib, Minecraft Forge light-weight library
    Copyright (C) 2025, nikgub_

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.nikgub.incandescent.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of linked hash map with entry limit
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class CacheMap<K, V> extends LinkedHashMap<K, V> implements Map<K, V>
{
    /**
     * The maximum size of a cache map
     */
    private final long maxCacheSize;

    /**
     * Constructor that provides the maximum size of a cache map
     *
     * @param maxCacheSize The maximum size of a cache map
     */
    public CacheMap (long maxCacheSize)
    {
        this.maxCacheSize = maxCacheSize;
    }

    /**
     * Validates whether the entry is to be removed from the map, based
     * on the {@code maxCacheSize}
     *
     * @param eldest The least recently inserted entry in the map, or if
     *               this is an access-ordered map, the least recently accessed
     *               entry.  This is the entry that will be removed it this
     *               method returns {@code true}.  If the map was empty prior
     *               to the {@code put} or {@code putAll} invocation resulting
     *               in this invocation, this will be the entry that was just
     *               inserted; in other words, if the map contains a single
     *               entry, the eldest entry is also the newest.
     * @return {@code true} if the eldest entry should be removed from the map; {@code false} if it should be retained
     */
    @Override
    protected boolean removeEldestEntry (Map.Entry<K, V> eldest)
    {
        return size() > this.maxCacheSize;
    }
}
