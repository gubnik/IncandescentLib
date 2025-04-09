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

package xyz.nikgub.incandescent.pyranim.lexer;

import xyz.nikgub.incandescent.pyranim.exception.PyranimLexerException;
import xyz.nikgub.incandescent.pyranim.lexer.impl.GlobalDirective;
import xyz.nikgub.incandescent.pyranim.lexer.impl.Instruction;
import xyz.nikgub.incandescent.pyranim.lexer.impl.LocalDirective;
import xyz.nikgub.incandescent.pyranim.lexer.impl.PartDeclaration;
import xyz.nikgub.incandescent.pyranim.parser.PyranimParser;
import xyz.nikgub.incandescent.pyranim.parser.intrep.AnimationIR;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class responsible for defining the syntax and parsing rules for the `{@code .pyranim} format.
 * It provides regular expressions for matching various components of the format,
 * including global directives, local directives, part declarations, and transformation instructions.
 *
 * <p>This class also includes enums and interfaces that define how to handle each
 * component during the parsing process.</p>
 *
 * @author Nikolay Gubankov (aka nikgub)
 * @see PyranimParser
 */
public final class PyranimLexer
{
    public static final String EMPTY_LINE_REGEX = MessageFormat.format("{0}{1}",
        "[\t ]*",
        "(;.*)?"
    );

    /*
    Groups:
    1 - directive (string)
    2 - value (float or string, optional)
     */
    public static final String GLOBAL_DIRECTIVE_DECLARATION_REGEX = MessageFormat.format("{0}{1}{2}({3}|{4}){5}{6}",
        "[\t ]*",
        "([.][a-zA-Z]+)",
        "[\t ]+",
        "([-+]?([0-9]+([.][0-9]*)?|[.][0-9]+)([eE][-+]?[0-9]+)?)?",
        "(\"[a-zA-Z]+\")",
        "[\t ]*",
        "(;.*)?"
    );

    /*
    Groups:
    1 - directive (string)
    2 - value (float or string, optional)
     */
    public static final String LOCAL_DIRECTIVE_DECLARATION_REGEX = MessageFormat.format("{0}{1}{2}({3}|{4}){5}{6}",
        "[\t ]*",
        "(>[a-zA-Z]+)",
        "[\t ]+",
        "([-+]?([0-9]+([.][0-9]*)?|[.][0-9]+)([eE][-+]?[0-9]+)?)?",
        "(\"[a-zA-Z]+\")",
        "[\t ]*",
        "(;.*)?"
    );

    public static final String PART_DECLARATION_REGEX = MessageFormat.format("{0}{1}:{2}{3}",
        "[\t ]*",
        "([_a-zA-Z0-9]+)",
        "[\t ]*",
        "(;.*)?"
    );


    /*
    Groups:
    1 - instruction (string)
    2 - x val (float)
    5 - y val (float)
    8 - z val (float)
     */
    public static final String INSTRUCTION_LINE_REGEX = MessageFormat.format("{0}{1}{2}{3}{0},{0}{3},{0}{3}{0}{4}",
        "[\t ]*",
        "(mov|rot|scl)",
        "[\t ]+",
        "([-+]?[0-9]+([.][0-9]*)?|[.][0-9]+)([eE][-+]?[0-9]+)?",
        "(;.*)?");

    public enum LineType
    {
        GLOBAL_DIRECTIVE(GlobalDirective::match, GLOBAL_DIRECTIVE_DECLARATION_REGEX),
        LOCAL_DIRECTIVE(LocalDirective::match, LOCAL_DIRECTIVE_DECLARATION_REGEX),
        PART_DECLARATION(PartDeclaration::match, PART_DECLARATION_REGEX),
        PART_INSTRUCTION(Instruction::match, INSTRUCTION_LINE_REGEX),
        IGNORE(s -> null, EMPTY_LINE_REGEX),
        WRONG(s -> null, "^$");

        private final Pattern pattern;
        private final ComponentProvider componentProvider;

        LineType (ComponentProvider componentProvider, String s)
        {
            pattern = Pattern.compile(s);
            this.componentProvider = componentProvider;
        }

        public static LineType match (String line)
        {
            return Arrays.stream(values()).filter(lt -> lt.pattern.matcher(line).matches()).findFirst().orElse(WRONG);
        }

        /**
         * Method that handles state transition and handling policy for a certain {@link LineType}.
         * It uses {@link Matcher} to match against {@link #pattern} and transfers down the handling to
         * {@link LexerComponent#handle(PyranimParser, AnimationIR, Matcher)}.
         *
         * @param parser      {@link PyranimParser} object that will be used for interpolation definitions
         * @param animationIR {@link AnimationIR} callback object
         * @param line        {@code String} raw line of the {@code .pyranim} file
         * @return {@link State} to transition to
         * @throws PyranimLexerException if the line is malformed or could not be handled
         */
        public State handle (PyranimParser parser, AnimationIR animationIR, String line) throws PyranimLexerException
        {
            Matcher matcher = pattern.matcher(line);
            if (!matcher.matches())
            {
                throw new PyranimLexerException(animationIR, "Malformed line could not be matched");
            }
            LexerComponent component = this.componentProvider.get(matcher.group(1));
            if (component == null)
            {
                return animationIR.getCurrentState();
            }
            return component.handle(parser, animationIR, matcher);
        }
    }

    /**
     * Represents the different states of the lexer during the
     * parsing process. It indicates whether the lexer is in the global header, part
     * header, or part instruction state.
     */
    public enum State
    {
        GLOBAL_HEADER,
        PART_HEADER,
        PART_INSTRUCTION
    }
}
