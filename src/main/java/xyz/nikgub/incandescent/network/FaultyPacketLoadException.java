package xyz.nikgub.incandescent.network;

/**
 * Exception thrown if the {@link IncandescentPacket} failed to load
 *
 * @see IncandescentNetwork
 */
public class FaultyPacketLoadException extends RuntimeException
{
    public FaultyPacketLoadException (String message)
    {
        super(message);
    }

    public FaultyPacketLoadException (String message, Throwable cause)
    {
        super(message, cause);
    }
}
