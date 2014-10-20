package edu.wcu.cs.agora.FriendFinderServer;

import java.io.OutputStream;
import java.util.Scanner;

/**
 * Created by tyler on 10/10/14.
 *
 * TODO: Implement children
 */
public abstract class Request
{
    public static final short DEFAULT = -1;
    public static final short REGISTER = 0;
    public static final short GROUP_UPDATE = 1;
    public static final short PROFILE_UPDATE = 2;
    public static final short SYNC = 3;
    public static final short RESPONSE = 4;

    private Scanner packetInput;

    protected Request(Scanner packetInput)
    {
        this.packetInput = packetInput;
    }

    protected Scanner getPacketInput()
    {
        return packetInput;
    }

    /**
     *
     * FIXME: This will be modified in correspondence to the way SyncAdapter communicates client side.
     *
     *   0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
     * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     * |      Request Type     |      Information...   |
     * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     * |                  Information...               |
     * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *
     * Request Type: A two-byte value indicating the type of request.
     * -1 - Default value.
     * 0  - Register
     * 1  - GroupUpdate
     * 2  - ProfileUpdate
     * 3  - Sync
     * 4  - Response
     * Request Type > 4  || Request Type < 0 - Malformed Packet
     *
     * Information - Information depends on specific request. See specific class documentation.
     *
     * This method is an implementation of the Builder design pattern, allowing an object of the correct type to be
     * returned based on the initial information retrieved from the request.
     *
     * @param in - A wrapper around an input stream attached to the socket.
     *
     */
    protected static Request requestBuilder(Scanner in)
                                            throws MalformedPacketException
    {
        short requestType = DEFAULT;
        Request request = null;
        if (in.hasNextShort())
        {
            requestType = in.nextShort();
        }
        else
        {
            throw new MalformedPacketException("Packet Missing Request Type");
        }
        switch (requestType)
        {
            case (REGISTER):
            {
                break;
            }
            case (GROUP_UPDATE):
            {
                break;
            }
            case (PROFILE_UPDATE):
            {
                break;
            }
            case (SYNC):
            {
                break;
            }
            case (RESPONSE):
            {
                break;
            }
            default:
            {
                throw new MalformedPacketException("Invalid Request Type");
            }
        }
        return request;
    }

    protected abstract void getResponse();
}
