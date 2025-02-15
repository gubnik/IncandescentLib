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

import com.google.common.collect.ImmutableMap;

import java.util.*;

public class ImmutableHypermap<AK, SK, V> implements Hypermap<AK, SK, V>
{
    private final ImmutableMap<AK, ImmutableMap<SK, V>> storage;

    private ImmutableHypermap (final Map<AK, ImmutableMap.Builder<SK, V>> submapBuilderMap)
    {
        final ImmutableMap.Builder<AK, ImmutableMap<SK, V>> builder = new ImmutableMap.Builder<>();
        for (var builderEntry : submapBuilderMap.entrySet())
        {
            builder.put(builderEntry.getKey(), builderEntry.getValue().build());
        }
        storage = builder.build();
    }

    public static <AK, SK, V> ImmutableHypermap<AK, SK, V> copyOf (Hypermap<AK, SK, V> hypermap)
    {
        Builder<AK, SK, V> builder = new Builder<>();
        for (var aEntry : hypermap.entrySet())
        {
            for (var sEntry : aEntry.getValue().entrySet())
            {
                builder.put(aEntry.getKey(), sEntry.getKey(), sEntry.getValue());
            }
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    public static <AK, SK, V> ImmutableHypermap<AK, SK, V> of (Object... input)
    {
        if (input.length % 3 != 0)
        {
            throw new IllegalArgumentException("The length of a parameter pack is not divisible by 3");
        }
        final ImmutableHypermap.Builder<AK, SK, V> subBuilder = new ImmutableHypermap.Builder<>();
        for (int i = 0; i < input.length / 3; i++)
        {
            final int idx = i * 3;
            subBuilder.put((AK) input[idx], (SK) input[idx + 1], (V) input[idx + 2]);
        }
        return subBuilder.build();
    }

    @Override
    public Optional<V> get (AK absoluteKey, SK subKey)
    {
        ImmutableMap<SK, V> submap = storage.get(absoluteKey);
        if (submap == null)
        {
            return Optional.empty();
        }
        return Optional.ofNullable(submap.get(subKey));
    }

    @Override
    public boolean put (AK absoluteKey, SK subKey, V value)
    {
        throw new UnsupportedOperationException("The hypermap is immutable");
    }

    @Override
    public Optional<V> remove (AK absoluteKey, SK subKey)
    {
        throw new UnsupportedOperationException("The hypermap is immutable");
    }

    @Override
    public Optional<ImmutableMap<SK, V>> removeAll (AK absoluteKey)
    {
        throw new UnsupportedOperationException("The hypermap is immutable");
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
        ImmutableMap<SK, V> submap = storage.get(absoluteKey);
        return submap != null && submap.containsKey(subKey) && submap.get(subKey).equals(value);
    }

    @Override
    public ImmutableMap<AK, ImmutableMap<SK, V>> raw ()
    {
        return storage;
    }

    @Override
    public Set<Map.Entry<AK, ImmutableMap<SK, V>>> entrySet ()
    {
        return storage.entrySet();
    }

    @Override
    public Set<AK> keySet ()
    {
        return storage.keySet();
    }

    @Override
    public Collection<ImmutableMap<SK, V>> submaps ()
    {
        return storage.values();
    }

    public static class Builder<AK, SK, V>
    {
        private final HashMap<AK, ImmutableMap.Builder<SK, V>> submapBuilderMap = new HashMap<>();

        public Builder ()
        {

        }

        public Builder<AK, SK, V> put (AK absoluteKey, SK subKey, V value)
        {
            submapBuilderMap.putIfAbsent(absoluteKey, new ImmutableMap.Builder<>());
            submapBuilderMap.get(absoluteKey).put(subKey, value);
            return this;
        }

        public ImmutableHypermap<AK, SK, V> build ()
        {
            return new ImmutableHypermap<>(submapBuilderMap);
        }
    }
}
