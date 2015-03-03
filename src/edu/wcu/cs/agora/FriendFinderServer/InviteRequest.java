package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Tyler Allen
 * @version 2/16/2015
 * @created 3/3/2015 Request type for a leaving or joining a group.
 */
public class InviteRequest extends Request
{


    /**
     * Default constructor to super.
     *
     * @param in Input JSON object.
     * @param out Output JSON object.
     */
    public InviteRequest (JSONObject in, JSONObject out)
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

        int id = Integer.parseInt(in.getString("id"));
        String invited_user = in.getString("invited_user");

        String sql1 = "SELECT attendee " +
                      "FROM friendfinder.attending_event " +
                      "WHERE attendee = ? AND event = ?";
        PreparedStatement test = conn.prepareStatement(sql1);
        test.setString(1, invited_user);
        test.setInt(2, id);
        ResultSet rs = test.executeQuery();
        success = rs.next();
        if (success)
        {
            String sql
                    = "INSERT INTO friendfinder.pending_event_invite (email, event) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, invited_user);
            stmt.setInt(2, id);
            int status = stmt.executeUpdate();
            success = status != 0;
        }
        out.put("success", success);
    }
}
