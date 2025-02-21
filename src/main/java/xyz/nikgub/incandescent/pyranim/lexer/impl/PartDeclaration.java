package xyz.nikgub.incandescent.pyranim.lexer.impl;

import net.minecraft.client.animation.AnimationChannel;
import xyz.nikgub.incandescent.pyranim.parser.AnimationIR;
import xyz.nikgub.incandescent.pyranim.lexer.PyranimLexer;
import xyz.nikgub.incandescent.pyranim.exception.PyranimLexerException;
import xyz.nikgub.incandescent.pyranim.parser.PyranimParser;
import xyz.nikgub.incandescent.pyranim.lexer.LexerComponent;

import java.util.regex.Matcher;

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
    public PyranimLexer.State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException
    {
        switch (animationIR.getCurrentState())
        {
            case GLOBAL_HEADER, PART_INSTRUCTION ->
            {
                animationIR.setCurrentPart(this.value);
                animationIR.setCurrentTime(0);
                animationIR.setCurrentInterpolation(AnimationChannel.Interpolations.LINEAR);
                return PyranimLexer.State.PART_HEADER;
            }
        }
        throw new PyranimLexerException(animationIR);
    }
}
