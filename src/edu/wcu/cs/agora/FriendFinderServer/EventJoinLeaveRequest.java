package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Tyler Allen
 * @version 2/16/2015
 *          <p>
 *          Request type for a leaving or joining a group.
 * @created 2/16/2015
 */
public class EventJoinLeaveRequest extends Request
{


    /**
     * Default constructor to super.
     *
     * @param in Input JSON object.
     * @param out Output JSON object.
     */
    public EventJoinLeaveRequest (JSONObject in, JSONObject out)
    {
        super(in, out);
    }

    /**
     * Creates/updates groups based on user supplied information.
     */
    @Override
    protected void getResponse () throws SQLException, JSONException
    {
        JSONObject in = getJsonIn();
        JSONObject out = getJsonOut();
        Connection conn = DatabaseConnectionBuilder.buildDatabaseConnection();
        boolean success = true;
        PreparedStatement stmt;
        String sql;
        if (in.getBoolean("joining"))
        {
            sql = "INSERT into friendfinder.attending_event " + "VALUES (?,?)";

        }
        else
        {
            sql = "DELETE FROM friendfinder.attending_event " + "WHERE attendee = ? and event = ?";
        }
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, in.getString("user"));
        stmt.setInt(2, Integer.parseInt(in.getString("id")));
        stmt.executeUpdate();

        out.put("success", success);
    }
}
