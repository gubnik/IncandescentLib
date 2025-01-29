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
