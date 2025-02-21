package xyz.nikgub.incandescent.pyranim.lexer.impl;

import net.minecraft.client.animation.AnimationChannel;
import xyz.nikgub.incandescent.pyranim.exception.PyranimLexerException;
import xyz.nikgub.incandescent.pyranim.lexer.LexerComponent;
import xyz.nikgub.incandescent.pyranim.lexer.PyranimLexer;
import xyz.nikgub.incandescent.pyranim.parser.intrep.AnimationIR;
import xyz.nikgub.incandescent.pyranim.parser.intrep.KeyframeIR;
import xyz.nikgub.incandescent.pyranim.parser.PyranimParser;

import java.util.Arrays;
import java.util.regex.Matcher;

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
    public PyranimLexer.State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException
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
                return PyranimLexer.State.PART_INSTRUCTION;
            }
        }
        throw new PyranimLexerException(animationIR);
    }
}