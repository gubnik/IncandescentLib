package xyz.nikgub.incandescent.pyranim.interfaces;

import xyz.nikgub.incandescent.pyranim.AnimationIR;
import xyz.nikgub.incandescent.pyranim.PyranimLexerException;
import xyz.nikgub.incandescent.pyranim.PyranimParser;

import java.util.regex.Matcher;

@FunctionalInterface
public interface LexerComponent
{
    PyranimParser.State handle (PyranimParser parser, AnimationIR animationIR, Matcher matcher) throws PyranimLexerException;
}
