package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

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

    private JSONObject in;
    private JSONObject out;

    /**
     * Constructor for requests.
     * @param in JSON object to get information from.
     * @param out JSON object to provide information.
     */
    protected Request(JSONObject in, JSONObject out)
    {
        this.in = in;
        this.out = out;
    }

    /**
     * Getter for in.
     * @return The input json object.
     */
    protected JSONObject getJsonIn()
    {
        return in;
    }

    /**
     * Getter for out.
     * @return The output json object.
     */
    protected JSONObject getJsonOut()
    {
        return out;
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
    //protected static Request requestBuilder(Scanner in)
    protected static void requestBuilder(JSONObject json, JSONObject jsonOut)
            throws MalformedPacketException, SQLException, JSONException
    {
        //short requestType = DEFAULT;
        String requestType = null;
        Request request = null;
        /*if (in.hasNextShort())
        {
            requestType = in.nextShort();
        }*/
        if (!json.has("request_type"))
        {
            throw new MalformedPacketException("Packet Missing Request Type");
        }
        requestType = json.getString("request_type");

        boolean requestSuccess = Authenticator.getInstance().authenticateUser(json);
        jsonOut.put("authenticated", requestSuccess);
        if (requestSuccess)
        {
            switch (requestType)
            {
                case ("0")://case (REGISTER):
                {
                    break;
                }
                case ("1")://case (GROUP_UPDATE):
                {

                    break;
                }
                case ("2")://case (PROFILE_UPDATE):
                {
                    break;
                }
                case ("3"): //case (SYNC):
                {
                    request = new SyncRequest(json, jsonOut);
                    break;
                }
                case ("4")://case (RESPONSE):
                {
                    break;
                }
                default:
                {
                    throw new MalformedPacketException("Invalid Request Type");
                }
            }
            request.getResponse();
        }
    }

    protected abstract void getResponse() throws SQLException, JSONException;
}
