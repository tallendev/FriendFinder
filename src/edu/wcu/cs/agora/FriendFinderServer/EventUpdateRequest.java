package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

/**
 * @author Tyler Allen
 * @version 2/1/2015
 *          <p>
 *          Request type for a group update.
 * @created 11/16/14
 */
public class EventUpdateRequest extends Request
{


    /**
     * Default constructor to super.
     *
     * @param in Input JSON object.
     * @param out Output JSON object.
     */
    public EventUpdateRequest (JSONObject in, JSONObject out)
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
        if (in.getBoolean("create"))
        {
            success = makeEvent(in, out, conn);
        }
        else
        {
            String sql = "UPDATE friendfinder.event " +
                         " SET event_name = ?, description = ?, location_value = ? " +
                         " WHERE id = ? and creator = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, in.getString("eventname"));
            stmt.setString(2, in.getString("description"));
            stmt.setString(3, in.getString("location"));
            stmt.setInt(4, Integer.parseInt(in.getString("id")));
            stmt.setString(5, in.getString("user"));
            stmt.executeUpdate();
        }
        out.put("success", success);
    }

    /**
     * Helper method for when we are making a new group.
     */
    private boolean makeEvent (JSONObject in, JSONObject out, Connection conn)
            throws SQLException, JSONException
    {
        boolean success = true;
        String name = in.getString("eventname");
        String desc = in.getString("description");
        String loc = in.getString("location");
        Statement max = conn.createStatement();
        ResultSet idMax = max
                .executeQuery("SELECT max(friendfinder.event.id) " + "FROM friendfinder.event");
        int id = 0;
        if (idMax.next())
        {
            id = idMax.getInt(1) + 1;
        }
        PreparedStatement stmt;

        String createEvent = "INSERT INTO friendfinder.event VALUES(?, ?, ?, ?, ?, ?, ?);";
        stmt = conn.prepareStatement(createEvent);
        stmt.setInt(1, id);
        stmt.setString(2, name);
        System.err.println("DATE: " + in.getString("date"));
        stmt.setDate(3, Date.valueOf(in.getString("date")));  //set default picture since they don't
        // work
        stmt.setTime(4, Time.valueOf(in.getString("time")));
        stmt.setString(5, loc);
        stmt.setString(6, in.getString("user"));
        stmt.setString(7, desc);
        stmt.executeUpdate();

        String addEventAttendee = "INSERT INTO friendfinder.attending_event VALUES(?, ?);";
        stmt = conn.prepareStatement(addEventAttendee);
        stmt.setString(1, in.getString("user"));
        stmt.setInt(2, id);
        stmt.executeUpdate();
        return success;
    }
}
