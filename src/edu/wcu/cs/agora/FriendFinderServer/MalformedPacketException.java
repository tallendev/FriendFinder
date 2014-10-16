package edu.wcu.cs.agora.FriendFinderServer;

import java.io.IOException;

/**
 * Exception in the event of a bad packet.
 */
public class MalformedPacketException extends IOException
{
    /**
     * Default exception constructor. Calls super.
     *
     */
    public MalformedPacketException()
    {
        super();
    }

    public MalformedPacketException(String reason)
    {
        super(reason);
    }
}
