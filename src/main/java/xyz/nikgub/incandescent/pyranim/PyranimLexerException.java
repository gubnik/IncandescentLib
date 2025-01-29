package xyz.nikgub.incandescent.pyranim;

/**
 * Checked exception thrown by {@link PyranimLexer}
 *
 * @see PyranimLexer
 */
public class PyranimLexerException extends Exception
{
    public PyranimLexerException (AnimationIR animationIR)
    {
        super(".pyranim lexing failed at part: " + animationIR.getCurrentPart() +
            ". Last state: " + animationIR.getCurrentState() +
            ". Last time: " + animationIR.getCurrentTime() +
            ". Last interpolation: " + animationIR.getCurrentInterpolation());
    }

    public PyranimLexerException (AnimationIR animationIR, String message)
    {
        super(".pyranim lexing failed at part: " + animationIR.getCurrentPart() +
            ". Last state: " + animationIR.getCurrentState() +
            ". Last time: " + animationIR.getCurrentTime() +
            ". Last interpolation: " + animationIR.getCurrentInterpolation() +
            ". Additional info: " + message);
    }

    public PyranimLexerException (AnimationIR animationIR, Exception e)
    {
        super(".pyranim lexing failed at part: " + animationIR.getCurrentPart() +
            ". Last state: " + animationIR.getCurrentState() +
            ". Last time: " + animationIR.getCurrentTime() +
            ". Last interpolation: " + animationIR.getCurrentInterpolation() +
            ". Additional info: ", e);
    }
}
