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
        System.err.println("EventJoinLeaveRequest:\n" + in.toString());
        Connection conn = DatabaseConnectionBuilder.buildDatabaseConnection();
        boolean success = true;
        PreparedStatement stmt;
        String sql1;
        if (in.getBoolean("joining"))
        {
            sql1 = "INSERT INTO friendfinder.attending_event " + "VALUES (?,?)";
            PreparedStatement rm = conn.prepareStatement("DELETE FROM friendfinder" +
                                                         ".pending_event_invite " +
                                                         "WHERE email = ? AND event = ?");
            rm.setString(1, in.getString("user"));
            rm.setString(2, in.getString("id"));
            rm.executeUpdate();
        }
        else
        {
            sql1 = "DELETE FROM friendfinder.attending_event " + "WHERE attendee = ? AND event = ?";
        }
        stmt = conn.prepareStatement(sql1);
        stmt.setString(1, in.getString("user"));
        stmt.setInt(2, Integer.parseInt(in.getString("id")));
        stmt.executeUpdate();

        out.put("success", success);
    }
}
