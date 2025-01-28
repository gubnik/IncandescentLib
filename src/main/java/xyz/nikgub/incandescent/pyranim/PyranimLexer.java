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
 * <p>Pyranim format, assembly-like animation representation</p>
 * <p>
 * Consists of 3 sections:
 * <ul>
 *     <li>Header</li>
 *     <li>Part's header</li>
 *     <li>Part's body</li>
 * </ul>
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

        public PyranimParser.State handle (PyranimParser parser, AnimationIR animationIR, PyranimParser.LineContext context) throws PyranimLexerException
        {
            Matcher matcher = pattern.matcher(context.line());
            if (!matcher.matches())
            {
                throw new PyranimLexerException(animationIR);
            }
            LexerComponent component = this.componentProvider.get(matcher.group(1));
            if (component == null)
            {
                return animationIR.getCurrentState();
            }
            return component.handle(parser, animationIR, matcher);
        }
    }

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
        public PyranimParser.State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException
        {
            Object arg = this.argumentPolicy.handle(parser, matcher.group(2));
            if (animationIR.getCurrentState() == PyranimParser.State.GLOBAL_HEADER)
            {
                switch (this)
                {
                    case DURATION ->
                    {
                        animationIR.setLength((Float) arg);
                        return PyranimParser.State.GLOBAL_HEADER;
                    }
                    case LOOPING ->
                    {
                        animationIR.setDoLoop(true);
                        return PyranimParser.State.GLOBAL_HEADER;
                    }
                }
            }
            throw new PyranimLexerException(animationIR);
        }
    }

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
        public PyranimParser.State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException
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
                            return PyranimParser.State.PART_HEADER;
                        }
                        case INTERPOLATION ->
                        {
                            animationIR.setCurrentInterpolation((AnimationChannel.Interpolation) arg);
                            return PyranimParser.State.PART_HEADER;
                        }
                    }
                }
            }
            throw new PyranimLexerException(animationIR);
        }
    }

    public record PartDeclaration(String value) implements LexerComponent
    {
        public static PartDeclaration match (final String rep)
        {
            return new PartDeclaration(rep);
        }

        @Override
        public PyranimParser.State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException
        {
            switch (animationIR.getCurrentState())
            {
                case GLOBAL_HEADER, PART_INSTRUCTION ->
                {
                    animationIR.setCurrentPart(this.value);
                    return PyranimParser.State.PART_HEADER;
                }
            }
            throw new PyranimLexerException(animationIR);
        }
    }

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
        public PyranimParser.State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException
        {
            float xVal = Float.parseFloat(matcher.group(2));
            float yVal = Float.parseFloat(matcher.group(5));
            float zVal = Float.parseFloat(matcher.group(8));
            switch (animationIR.getCurrentState())
            {
                case PART_HEADER, PART_INSTRUCTION ->
                {
                    animationIR.addKeyframe(new KeyframeIR(this, xVal, yVal, zVal, animationIR.getCurrentInterpolation()));
                    return PyranimParser.State.PART_INSTRUCTION;
                }
            }
            throw new PyranimLexerException(animationIR);
        }
    }
}
