package xyz.nikgub.incandescent.pyranim;

/**
 * Unchecked exception thrown by {@link PyranimParser}
 *
 * @see PyranimParserException
 */
public class PyranimParserException extends RuntimeException
{

    /**
     * Wraps the {@link PyranimLexerException} to be thrown in {@link PyranimParser#parse(PyranimLoader)}
     *
     * @param message    {@code String} additional information
     * @param lineNumber {@code int} number of the line that failed to be tokenized
     * @param e          {@link PyranimLexerException} thrown by {@link PyranimLexer.LineType#handle(PyranimParser, AnimationIR, String)}
     */
    public PyranimParserException (String message, int lineNumber, PyranimLexerException e)
    {
        super(".pyranim file cannot be parsed at line " + lineNumber + ": " + message, e);
    }
}

