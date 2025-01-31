package xyz.nikgub.incandescent.pyranim;

import net.minecraft.client.animation.AnimationChannel;
import xyz.nikgub.incandescent.pyranim.interfaces.ArgumentPolicy;
import xyz.nikgub.incandescent.pyranim.interfaces.ComponentProvider;
import xyz.nikgub.incandescent.pyranim.interfaces.LexerComponent;

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
 * @see PyranimParser
 *
 * @author Nikolay Gubankov (aka nikgub)
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
        "[-+]?([0-9]+([.][0-9]*)?|[.][0-9]+)([eE][-+]?[0-9]+)?",
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

    /**
     * Defines the global directives that can be used
     * in the {@code .pyranim} format. Each directive has a representation and a policy for
     * handling the provided argument.
     *
     * <p>If the argument was provided for a directive that does not take one, it will be
     * swallowed and ignored.</p>
     *
     * @see LexerComponent
     */
    public enum GlobalDirective implements LexerComponent
    {
        DURATION(".drtion", (l, s) -> Float.parseFloat(s)),
        LOOPING(".doloop", (l, s) -> null);

        private final String representation;
        private final ArgumentPolicy<?> argumentPolicy;

        GlobalDirective (String representation, ArgumentPolicy<?> argumentPolicy)
        {
            this.representation = representation;
            this.argumentPolicy = argumentPolicy;
        }

        public static GlobalDirective match (final String rep)
        {
            return Arrays.stream(values()).filter(i -> i.representation.equals(rep)).findFirst().orElse(null);
        }

        @Override
        public State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException
        {
            Object arg = this.argumentPolicy.handle(parser, matcher.group(2));
            if (animationIR.getCurrentState() == State.GLOBAL_HEADER)
            {
                switch (this)
                {
                    case DURATION ->
                    {
                        animationIR.setLength((Float) arg);
                        return State.GLOBAL_HEADER;
                    }
                    case LOOPING ->
                    {
                        animationIR.setDoLoop(true);
                        return State.GLOBAL_HEADER;
                    }
                }
            }
            throw new PyranimLexerException(animationIR);
        }
    }

    /**
     * Defines the local directives that can be used
     * in the {@code .pyranim} format. Each directive has a representation and a policy for
     * handling the provided argument.
     *
     * <p>If the argument was provided for a directive that does not take one, it will be
     * swallowed and ignored.</p>
     *
     * @see LexerComponent
     */
    public enum LocalDirective implements LexerComponent
    {
        ATTIMESTAMP(">attime", (l, s) -> Float.parseFloat(s)),
        INTERPOLATION(">intrpl", PyranimParser::getInterpolation);

        private final String representation;
        private final ArgumentPolicy<?> argumentPolicy;

        LocalDirective (String representation, ArgumentPolicy<?> argumentPolicy)
        {
            this.representation = representation;
            this.argumentPolicy = argumentPolicy;
        }

        public static LocalDirective match (final String rep)
        {
            return Arrays.stream(values()).filter(i -> i.representation.equals(rep)).findFirst().orElse(null);
        }

        @Override
        public State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException
        {
            Object arg = this.argumentPolicy.handle(parser, matcher.group(2));
            switch (animationIR.getCurrentState())
            {
                case PART_HEADER, PART_INSTRUCTION ->
                {
                    switch (this)
                    {
                        case ATTIMESTAMP ->
                        {
                            animationIR.setCurrentTime((Float) arg);
                            return State.PART_HEADER;
                        }
                        case INTERPOLATION ->
                        {
                            animationIR.setCurrentInterpolation((AnimationChannel.Interpolation) arg);
                            return State.PART_HEADER;
                        }
                    }
                }
            }
            throw new PyranimLexerException(animationIR);
        }
    }

    /**
     * The class representing a part declaration in the {@code .pyranim} format.
     */
    public record PartDeclaration(String value) implements LexerComponent
    {
        public static PartDeclaration match (final String rep)
        {
            return new PartDeclaration(rep);
        }

        @Override
        public State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException
        {
            switch (animationIR.getCurrentState())
            {
                case GLOBAL_HEADER, PART_INSTRUCTION ->
                {
                    animationIR.setCurrentPart(this.value);
                    return State.PART_HEADER;
                }
            }
            throw new PyranimLexerException(animationIR);
        }
    }

    /**
     * The {@link  Instruction} enum defines the transformation instructions that can be
     * used in the {@code .pyranim} format. Each instruction has a representation and a target
     * for the animation channel.
     */
    public enum Instruction implements LexerComponent
    {
        MOVE("mov", AnimationChannel.Targets.POSITION),
        ROTATE("rot", AnimationChannel.Targets.ROTATION),
        SCALE("scl", AnimationChannel.Targets.SCALE);

        private final String representation;
        private final AnimationChannel.Target animationTarget;

        Instruction (String s, AnimationChannel.Target animationTarget)
        {
            representation = s;
            this.animationTarget = animationTarget;
        }

        public static Instruction match (final String rep)
        {
            return Arrays.stream(values()).filter(i -> i.representation.equals(rep)).findFirst().orElse(null);
        }

        public AnimationChannel.Target getAnimationTarget ()
        {
            return animationTarget;
        }

        @Override
        public State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException
        {
            float xVal, yVal, zVal;
            try
            {
                xVal = Float.parseFloat(matcher.group(2));
                yVal = Float.parseFloat(matcher.group(5));
                zVal = Float.parseFloat(matcher.group(8));
            } catch (NumberFormatException | NullPointerException e)
            {
                throw new PyranimLexerException(animationIR, e);
            }
            switch (animationIR.getCurrentState())
            {
                case PART_HEADER, PART_INSTRUCTION ->
                {
                    animationIR.addKeyframe(new KeyframeIR(this, xVal, yVal, zVal, animationIR.getCurrentInterpolation()));
                    return State.PART_INSTRUCTION;
                }
            }
            throw new PyranimLexerException(animationIR);
        }
    }
}
