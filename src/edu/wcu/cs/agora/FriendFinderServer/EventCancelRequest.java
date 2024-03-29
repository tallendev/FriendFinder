package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Tyler Allen
 * @version 2/15/2015
 * @created 3/3/2015
 * <p>
 * Request type for cancelling an event
 */
public class EventCancelRequest extends Request
{
    /**
     * Default constructor to super.
     *
     * @param in Input JSON object.
     * @param out Output JSON object.
     */
    public EventCancelRequest (JSONObject in, JSONObject out)
    {
        super(in, out);
    }

    /**
     * Deletes group and members where owner, group name match.
     *
     * @throws java.sql.SQLException In the event there is an error with an sql statement.
     * @throws org.json.JSONException In the event JSON does not understand an SQL query.
     */
    @Override
    protected void getResponse () throws SQLException, JSONException
    {
        JSONObject in = getJsonIn();
        JSONObject out = getJsonOut();
        int id = Integer.parseInt(in.getString("id"));
        Connection conn = DatabaseConnectionBuilder.buildDatabaseConnection();
        boolean success = true;
        String deleteMembers = "DELETE FROM friendfinder.attending_event " +
                               "USING friendfinder.event " +
                               "WHERE event = ? AND " +
                               "creator = ?";
        PreparedStatement stmt = conn.prepareStatement(deleteMembers);
        stmt.setInt(1, id);
        stmt.setString(2, in.getString("user"));
        stmt.executeUpdate();

        String deleteInvites = "DELETE FROM friendfinder.pending_event_invite " + "WHERE event = ?";
        stmt = conn.prepareStatement(deleteInvites);
        stmt.setInt(1, id);
        stmt.executeUpdate();

        String deleteEvent = "DELETE FROM friendfinder.event " + "WHERE id = ? AND creator = ?";
        stmt = conn.prepareStatement(deleteEvent);
        stmt.setInt(1, id);
        stmt.setString(2, in.getString("user"));
        stmt.executeUpdate();
        out.put("success", success);
    }
}
