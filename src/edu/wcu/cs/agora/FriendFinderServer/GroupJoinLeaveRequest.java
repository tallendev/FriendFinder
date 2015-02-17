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
public class GroupJoinLeaveRequest extends Request
{


    /**
     * Default constructor to super.
     *
     * @param in Input JSON object.
     * @param out Output JSON object.
     */
    public GroupJoinLeaveRequest (JSONObject in, JSONObject out)
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
            sql = "INSERT into friendfinder.group_member " + "VALUES (?,?)";

        }
        else
        {
            sql = "DELETE FROM friendfinder.group_member " +
                  "WHERE member_email = ? and group_name = ?";
        }
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, in.getString("user"));
        stmt.setString(2, in.getString("group_name"));
        stmt.executeUpdate();

        out.put("success", success);
    }
}
