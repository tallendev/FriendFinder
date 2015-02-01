package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONObject;

/**
 * @author Tyler Allen
 * @created 11/16/14
 * @version 12/8/2014
 *
 * Request type for a group update. Not in use yet.
 */
public class GroupUpdateRequest extends Request
{


    /**
     * Default constructor to super.
     *
     * @param in Input JSON object.
     * @param out Output JSON object.
     */
    public GroupUpdateRequest(JSONObject in, JSONObject out)
    {
        super(in, out);

    }

    /**
     * Not implemented yet.
     */
    @Override
    protected void getResponse()
    {
        JSONObject in = getJsonIn();
        JSONObject out = getJsonOut();
    }
}
