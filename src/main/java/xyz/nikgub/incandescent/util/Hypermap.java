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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Hypermap is a generic interface that represents a multi-level mapping structure.
 * It allows for the association of values (V) with a combination of an absolute key (AK)
 * and a sub key (SK), organized in a nested map structure.
 *
 *
 * <h2>Abstraction</h2>
 * <p>
 * This interface does not implement {@link Map} for the sake of abstraction,
 * since the goal is to provide an API to use existing map definitions in this complex form.
 * Access to inner contents of the class are provided via {@link #raw()} method, and methods
 * like {@link #submaps()} should ideally depend on the return result of {@link #raw()},
 * though this is not enforced.
 * </p>
 * <h2>Type safety</h2>
 * <p>
 * Type safety of inner maps is <b>NOT</b> enforced, for the sake of usability of the base
 * Hypermap interface in an implementation-free context.
 * Methods that return submaps are assumed to return something that extends {@link Map},
 * and thus they do not enforce any particular kind of map. It is the implementation's
 * liability to keep track of what map is returned, and if it is immutable or not, and
 * base Hypermap always assumes that all of its methods are consistently compatible.
 * <h3>Example</h3>
 * <pre><code>
 *         public class HashHashHypermap&lt;AK, SK, V&gt; implements Hypermap&lt;AK, SK, V&gt;
 *         {
 *              private final HashMap&lt;AK, HashMap&lt;SK, V&gt;&gt; storage = new HashMap&lt;&gt;();   // actual storage
 *
 *              public HashMap&lt;AK, HashMap&lt;SK, V&gt;&gt; raw () // correct usage
 *              {
 *                  return storage;
 *              }
 *
 *              public LinkedHashMap&lt;AK, CacheMap&lt;SK, V&gt;&gt; raw () // wrong usage, mismatched maps
 *              {
 *                  return storage;
 *              }
 *
 *              public ImmutableHashMap&lt;AK, ImmutableHashMap&lt;SK, V&gt;&gt; raw () // wrong usage, immutable maps
 *              {
 *                  return storage;
 *              }
 *         }
 *     </code></pre>
 * </p>
 * <h2>Multithreading</h2>
 * <p>
 * The interface does not guarantee multithreading support, and the safety of such operations
 * depends entirely on implementation.
 * Multithreading support, if implemented, should rely on already existing concurrent versions
 * of {@link Map} as to avoid further complexity.
 * </p>
 * <h2>Exceptions</h2>
 * <p>
 * Implementations of the Hypermap must not add exceptions to the implementations of the methods
 * of this interface, and should instead rely on {@link Optional} and exceptions thrown by inner
 * structures.
 * The only exceptions that may be thrown in any of the methods are {@link IllegalArgumentException}
 * for whenever the keys or the values are {@code null}, and {@link UnsupportedOperationException} for
 * immutable implementations of this interface.
 * </p>
 * <h2>Immutable implementation</h2>
 * <p>
 * Immutable implementations of Hypermap must consider both mappings immutable, for the sake
 * of consistency.
 * <h3>Example</h3>
 * <pre><code>
 *         public class ImmutableHashHashHypermap&lt;AK, SK, V&gt; implements Hypermap&lt;AK, SK, V&gt;
 *         {
 *              private final ImmutableHashMap&lt;AK, ImmutableHashMap&lt;SK, V&gt;&gt; storage = new HashMap&lt;&gt;();   // correct storage
 *
 *              private final ImmutableHashMap&lt;AK, HashMap&lt;SK, V&gt;&gt; storage = new HashMap&lt;&gt;();   // incorrect storage, used mutable submap
 *     </code></pre>
 * </p>
 * </p>
 *
 * @param <AK> The type of the absolute key.
 * @param <SK> The type of the sub key.
 * @param <V>  The type of the value associated with the keys.
 * @see HashHashHypermap Common implementation
 * @see ImmutableHypermap Immutable implementation
 */
public interface Hypermap<AK, SK, V>
{

    /**
     * Retrieves a value associated with the given absolute key and sub key.
     *
     * @param absoluteKey The absolute key used to access the map.
     * @param subKey      The sub key used to access the submap.
     * @return An {@link Optional} containing the value if present, {@link Optional#empty()} if none was found.
     */
    Optional<V> get (AK absoluteKey, SK subKey);

    /**
     * Puts a value into the map associated with the given absolute key and sub key.
     *
     * @param absoluteKey The absolute key under which the value is stored.
     * @param subKey      The sub key under which the value is stored in the submap.
     * @param value       The value to be stored.
     * @return {@code true} if the value was not present, {@code false} otherwise.
     */
    boolean put (AK absoluteKey, SK subKey, V value);

    /**
     * Removes and returns a value from the hypermap.
     *
     * @param absoluteKey The absolute key under which the value is stored.
     * @param subKey      The sub key under which the value is stored in the submap.
     * @return {@link Optional} of removed value, {@link Optional#empty()} if none was found.
     */
    Optional<V> remove (AK absoluteKey, SK subKey);

    /**
     * Removes and returns an entire submap from the hypermap.
     *
     * @param absoluteKey The absolute key under which the submap is stored.
     * @return {@link Optional} of removed submap, {@link Optional#empty()} if none was found.
     */
    Optional<? extends Map<SK, V>> removeAll (AK absoluteKey);

    /**
     * Checks if the hypermap contains an absolute key
     *
     * @param absoluteKey Key to be checked
     * @return {@code true} if the hypermap has the value
     */
    boolean containsKey (AK absoluteKey);

    /**
     * Checks if the hypermap contains an absolute key and a sub key
     *
     * @param absoluteKey Absolute key to be checked
     * @param subKey      Smaller key to be checked
     * @return {@code true} if the hypermap has the value
     */
    boolean containsKey (AK absoluteKey, SK subKey);

    /**
     * Checks if the hypermap contains a value
     *
     * @param value Value to be checked
     * @return {@code true} if the hypermap has the value
     */
    boolean containsValue (V value);

    /**
     * Checks if the hypermap contains a sub key-value pair
     *
     * @param subKey Smaller key to be checked
     * @param value  Value to be checked
     * @return {@code true} if the hypermap has the value
     */
    boolean containsValue (SK subKey, V value);

    /**
     * Checks if the hypermap contains an absolute key, sub key and value mapping
     *
     * @param absoluteKey Absolute key to be checked
     * @param subKey      Smaller key to be checked
     * @param value       Value to be checked
     * @return {@code true} if the hypermap has the value
     */
    boolean containsValue (AK absoluteKey, SK subKey, V value);

    /**
     * Returns the raw base map of the Hypermap.
     *
     * @return The base map.
     */
    Map<AK, ? extends Map<SK, V>> raw ();

    /**
     * Returns a set of entries in the Hypermap, where each entry consists of an absolute key
     * and its corresponding submap.
     *
     * @return A {@link Set} of Map.Entry objects representing the entries in the Hypermap.
     */
    Set<? extends Map.Entry<AK, ? extends Map<SK, V>>> entrySet ();

    /**
     * Returns a set of absolute keys present in the Hypermap.
     *
     * @return A {@link Set} of absolute keys of type AK.
     */
    Set<AK> keySet ();

    /**
     * Returns a collection of submaps associated with the absolute keys in the Hypermap.
     *
     * @return A {@link Collection} of submaps of type SM.
     */
    Collection<? extends Map<SK, V>> submaps ();

    /**
     * Creates an immutable hypermap from the provided elements.
     * <p>
     *     This method performs no type checking whatsoever and expects elements of the correct type
     *     to be provided, otherwise it will crash with a cast exception.
     * </p>
     * @param input {@code Object} varargs, must come in triplets representing absolute and sub keys and a value.
     * @return {@link Hypermap} view instance containing all the elements.
     * @param <AK> The type of the absolute key.
     * @param <SK> The type of the sub key.
     * @param <V>  The type of the value associated with the keys.
     */
    @SuppressWarnings("unchecked")
    static <AK, SK, V> Hypermap<AK, SK, V> of (Object... input)
    {
        if (input.length % 3 != 0)
        {
            throw new InternalError("length is not divisible by 3");
        }
        final ImmutableHypermap.Builder<AK, SK, V> subBuilder = new ImmutableHypermap.Builder<>();
        for (int i = 0; i < input.length / 3; i++)
        {
            final int idx = i * 3;
            subBuilder.put((AK) input[idx], (SK) input[idx + 1], (V) input[idx + 2]);
        }
        return subBuilder.build();
    }
}

