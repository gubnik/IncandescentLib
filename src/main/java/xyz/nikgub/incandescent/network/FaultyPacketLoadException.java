package xyz.nikgub.incandescent.network;

/**
 * Exception thrown if the {@link IncandescentPacket} failed to load
 *
 * @see IncandescentNetwork
 */
public class FaultyPacketLoadException extends RuntimeException
{
    public FaultyPacketLoadException ()
    {
        super();
    }

    public FaultyPacketLoadException (String message)
    {
        super(message);
    }

    public FaultyPacketLoadException (String message, Throwable cause)
    {
        super(message, cause);
    }

    public FaultyPacketLoadException (Throwable cause)
    {
        super(cause);
    }

    protected FaultyPacketLoadException (String message, Throwable cause,
                                         boolean enableSuppression,
                                         boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
