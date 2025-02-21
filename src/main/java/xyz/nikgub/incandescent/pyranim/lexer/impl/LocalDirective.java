package xyz.nikgub.incandescent.pyranim.lexer.impl;

import net.minecraft.client.animation.AnimationChannel;
import xyz.nikgub.incandescent.pyranim.exception.PyranimLexerException;
import xyz.nikgub.incandescent.pyranim.lexer.LexerComponent;
import xyz.nikgub.incandescent.pyranim.lexer.PyranimLexer;
import xyz.nikgub.incandescent.pyranim.parser.intrep.AnimationIR;
import xyz.nikgub.incandescent.pyranim.parser.ArgumentPolicy;
import xyz.nikgub.incandescent.pyranim.parser.PyranimParser;

import java.util.Arrays;
import java.util.regex.Matcher;

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
    AT_TIME(">attime", (l, s) -> Float.parseFloat(s)),
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
    public PyranimLexer.State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException
    {
        Object arg = this.argumentPolicy.handle(parser, matcher.group(2));
        switch (animationIR.getCurrentState())
        {
            case PART_HEADER, PART_INSTRUCTION ->
            {
                switch (this)
                {
                    case AT_TIME ->
                    {
                        animationIR.setCurrentTime((Float) arg);
                        return PyranimLexer.State.PART_HEADER;
                    }
                    case INTERPOLATION ->
                    {
                        animationIR.setCurrentInterpolation((AnimationChannel.Interpolation) arg);
                        return PyranimLexer.State.PART_HEADER;
                    }
                }
            }
        }
        throw new PyranimLexerException(animationIR);
    }
}
