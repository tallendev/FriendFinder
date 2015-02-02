package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * @author Tyler Allen
 * @created 10/10/14
 * @version 12/8/2014
 *
 * TODO: Implement children
 */
public abstract class Request
{
    /**
     * Default request type.
     */
    public static final short DEFAULT        = -1;
    /**
     * Request type to indicate a registration request.
     */
    public static final short REGISTER       = 0;
    /**
     * Request type indicating a group update.
     */
    public static final short GROUP_UPDATE   = 1;
    /**
     * Request type indicating a user's profile update.
     */
    public static final short PROFILE_UPDATE = 2;
    /**
     * Request type indicating a synchronization.
     */
    public static final short SYNC           = 3;
    /**
     * Request type indicating some kind of secondary response from the client.
     */
    public static final short RESPONSE       = 4;
    /**
     * Request type indicating an update to an event.
     */
    public static final short EVENT_UPDATE = 5;

    /**
     * JSON object received from the client.
     */
    private JSONObject in;
    /** JSON object in preparation to be sent to the client.*/
    private JSONObject out;

    /**
     * Constructor for requests.
     * @param in JSON object to get information from.
     * @param out JSON object to provide information.
     */
    protected Request (JSONObject in, JSONObject out)
    {
        this.in = in;
        this.out = out;
    }

    /**
     * Request Type: A value indicating the type of request. -1 - Default value. 0  - Register 1  -
     * GroupUpdate 2  - ProfileUpdate 3  - Sync 4  - Response Request Type > 4  || Request Type < 0
     * - Malformed Packet
     * <p>
     * Information - Information depends on specific request. See specific class documentation.
     * <p>
     * This method is an implementation of the Builder design pattern, allowing an object of the
     * correct type to be returned based on the initial information retrieved from the request.
     * <p>
     * TODO: This method is under heavy modification as features are added, stubbed out code is
     * generally to be used in the future.
     */
    protected static Request requestBuilder (JSONObject json, JSONObject jsonOut)
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
        boolean requestSuccess = true;
        if (!requestType.equals("0"))
        {
            requestSuccess = Authenticator.getInstance().authenticateUser(json);
            jsonOut.put("authenticated", requestSuccess);
        }
        if (requestSuccess)
        {
            System.err.println("RequestType: " + requestType);
            request = requestTypeHelper(requestType, json, jsonOut);
        }
        return request;
    }

    /**
     * Contains building of object.
     * @param requestType The type of request we are making.
     */
    private static Request requestTypeHelper (String requestType, JSONObject json,
                                              JSONObject jsonOut) throws MalformedPacketException
    {
        Request request = null;
        switch (requestType)
        {
            case ("0")://case (REGISTER):
            {
                System.err.println("New register request");
                request = new RegistrationRequest(json, jsonOut);
                break;
            }
            case ("1")://case (GROUP_UPDATE):
            {
                System.err.println("new group update");
                request = new GroupUpdateRequest(json, jsonOut);
                break;
            }
            case ("2")://case (PROFILE_UPDATE):
            {
                break;
            }
            case ("3"): //case (SYNC):
            {
                System.err.println("new SyncRequest");
                request = new SyncRequest(json, jsonOut);
                break;
            }
            case ("4")://case (RESPONSE):
            {
                break;
            }
            case ("5")://case (RESPONSE):
            {
                System.err.println("new event update");
                request = new EventUpdateRequest(json, jsonOut);
                break;
            }
            default:
            {
                throw new MalformedPacketException("Invalid Request Type");
            }
        }
        return request;
    }

    /**
     * Getter for in.
     * @return The input json object.
     */
    protected JSONObject getJsonIn ()
    {
        return in;
    }

    /**
     * Getter for out.
     * @return The output json object.
     */
    protected JSONObject getJsonOut ()
    {
        return out;
    }

    /**
     * This method is the workhorse of the Request type. Executes the primary functionality of
     * that object.
     * @throws SQLException An error connecting to the database or with a database query has been
     *                      encountered.
     * @throws JSONException An error with a JSON object has been encountered.
     */
    protected abstract void getResponse() throws SQLException, JSONException;
}
