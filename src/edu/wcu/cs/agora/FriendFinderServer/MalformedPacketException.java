package edu.wcu.cs.agora.FriendFinderServer;

import java.io.IOException;

/**
 * @author Tyler Allen
 * @created 9/29/2014
 * @version 12/8/2014
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

    /**
     * Default exception with message constructor.
     *
     * @param reason The reason this exception was thrown.
     */
    public MalformedPacketException(String reason)
    {
        super(reason);
    }
}
