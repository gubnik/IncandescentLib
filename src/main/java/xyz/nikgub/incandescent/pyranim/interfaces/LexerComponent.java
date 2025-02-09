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

package xyz.nikgub.incandescent.pyranim.interfaces;

import xyz.nikgub.incandescent.pyranim.*;

import java.util.regex.Matcher;

/**
 * An interface which declares the class as the component of {@link PyranimLexer}.
 * It should only be implemented for the sake of being handled in
 * {@link xyz.nikgub.incandescent.pyranim.PyranimLexer.LineType}.
 */
@FunctionalInterface
public interface LexerComponent
{
    /**
     * @param parser      {@link PyranimParser} object that will be used for interpolation definitions
     * @param animationIR {@link AnimationIR} callback object
     * @param matcher     {@link Matcher} object provided by
     *                    {@link xyz.nikgub.incandescent.pyranim.PyranimLexer.LineType#handle(PyranimParser, AnimationIR, String)}
     *                    that should be used to access arguments
     * @return {@link xyz.nikgub.incandescent.pyranim.PyranimLexer.State} to transfer the {@link PyranimLexer} into
     * @throws PyranimLexerException handled in {@link PyranimParser#parse(PyranimLoader)}
     * @see xyz.nikgub.incandescent.pyranim.PyranimLexer.LineType#handle(PyranimParser, AnimationIR, String)
     */
    PyranimLexer.State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException;
}
