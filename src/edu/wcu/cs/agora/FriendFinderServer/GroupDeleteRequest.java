package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Tyler Allen
 * @version 2/15/2015
 *          <p>
 *          Request type for a group update.
 * @created 2/15/2015
 */
public class GroupDeleteRequest extends Request
{
    /**
     * Default constructor to super.
     *
     * @param in Input JSON object.
     * @param out Output JSON object.
     */
    public GroupDeleteRequest (JSONObject in, JSONObject out)
    {
        super(in, out);
    }

    /**
     * Deletes group and members where owner, group name match.
     *
     * @throws SQLException In the event there is an error with an sql statement.
     * @throws JSONException In the event JSON does not understand an SQL query.
     */
    @Override
    protected void getResponse () throws SQLException, JSONException
    {
        JSONObject in = getJsonIn();
        JSONObject out = getJsonOut();
        Connection conn = DatabaseConnectionBuilder.buildDatabaseConnection();
        boolean success = true;
        String deleteMembers = "DELETE FROM friendfinder.group_member " +
                               "USING friendfinder.user_group " +
                               "WHERE group_name = ? AND user_group.owner = ?";
        PreparedStatement stmt = conn.prepareStatement(deleteMembers);
        stmt.setString(1, in.getString("groupname"));
        stmt.setString(2, in.getString("user"));
        stmt.executeUpdate();
        String deleteGroup = "DELETE FROM friendfinder.user_group " +
                             "WHERE group_name = ? AND owner = ?";
        stmt = conn.prepareStatement(deleteGroup);
        stmt.setString(1, in.getString("groupname"));
        stmt.setString(2, in.getString("user"));
        stmt.executeUpdate();
        out.put("success", success);
    }
}
