package xyz.nikgub.incandescent.util;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Hypermap is a generic interface that represents a multi-level mapping structure.
 * It allows for the association of values (V) with a combination of an absolute key (AK)
 * and a smaller key (SK), organized in a nested map structure.
 *
 * <h2>Abstraction</h2>
 * <p>
 *     This interface does not implement {@link Map} for the sake of abstraction,
 *     since the goal is to provide an API to use existing map definitions in this complex form.
 *     Access to inner contents of the class are provided via {@link #raw()} method, and methods
 *     like {@link #submaps()} should ideally depend on the return result of {@link #raw()},
 *     though this is not enforced.
 * </p>
 * <h2>Multithreading</h2>
 * <p>
 *     The interface does not guarantee multithreading support, and the safety of such operations
 *     depends entirely on implementation.
 *     Multithreading support, if implemented, should rely on already existing concurrent versions
 *     of {@link Map} as to avoid further complexity.
 * </p>
 * <h2>Exceptions</h2>
 * <p>
 *     Implementations of the Hypermap must not add exceptions to the implementations of the methods
 *     of this interface, and should instead rely on {@link Optional} and exceptions thrown by inner
 *     structures.
 *     The only exceptions that may be thrown in any of the methods are {@link IllegalArgumentException}
 *     for whenever the keys or the values are {@code null}, and {@link UnsupportedOperationException} for
 *     immutable implementations of this interface.
 * </p>
 *
 * @param <AK> The type of the absolute key.
 * @param <SK> The type of the smaller key.
 * @param <V> The type of the value associated with the keys.
 * @param <SM> The type of the submap, which extends Map with keys of type SK and values of type V.
 * @param <BM> The type of the base map, which extends Map with keys of type AK and values of type SM.
 */
public interface Hypermap<AK, SK, V, SM extends Map<SK, V>, BM extends Map<AK, SM>> {

    /**
     * Retrieves a value associated with the given absolute key and smaller key.
     *
     * @param absoluteKey The absolute key used to access the map.
     * @param smallerKey The smaller key used to access the submap.
     * @return An {@link Optional} containing the value if present, {@link Optional#empty()} if none was found.
     */
    Optional<V> get(AK absoluteKey, SK smallerKey);

    /**
     * Puts a value into the map associated with the given absolute key and smaller key.
     *
     * @param absoluteKey The absolute key under which the value is stored.
     * @param smallerKey The smaller key under which the value is stored in the submap.
     * @param value The value to be stored.
     * @return {@code true} if the value was not present, {@code false} otherwise.
     */
    boolean put(AK absoluteKey, SK smallerKey, V value);

    /**
     * Removes and returns a value from the hypermap.
     *
     * @param absoluteKey The absolute key under which the value is stored.
     * @param smallerKey The smaller key under which the value is stored in the submap.
     * @return {@link Optional} of removed value, {@link Optional#empty()} if none was found.
     */
    Optional<V> remove (AK absoluteKey, SK smallerKey);

    /**
     * Removes and returns an entire submap from the hypermap.
     *
     * @param absoluteKey The absolute key under which the submap is stored.
     * @return {@link Optional} of removed submap, {@link Optional#empty()} if none was found.
     */
    Optional<SM> removeAll (AK absoluteKey);

    /**
     * Checks if the hypermap contains an absolute key
     * @param absoluteKey Key to be checked
     * @return {@code true} if the hypermap has the value
     */
    boolean containsKey (AK absoluteKey);

    /**
     * Checks if the hypermap contains an absolute key and a smaller key
     * @param absoluteKey Absolute key to be checked
     * @param smallerKey  Smaller key to be checked
     * @return {@code true} if the hypermap has the value
     */
    boolean containsKey (AK absoluteKey, SK smallerKey);

    /**
     * Checks if the hypermap contains a value
     * @param value Value to be checked
     * @return {@code true} if the hypermap has the value
     */
    boolean containsValue (V value);

    /**
     * Checks if the hypermap contains a smaller key-value pair
     * @param smallerKey Smaller key to be checked
     * @param value Value to be checked
     * @return {@code true} if the hypermap has the value
     */
    boolean containsValue (SK smallerKey, V value);

    /**
     * Checks if the hypermap contains an absolute key, smaller key and value mapping
     * @param absoluteKey Absolute key to be checked
     * @param smallerKey Smaller key to be checked
     * @param value Value to be checked
     * @return {@code true} if the hypermap has the value
     */
    boolean containsValue (AK absoluteKey, SK smallerKey, V value);

    /**
     * Returns the raw base map of the Hypermap.
     *
     * @return The base map of type BM.
     */
    BM raw();

    /**
     * Returns a set of entries in the Hypermap, where each entry consists of an absolute key
     * and its corresponding submap.
     *
     * @return A {@link Set} of Map.Entry objects representing the entries in the Hypermap.
     */
    Set<Map.Entry<AK, SM>> entrySet();

    /**
     * Returns a set of absolute keys present in the Hypermap.
     *
     * @return A {@link Set} of absolute keys of type AK.
     */
    Set<AK> keySet();

    /**
     * Returns a collection of submaps associated with the absolute keys in the Hypermap.
     *
     * @return A {@link Collection} of submaps of type SM.
     */
    Collection<SM> submaps();
}

