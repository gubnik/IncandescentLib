package xyz.nikgub.incandescent.network;

/**
 * Exception thrown if the {@link IncandescentPacket} is not properly formed
 *
 * @see IncandescentNetworkCore
 */
public class IllformedPacketException extends RuntimeException
{
    public IllformedPacketException (String message)
    {
        super(message);
    }

    public IllformedPacketException (String message, Throwable cause)
    {
        super(message, cause);
    }
}
