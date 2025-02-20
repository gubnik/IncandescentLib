package xyz.nikgub.incandescent.itemgen;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nikgub.incandescent.itemgen.interfaces.IConverter;
import xyz.nikgub.incandescent.itemgen.interfaces.IPropertyMutator;

import java.util.NoSuchElementException;

/**
 *
 * @param clazz        {@link Class} of the argument
 * @param mutator      {@link IPropertyMutator} enacted during item generation
 * @param defaultValue {@code T} value that will be used if no value was parsed
 * @param converter    {@link IConverter} used to create convert objects of one type to another
 * @param <FT>         Type of the expected parsed argument
 * @param <TT>         Type of the transformed argument
 */
public record PropertyDefinition<FT, TT>(@NotNull Class<TT> clazz, @NotNull IPropertyMutator<TT> mutator,
                                         @Nullable TT defaultValue, @Nullable IConverter<FT, TT> converter)
{
    @NotNull
    public TT getValue (@Nullable final FT mutatorValue)
    {
        if (mutatorValue == null)
        {
            if (defaultValue != null)
            {
                return defaultValue;
            }
            throw new NoSuchElementException("No value provided for a property");
        }
        if (converter != null)
        {
            return converter.convert(mutatorValue);
        }
        if (clazz.isInstance(mutatorValue))
        {
            return clazz.cast(mutatorValue);
        }
        throw new IllegalArgumentException("Invalid property value: expected " + clazz.getName() + ", got " + mutatorValue.getClass().getName());
    }

    public Item.Properties applyMutator (Item.Properties properties, final String propertyName, final ItemGenObjectInfo objectInfo)
    {
        return mutator().set(properties, this.getValue((FT) objectInfo.getPropertyValues().get(propertyName)));
    }
}
