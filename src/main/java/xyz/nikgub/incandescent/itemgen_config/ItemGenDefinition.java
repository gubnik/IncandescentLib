package xyz.nikgub.incandescent.itemgen_config;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nikgub.incandescent.itemgen_config.interfaces.IConverter;
import xyz.nikgub.incandescent.itemgen_config.interfaces.IPropertyMutator;
import xyz.nikgub.incandescent.itemgen_config.interfaces.IPseudoConstructor;
import xyz.nikgub.incandescent.util.ImmutableOrderedMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;

/**
 * A definition for an {@link Item} to be produced from a JSON config.
 *
 * <h2>General structure</h2>
 * <p>
 * The definition is done via a {@link Builder} and is separated into
 * constructor arguments definition and property definitions.
 * Properties are applied before the constructor arguments are gathered, and
 * collisions may occur if definitions of a property and of a constructor argument
 * have the same string identifier.
 * </p>
 *
 * <h2>Constructor arguments definition</h2>
 * <p>
 * The constructor can be defined either with a manually provided {@link IPseudoConstructor},
 * which takes in an {@code Object...} varargs and returns an instance of the item,
 * or using reflection {@link #generateAutoConstructor(ItemGenConfigProvider)}, which tries
 * to search for a constructor within the class' definition based on known args' transformed types.
 * Exact implementation, type safety or constructor are not enforced for this pseudo-constructor,
 * but it is guaranteed that the {@code Object...} provided will contain correctly typed arguments
 * as per constructor definition.
 * The constructor arguments are to be defined in the order they go into the constructor.
 * Since constructors extending {@link Item} must take {@link Item.Properties} as an argument at some point,
 * the {@link Builder#constructorArgProperties()} is provided to define that the properties are expected to be
 * built by the definition itself and are not required to be present in the JSON
 * or as a default argument provided by {@link Builder#constructorArgOrDefault(String, Class, Object)}.
 * This definition of properties is the expected behaviour and is guaranteed to work.
 * </p>
 *
 * <h2>Property definitions</h2>
 * <p>
 * Properties are defined by their name and {@link IPropertyMutator}, which is expected to be a method
 * that performs an operation on an instance of {@link Item.Properties} and returns it, but it is not
 * enforced for it to act in exactly this way.
 * </p>
 *
 * <h2>Example usage</h2>
 * <pre>
 *     {@code
 *     public static final ItemGenDefinition.Generator<SwordItem> SWORD_DEFINITION = new ItemGenDefinition.Builder<>(SwordItem.class)
 *         .startConstructor()
 *         .constructorArgConverted("tier", Tier.class, DefaultedTierImplementation::fromObjectMap)
 *         .constructorArgConverted("attackDamageModifier", int.class, Number::intValue)
 *         .constructorArgConverted("attackSpeed", float.class, (Number number) -> number.floatValue() - 4)
 *         .constructorArgProperties()
 *         .finishConstructor()
 *         .propertyWithConverter("durability", int.class, Item.Properties::durability, Number::intValue)
 *         .build()
 *         .generateAutoConstructor(new ItemGenConfigProvider("testconf.json"));
 *
 *     // static lifetime somewhere
 *     public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
 *
 *     // within mod's setup
 *     SWORD_DEFINITION.register(ITEMS);
 *     ITEMS.register(modEventBus);
 *     }
 * </pre>
 *
 * @param <I> Type extending {@link Item}
 * @apiNote Properties take precedence over {@link net.minecraft.world.item.Tier} values.
 * @see ItemGenConfigProvider
 * @see DefaultedTierImplementation
 * @see ItemGenObjectInfo
 */
public class ItemGenDefinition<I extends Item>
{
    /**
     * Magic string to indicate the placement of generated properties within the constructor.
     */
    private static final String PROPERTY_DEFINITION = "___PROPERTIES___";

    /**
     * Class object of generated items.
     * Used for reflection when needed.
     */
    private final Class<I> clazz;

    /**
     * Ordered map view of constructor argument definitions.
     */
    private final ImmutableOrderedMap<String, ConstructorArgDefinition<?, ?>> constructorArguments;

    /**
     * Ordered map view of property mutators.
     */
    private final ImmutableOrderedMap<String, PropertyDefinition<?, ?>> propertyMutators;

    /**
     * Cached classes of the expected transformed constructor arguments.
     */
    private final Class<?>[] constructorArgsClasses;

    /**
     * Cached result of {@link #fetchConstructor()}.
     * Can be null if the method has not invoked yet.
     */
    @Nullable
    private Constructor<I> cachedConstructor;

