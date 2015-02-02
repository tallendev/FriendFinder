package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Tyler Allen
 * @created 11/16/14
 * @version 2/1/2015
 *
 * Request type for a group update.
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
     * Creates/updates groups based on user supplied information.
     */
    @Override
    protected void getResponse() throws SQLException, JSONException
    {
        JSONObject in = getJsonIn();
        JSONObject out = getJsonOut();
        Connection conn = DatabaseConnectionBuilder.buildDatabaseConnection();
        boolean success = true;
        if (in.getBoolean("create"))
        {
            success = makeGroup(in, out, conn);
        }
        else
        {
            //TODO update me for photos
            String sql = "UPDATE friendfinder.user_group " +
                         " SET group_description = ? " +
                         " WHERE group_name = ? and owner = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, in.getString("groupdesc"));
            stmt.setString(2, in.getString("groupname"));
            stmt.setString(3, in.getString("user"));
            stmt.executeUpdate();
        }
        out.put("success", success);
    }

    /**
     * Helper method for when we are making a new group.
     */
    private boolean makeGroup(JSONObject in, JSONObject out, Connection conn) throws SQLException,
                                                                                     JSONException
    {
        boolean success = true;
        String name = in.getString("groupname");
        String desc = in.getString("groupdesc");

        String sql = "SELECT friendfinder.user_group.group_name " +
                     "FROM friendfinder.user_group WHERE group_name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        if(rs.next())
        {
            success = false;
        }
        else
        {
            String createGroup = "INSERT INTO friendfinder.user_group VALUES(?, ?, ?, ?);";
            stmt = conn.prepareStatement(createGroup);
            stmt.setString(1, name);
            stmt.setString(2, desc);
            stmt.setInt(3, 1);  //set default picture since they don't work
            stmt.setString(4, in.getString("user"));
            stmt.executeUpdate();

            String addGroupUser = "INSERT INTO friendfinder.group_member VALUES(?, ?);";
            stmt = conn.prepareStatement(addGroupUser);
            stmt.setString(1, in.getString("user"));
            stmt.setString(2, name);
            stmt.executeUpdate();
        }
        return success;
    }
}
