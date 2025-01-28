package xyz.nikgub.incandescent.pyranim;

public class PyranimParserException extends RuntimeException
{
    public PyranimParserException ()
    {
        super();
    }

    public PyranimParserException (String message)
    {
        super(".pyranim file cannot be parsed: " + message);
    }

    public PyranimParserException (String message, int lineNumber)
    {
        super(".pyranim file cannot be parsed at line " + lineNumber + ": " + message);
    }

    public PyranimParserException (String message, int lineNumber, Throwable e)
    {
        super(".pyranim file cannot be parsed at line " + lineNumber + ": " + message, e);
    }

    public PyranimParserException (PyranimParser parser, int lineNumber, String message)
    {
        super("File cannot be parsed at line at line " + lineNumber + ": " + message);
    }
}

