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

package xyz.nikgub.incandescent.itemgen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nikgub.incandescent.itemgen.interfaces.Converter;

import java.util.NoSuchElementException;

/**
 * Holder record to provide a type-safe way of handling a parsed value from a JSON.
 * This implementation is made specifically with GSON in mind, and it assumes that all
 * numbers are {@link Double} by default.
 *
 * @param clazz        {@link Class} of the argument
 * @param defaultValue {@code T} value that will be used if no value was parsed
 * @param converter    {@link Converter} used to create convert objects of one type to another
 * @param <FT>         Type of the expected parsed argument
 * @param <TT>         Type of the transformed argument
 */
public record ConstructorArgDefinition<FT, TT>(@NotNull Class<TT> clazz, @Nullable TT defaultValue,
                                               @Nullable Converter<FT, TT> converter)
{
    @NotNull
    TT getValue (@Nullable FT constructorArgValue)
    {
        if (constructorArgValue == null)
        {
            if (defaultValue != null)
            {
                return defaultValue;
            }
            throw new NoSuchElementException("No value provided for a constructor argument");
        }
        if (converter != null)
        {
            return converter.convert(constructorArgValue);
        }
        if (clazz.isInstance(constructorArgValue))
        {
            return clazz.cast(constructorArgValue);
        }
        throw new IllegalArgumentException("Invalid property value: expected " + clazz.getName() + ", got " + constructorArgValue.getClass().getName());
    }

    public TT produceArg (final String constructorArgName, final ItemGenObjectInfo objectInfo)
    {
        final Object jsonVal = objectInfo.getPropertyValues().get(constructorArgName);
        return this.getValue((FT) jsonVal);
    }
}
