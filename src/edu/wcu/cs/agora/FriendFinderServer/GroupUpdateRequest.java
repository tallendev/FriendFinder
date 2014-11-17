package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONObject;

/**
 * Created by tyler on 11/16/14.
 */
public class GroupUpdateRequest extends Request
{
    public GroupUpdateRequest(JSONObject in, JSONObject out)
    {
        super(in, out);
    }

    @Override
    protected void getResponse()
    {

    }
}