    /**
     * Constructor to be used by {@link Builder}.
     *
     * @param constructorArguments View of a {@link LinkedHashMap} to be copied into {@link #constructorArguments}
     * @param propertyMutators     View of a {@link LinkedHashMap} to be copied into {@link #propertyMutators}
     */
    private ItemGenDefinition (Class<I> clazz, LinkedHashMap<String, ConstructorArgDefinition<?, ?>> constructorArguments, LinkedHashMap<String, PropertyDefinition<?, ?>> propertyMutators)
    {
        this.clazz = clazz;
        this.constructorArguments = ImmutableOrderedMap.of(constructorArguments);
        this.propertyMutators = ImmutableOrderedMap.of(propertyMutators);
        this.constructorArgsClasses = this.constructorArguments.values().stream().map(ConstructorArgDefinition::clazz).toArray(Class<?>[]::new);
    }

    /**
     * Main generator of the definition. Processes an {@link ItemGenConfigProvider} into
     * validated properties and constructor arguments to be later passed into {@link IPseudoConstructor}.
     *
     * @param configProvider    {@link ItemGenConfigProvider} for the gathered JSON objects.
     * @param pseudoConstructor {@link IPseudoConstructor} to be used in item creation.
     * @return {@link Product} containing definitions of items ready to be registered
     */
    public Product<I> generate (ItemGenConfigProvider configProvider, IPseudoConstructor<I> pseudoConstructor)
    {
        final Map<String, Supplier<I>> resultingMap = new HashMap<>();
        for (var definedCandidate : configProvider.getItemObjects().entrySet())
        {
            final String candidateName = definedCandidate.getKey();
            final ItemGenObjectInfo objectInfo = definedCandidate.getValue();
            final Item.Properties properties = this.mutateProperties(objectInfo);
            final Object[] constructorValues = this.provideConstructorArgs(objectInfo, properties);
            resultingMap.put(candidateName, () -> pseudoConstructor.create(constructorValues)); //this.instantiate(constructor, constructorValues));
        }
        return new Product<>(resultingMap);
    }

