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

package xyz.nikgub.incandescent.pyranim;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * The {@code PyranimParser} class is responsible for parsing animations defined in the
 * {@code .pyranim} format, which represents Minecraft's {@link AnimationDefinition} in a
 * human-readable and easily generated format.
 *
 * <p>This parser can be reused for multiple {@code .pyranim} files and allows for user-defined
 * interpolations through its builder.</p>
 *
 * <p>Animations are parsed into Minecraft's {@link AnimationDefinition} using the
 * {@link PyranimParser#parse(PyranimLoader)} method.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * PyranimParser parser = new PyranimParser.Builder().build();
 * AnimationDefinition animation = parser.parse(new PyranimLoader("example.pyranim"));
 * }
 * </pre>
 *
 * @author Nikolay Gubankov (aka nikgub)
 * @see PyranimLoader
 * @see AnimationDefinition
 * @see PyranimLexer
 */
public class PyranimParser
{
    private final Map<String, AnimationChannel.Interpolation> interpolationMap;

    /**
     * Constructs the {@link PyranimParser} object. Used by {@link Builder#build()}.
     *
     * @param interpolationMap Mapped {@link net.minecraft.client.animation.AnimationChannel.Interpolation} objects
     */
    private PyranimParser (Map<String, AnimationChannel.Interpolation> interpolationMap)
    {
        this.interpolationMap = interpolationMap;
    }

    /**
     * Gets the {@link net.minecraft.client.animation.AnimationChannel.Interpolation} from {@link #interpolationMap}.
     * Should only be used in the context of {@link PyranimLexer} since it trims of any {@code "} symbols.
     * that remain after lexing.
     *
     * @param s {@code String} name of the interpolation
     * @return {@link net.minecraft.client.animation.AnimationChannel.Interpolation} from {@link #interpolationMap}
     */
    @NotNull
    public AnimationChannel.Interpolation getInterpolation (String s)
    {
        final String toSearch = s.replace("\"", "");
        var val = interpolationMap.get(toSearch);
        if (val == null)
        {
            throw new IllegalArgumentException("Undefined interpolation \"" + toSearch + "\"");
        }
        return val;
    }

    /**
     * Parses the provided {@link PyranimLoader} and converts its contents into an
     * {@link AnimationDefinition}.
     *
     * @param loader the {@link PyranimLoader} containing the contents of the
     *               {@code parse} file to be parsed
     * @return {@link AnimationDefinition} representing the parsed animation
     * @throws PyranimParserException if an error occurs during parsing, including
     *                                issues with line placement or syntax errors
     * @see PyranimLexer
     */
    private AnimationDefinition parse (@NotNull PyranimLoader loader)
    {
        final AnimationIR animationIR = new AnimationIR();
        final Queue<String> lines = loader.getLines();
        int i = 0;
        while (!lines.isEmpty())
        {
            i++;
            final String line = lines.poll();
            final PyranimLexer.LineType lineType = PyranimLexer.LineType.match(line);
            try
            {
                animationIR.setCurrentState(lineType.handle(this, animationIR, line));
            } catch (PyranimLexerException e)
            {
                throw new PyranimParserException("Lexing failed at line of type: " + lineType, i, e);
            }
        }
        final AnimationDefinition.Builder builder = animationIR.bakeIntoBuilder();
        return builder.build();
    }

    public AnimationDefinition parse (@NotNull String fileLocation)
    {
        return this.parse(new PyranimLoader(fileLocation));
    }

    /**
     * A builder class for constructing instances of {@link  PyranimParser}.
     *
     * <p>This builder allows for the configuration of user-defined interpolations
     * before creating a {@link  PyranimParser} instance.</p>
     */
    public static class Builder
    {
        private final Map<String, AnimationChannel.Interpolation> interpolationMap = new HashMap<>(
            Map.of(
                "catmullrom", AnimationChannel.Interpolations.CATMULLROM,
                "linear", AnimationChannel.Interpolations.LINEAR
            )
        );

        /**
         * Defines an interpolation to be recognized by the parser
         *
         * @param name          {@code String} name of the interpolation used in {@code .pyranim} file
         * @param interpolation {@link AnimationChannel.Interpolation}
         * @return {@link Builder}
         */
        public Builder defineInterpolation (String name, AnimationChannel.Interpolation interpolation)
        {
            if (interpolationMap.putIfAbsent(name, interpolation) != null)
            {
                throw new IllegalArgumentException("Interpolation '" + name + "' cannot be redefined");
            }
            return this;
        }

        /**
         * Constructs a new {@link PyranimParser} instance with the specified configurations.
         *
         * @return a new instance of {@link PyranimParser}
         */
        public PyranimParser build ()
        {
            return new PyranimParser(interpolationMap);
        }
    }
}