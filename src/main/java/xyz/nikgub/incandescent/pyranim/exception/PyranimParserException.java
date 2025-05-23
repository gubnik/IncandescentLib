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

package xyz.nikgub.incandescent.pyranim.exception;

import xyz.nikgub.incandescent.pyranim.lexer.PyranimLexer;
import xyz.nikgub.incandescent.pyranim.parser.intrep.AnimationIR;
import xyz.nikgub.incandescent.pyranim.parser.PyranimParser;

/**
 * Unchecked exception thrown by {@link PyranimParser}
 *
 * @see PyranimParserException
 */
public class PyranimParserException extends RuntimeException
{

    /**
     * Wraps the {@link PyranimLexerException} to be thrown in {@link PyranimParser#parse(String)}
     *
     * @param message    {@code String} additional information
     * @param lineNumber {@code int} number of the line that failed to be tokenized
     * @param e          {@link PyranimLexerException} thrown by {@link PyranimLexer.LineType#handle(PyranimParser, AnimationIR, String)}
     */
    public PyranimParserException (String message, int lineNumber, PyranimLexerException e)
    {
        super(".pyranim file cannot be parsed at line " + lineNumber + ": " + message, e);
    }
}

