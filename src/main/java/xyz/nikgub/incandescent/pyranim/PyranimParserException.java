package xyz.nikgub.incandescent.pyranim;

public class PyranimParserException extends RuntimeException
{
    public PyranimParserException ()
    {
        super();
    }

    public PyranimParserException (String message)
    {
        super(message);
    }

    public PyranimParserException (String message, Throwable cause)
    {
        super(message, cause);
    }

    public PyranimParserException (Throwable cause)
    {
        super(cause);
    }

    protected PyranimParserException (String message, Throwable cause,
                                      boolean enableSuppression,
                                      boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PyranimParserException (PyranimParser parser, int lineNumber)
    {
        super("File " + parser.getLoader().getFilename() + " cannot be parsed at line " + lineNumber);
    }

    public PyranimParserException (PyranimParser parser, int lineNumber, String message)
    {
        super("File " + parser.getLoader().getFilename() + " cannot be parsed at line " + lineNumber + ": " + message);
    }
}