    /**
     * Reflection-driven generator of the definition. Processes an {@link ItemGenConfigProvider} into
     * validated properties and constructor arguments to be later passed into the constructor
     * gathered from reflection based on {@link #constructorArguments}.
     *
     * @param configProvider {@link ItemGenConfigProvider} for the gathered JSON objects.
     * @return {@link Product} containing definitions of items ready to be registered
     */
    public Product<I> generateAutoConstructor (ItemGenConfigProvider configProvider)
    {
        final Map<String, Supplier<I>> resultingMap = new HashMap<>();
        final Constructor<I> optionalConstructor = fetchConstructor();
        for (var definedCandidate : configProvider.getItemObjects().entrySet())
        {
            final String candidateName = definedCandidate.getKey();
            final ItemGenObjectInfo objectInfo = definedCandidate.getValue();
            final Item.Properties properties = this.mutateProperties(objectInfo);
            final Object[] constructorValues = this.provideConstructorArgs(objectInfo, properties);
            resultingMap.put(candidateName, () ->
            {
                try
                {
                    return optionalConstructor.newInstance(constructorValues);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
                {
                    throw new RuntimeException("Constructor execution failed for an item definition " + candidateName +
                        ", with classes " + Arrays.toString(constructorArgsClasses), e);
                }
            });
        }
        return new Product<>(resultingMap);
    }

    /**
     * Fetches a constructor from the reflection and caches the result to be reused on subsequent calls.
     *
     * @return Fetched {@link Constructor}
     */
    @NotNull
    private Constructor<I> fetchConstructor ()
    {
        if (cachedConstructor != null)
        {
            return cachedConstructor;
        }
        try
        {
            cachedConstructor = clazz.getConstructor(constructorArgsClasses);
            return cachedConstructor;
        } catch (NoSuchMethodException e)
        {
            final Class<?>[] closestArgsArr = closestArray(constructorArgsClasses, Arrays.stream(clazz.getConstructors())
                .map(Constructor::getParameterTypes).toArray(Class[][]::new));
            throw new RuntimeException("No constructor with types " + Arrays.toString(constructorArgsClasses) + " found." + (closestArgsArr != null ?
                "Maybe you meant to define " + Arrays.toString(closestArgsArr) + "?" : ""), e);
        }
    }

    /**
     * Helper method to generate mutated properties based on {@link #propertyMutators}
     *
     * @param objectInfo {@link ItemGenObjectInfo} values of the to-be-created item.
     * @return Created and mutated {@link Item.Properties}
     */
    private Item.Properties mutateProperties (final ItemGenObjectInfo objectInfo)
    {
        Item.Properties properties = new Item.Properties();
        for (var definedProperties : propertyMutators.entrySet())
        {
            final String propertyName = definedProperties.getKey();
            final PropertyDefinition<?, ?> propertyDefinition = definedProperties.getValue();
            properties = propertyDefinition.applyMutator(properties, propertyName, objectInfo);
        }
        return properties;
    }

    /**
     * Helper method to provide an array of constructor args.
     * All the checks regarding nullability, default value, mapping etc. are handled by
     * {@link ConstructorArgDefinition#getValue(Object)}.
     *
     * @param objectInfo {@link ItemGenObjectInfo} values of the to-be-created item.
     * @param properties {@link Item.Properties} generated properties of the to-be-created item.
     * @return A primitive array of constructor argument values
     */
    private Object[] provideConstructorArgs (final ItemGenObjectInfo objectInfo, final Item.Properties properties)
    {
        final Object[] constructorValues = new Object[constructorArguments.size()];
        int constructorArgId = 0;
        for (var constructorArg : constructorArguments.entrySet())
        {
            final String constructorArgName = constructorArg.getKey();
            final ConstructorArgDefinition<?, ?> definition = constructorArg.getValue();
            if (constructorArgName.equals(PROPERTY_DEFINITION))
            {
                constructorValues[constructorArgId++] = properties;
                continue;
            }
            constructorValues[constructorArgId++] = definition.produceArg(constructorArgName, objectInfo);
        }
        return constructorValues;
    }

    @SafeVarargs
    public static <T> T[] closestArray (T[] targetArray, T[]... arrays) {
        int maxMatches = 0;
        int bestIndex = -1;
        Set<T> targetSet = new HashSet<>(Arrays.asList(targetArray));
        for (int i = 0; i < arrays.length; i++) {
            long matchCount = Arrays.stream(arrays[i])
                .filter(targetSet::contains)
                .count();
            if (matchCount > maxMatches) {
                maxMatches = (int) matchCount;
                bestIndex = i;
            }
        }

        if (bestIndex < 0)
        {
            return null;
        }
        return arrays[bestIndex];
    }


    /**
     * Builder class for {@link ItemGenDefinition}
     *
     * @param <I> Type extending {@link Item}
     */
    @SuppressWarnings("unused")
    public static class Builder<I extends Item>
    {
        private final LinkedHashMap<String, ConstructorArgDefinition<?, ?>> constructorArguments = new LinkedHashMap<>();
        private final LinkedHashMap<String, PropertyDefinition<?, ?>> propertyMutators = new LinkedHashMap<>();

        private final Class<I> clazz;

        private boolean isConstructorStarted = false;
        private boolean isConstructorFinished = false;

        public Builder (Class<I> clazz)
        {
            this.clazz = clazz;
        }

        @NotNull
        public Builder<I> startConstructor ()
        {
            if (isConstructorFinished)
            {
                throw new IllegalStateException("Attempted to redefine Item definition constructor");
            }
            isConstructorStarted = true;
            return this;
        }

        @NotNull
        public Builder<I> endConstructor ()
        {
            if (isConstructorFinished)
            {
                throw new IllegalStateException("Attempted to redefine Item definition constructor");
            }
            if (!isConstructorStarted)
            {
                throw new IllegalStateException("Item definition constructor cannot be finished because it had not been started");
            }
            if (!constructorArguments.containsKey(PROPERTY_DEFINITION))
            {
                throw new IllegalStateException("Constructor must contain properties placement");
            }
            isConstructorStarted = false;
            isConstructorFinished = true;
            return this;
        }

        @NotNull
        public <T> Builder<I> constructorArg (@NotNull String argName, @NotNull Class<T> clazz)
        {
            this.validateConstructorArg(argName);
            constructorArguments.put(argName, new ConstructorArgDefinition<>(clazz, null, null));
            return this;
        }

        @NotNull
        public <FT, TT> Builder<I> constructorArgConverted (@NotNull String argName, @NotNull Class<TT> clazz, @NotNull IConverter<FT, TT> converter)
        {
            this.validateConstructorArg(argName);
            constructorArguments.put(argName, new ConstructorArgDefinition<>(clazz, null, converter));
            return this;
        }

        @NotNull
        public <T> Builder<I> constructorArgOrDefault (@NotNull String argName, @NotNull Class<T> clazz, @NotNull T defaultValue)
        {
            this.validateConstructorArg(argName);
            constructorArguments.put(argName, new ConstructorArgDefinition<>(clazz, defaultValue, null));
            return this;
        }

        @NotNull
        public <FT, TT> Builder<I> constructorArgConvertedOrDefault (@NotNull String argName, @NotNull Class<TT> clazz, @NotNull IConverter<FT, TT> converter, @NotNull TT defaultValue)
        {
            this.validateConstructorArg(argName);
            constructorArguments.put(argName, new ConstructorArgDefinition<>(clazz, defaultValue, converter));
            return this;
        }

        @NotNull
        public Builder<I> constructorArgProperties ()
        {
            constructorArguments.put(PROPERTY_DEFINITION, new ConstructorArgDefinition<>(Item.Properties.class, null, null));
            return this;
        }

        @NotNull
        public <T> Builder<I> property (@NotNull String propertyName, @NotNull Class<T> clazz, @NotNull IPropertyMutator<T> mutator)
        {
            this.validateProperty(propertyName);
            propertyMutators.put(propertyName, new PropertyDefinition<>(clazz, mutator, null, null));
            return this;
        }

        @NotNull
        public <FT, TT> Builder<I> propertyConverted (@NotNull String propertyName, @NotNull Class<TT> clazz, @NotNull IPropertyMutator<TT> mutator, @NotNull IConverter<FT, TT> converter)
        {
            this.validateProperty(propertyName);
            propertyMutators.put(propertyName, new PropertyDefinition<>(clazz, mutator, null, converter));
            return this;
        }

        @NotNull
        public <T> Builder<I> propertyOrDefault (@NotNull String propertyName, @NotNull Class<T> clazz, @NotNull IPropertyMutator<T> mutator, @NotNull T defaultValue)
        {
            this.validateProperty(propertyName);
            propertyMutators.put(propertyName, new PropertyDefinition<>(clazz, mutator, defaultValue, null));
            return this;
        }

        @NotNull
        public <FT, TT> Builder<I> propertyConvertedOrDefault (@NotNull String propertyName, @NotNull Class<TT> clazz, @NotNull IPropertyMutator<TT> mutator, @NotNull IConverter<FT, TT> converter, @NotNull TT defaultValue)
        {
            this.validateProperty(propertyName);
            propertyMutators.put(propertyName, new PropertyDefinition<>(clazz, mutator, defaultValue, converter));
            return this;
        }

        @NotNull
        public ItemGenDefinition<I> build ()
        {
            return new ItemGenDefinition<>(clazz, constructorArguments, propertyMutators);
        }

        private void validateConstructorArg (final String argName)
        {
            if (argName.equals(PROPERTY_DEFINITION))
            {
                throw new IllegalArgumentException(PROPERTY_DEFINITION + " is a reserved magic string and should not be used");
            }
            if (!isConstructorStarted || isConstructorFinished)
            {
                throw new IllegalStateException("Item definition constructor arg cannot be added");
            }
            if (constructorArguments.containsKey(argName))
            {
                throw new IllegalArgumentException("Attempted to redefine constructor argument \"" + argName + "\"");
            }
            if (propertyMutators.containsKey(argName))
            {
                throw new IllegalArgumentException("Constructor arg \"" + argName + " was previously defined as a property");
            }
        }

        private void validateProperty (final String propertyName)
        {
            if (propertyName.equals(PROPERTY_DEFINITION))
            {
                throw new IllegalArgumentException(PROPERTY_DEFINITION + " is a reserved magic string and should not be used");
            }
            if (isConstructorStarted)
            {
                throw new IllegalStateException("Item definition property cannot be defined");
            }
            if (propertyMutators.containsKey(propertyName))
            {
                throw new IllegalArgumentException("Attempted to redefine a property mutator");
            }
            if (constructorArguments.containsKey(propertyName))
            {
                throw new IllegalArgumentException("Property \"" + propertyName + " was previously defined as a constructor arg");
            }
        }
    }

    /**
     * Class responsible for storing a finalized map of generated items.
     * It should be used to register items into the registry.
     *
     * @param <I> Type of item
     */
    public static class Product<I extends Item>
    {
        private final Map<String, Supplier<I>> generatedItems;

        public Product (Map<String, Supplier<I>> generatedItems)
        {
            this.generatedItems = generatedItems;
        }

        public void register (DeferredRegister<Item> register)
        {
            for (var itemEntry : generatedItems.entrySet())
            {
                register.register(itemEntry.getKey(), itemEntry.getValue());
            }
        }
    }
}
