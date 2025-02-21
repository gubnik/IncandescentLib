package xyz.nikgub.incandescent.pyranim.lexer.impl;

import xyz.nikgub.incandescent.pyranim.exception.PyranimLexerException;
import xyz.nikgub.incandescent.pyranim.lexer.LexerComponent;
import xyz.nikgub.incandescent.pyranim.lexer.PyranimLexer;
import xyz.nikgub.incandescent.pyranim.parser.intrep.AnimationIR;
import xyz.nikgub.incandescent.pyranim.parser.ArgumentPolicy;
import xyz.nikgub.incandescent.pyranim.parser.PyranimParser;

import java.util.Arrays;
import java.util.regex.Matcher;

/**
 * Defines the global directives that can be used
 * in the {@code .pyranim} format. Each directive has a representation and a policy for
 * handling the provided argument.
 *
 * <p>
 *     If the argument was provided for a directive that does not take one, it will be swallowed and ignored.
 * </p>
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
    public PyranimLexer.State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException
    {
        Object arg = this.argumentPolicy.handle(parser, matcher.group(2));
        if (animationIR.getCurrentState() == PyranimLexer.State.GLOBAL_HEADER)
        {
            switch (this)
            {
                case DURATION ->
                {
                    animationIR.setLength((Float) arg);
                    return PyranimLexer.State.GLOBAL_HEADER;
                }
                case LOOPING ->
                {
                    animationIR.setDoLoop(true);
                    return PyranimLexer.State.GLOBAL_HEADER;
                }
            }
        }
        throw new PyranimLexerException(animationIR);
    }
}