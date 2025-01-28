package xyz.nikgub.incandescent.pyranim;

import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * <p>Pyranim format, assembly-like animation representation</p>
 *
 * Consists of 3 sections:
 * <ul>
 *     <li>Header</li>
 *     <li>Part's header</li>
 *     <li>Part's body</li>
 * </ul>
 *
 */
public final class PyranimFormat
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
    public static final String DIRECTIVE_DECLARATION_REGEX = MessageFormat.format("{0}{1}{2}({3}|{4}){5}{6}",
        "[\t ]*",
        "([.][a-zA-Z]+)",
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
        "[-+]?([0-9]+([.][0-9]*)?|[.][0-9]+)([eE][-+]?[0-9]+)?",
        "(;.*)?");

    enum LineType
    {
        DIRECTIVE(DIRECTIVE_DECLARATION_REGEX),
        PART(PART_DECLARATION_REGEX),
        INSTRUCTION(INSTRUCTION_LINE_REGEX),
        COMMENT(EMPTY_LINE_REGEX),
        WRONG("^$");

        private final String regex;

        LineType (String s)
        {
            regex = s;
        }

        public static LineType match (String line)
        {
            return Arrays.stream(values()).filter(lt -> line.matches(lt.regex)).findFirst().orElse(WRONG);
        }
    }

    enum Directive
    {
        DURATION(".drtion", PyranimParser.State.HEADER, (l, s) -> Float.parseFloat(s)),
        LOOPING(".doloop", PyranimParser.State.HEADER, (l, s) -> null),
        AT(".at", PyranimParser.State.PART_HEADER, (l, s) -> Float.parseFloat(s)),
        INTERPOLATION(".intrpl", PyranimParser.State.PART_HEADER, PyranimParser::getInterpolation);

        private final String representation;
        private final PyranimParser.State appropriateState;
        private final ArgumentPolicy<?> argumentPolicy;

        Directive (String representation, PyranimParser.State appropriateState, ArgumentPolicy<?> argumentPolicy)
        {
            this.representation = representation;
            this.appropriateState = appropriateState;
            this.argumentPolicy = argumentPolicy;
        }

        public String getRepresentation ()
        {
            return representation;
        }

        public PyranimParser.State getAppropriateState ()
        {
            return appropriateState;
        }

        public static Directive match (final String rep)
        {
            return Arrays.stream(values()).filter(i -> i.representation.equals(rep)).findFirst().orElse(null);
        }

        public ArgumentPolicy<?> getArgumentPolicy ()
        {
            return argumentPolicy;
        }

        @FunctionalInterface
        interface ArgumentPolicy<T>
        {
            @Nullable
            T handle (PyranimParser lexer, String arg);
        }
    }

    enum Instruction
    {
        MOVE("mov"),
        ROTATE("rot"),
        SCALE("scl");

        private final String representation;

        Instruction (String s)
        {
            representation = s;
        }

        public static Instruction translate (final String rep)
        {
            return Arrays.stream(values()).filter(i -> i.representation.equals(rep)).findFirst().orElse(null);
        }
    }
}
