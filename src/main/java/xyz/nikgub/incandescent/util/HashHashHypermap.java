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

import java.util.*;

public class HashHashHypermap<AK, SK, V> implements Hypermap<AK, SK, V>
{
    private final HashMap<AK, HashMap<SK, V>> storage = new HashMap<>();


    public HashHashHypermap ()
    {
    }

    @Override
    public Optional<V> get (AK absoluteKey, SK subKey)
    {
        HashMap<SK, V> submap = storage.get(absoluteKey);
        if (submap == null)
        {
            return Optional.empty();
        }
        return Optional.ofNullable(submap.get(subKey));
    }

    @Override
    public boolean put (AK absoluteKey, SK subKey, V value)
    {
        storage.putIfAbsent(absoluteKey, new HashMap<>());
        HashMap<SK, V> submap = storage.get(absoluteKey);
        return submap.putIfAbsent(subKey, value) == null;
    }

    @Override
    public Optional<V> remove (AK absoluteKey, SK subKey)
    {
        HashMap<SK, V> submap = storage.get(absoluteKey);
        if (submap == null)
        {
            return Optional.empty();
        }
        return Optional.ofNullable(submap.remove(subKey));
    }

    @Override
    public Optional<HashMap<SK, V>> removeAll (AK absoluteKey)
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
        HashMap<SK, V> submap = storage.get(absoluteKey);
        return submap != null && submap.containsKey(subKey) && submap.get(subKey).equals(value);
    }

    @Override
    public HashMap<AK, HashMap<SK, V>> raw ()
    {
        return storage;
    }

    @Override
    public Set<Map.Entry<AK, HashMap<SK, V>>> entrySet ()
    {
        return storage.entrySet();
    }

    @Override
    public Set<AK> keySet ()
    {
        return storage.keySet();
    }

    @Override
    public Collection<HashMap<SK, V>> submaps ()
    {
        return storage.values();
    }
}
