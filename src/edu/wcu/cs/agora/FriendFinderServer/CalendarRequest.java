package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

/**
 * Created by tyler on 3/24/2015.
 */
public class CalendarRequest extends Request
{
    public static final int DATE_START = 0;
    public static final int TIME_START = 1;
    public static final int DATE_END   = 2;
    public static final int TIME_END   = 3;


    /**
     * Default constructor to super.
     *
     * @param in Input JSON object.
     * @param out Output JSON object.
     */
    public CalendarRequest (JSONObject in, JSONObject out)
    {
        super(in, out);
    }

    /**
     * This method is the workhorse of the Request type. Executes the primary functionality of that
     * object.
     *
     * @throws SQLException An error connecting to the database or with a database query has been
     * encountered.
     * @throws JSONException An error with a JSON object has been encountered.
     */
    @Override
    protected void getResponse () throws SQLException, JSONException, MalformedPacketException
    {
        JSONObject in = getJsonIn();
        JSONObject out = getJsonOut();
        Connection conn = DatabaseConnectionBuilder.buildDatabaseConnection();
        PreparedStatement stmt = conn
                .prepareStatement("DELETE FROM friendfinder.calendar WHERE " + "email = ?");
        stmt.setString(1, in.getString("user"));
        stmt.executeUpdate();
        String events = in.getString("events");
        System.err.println("Calendar events: " + events);
        String[] eventsArray = events.split(";");
        for (String event : eventsArray)
        {
            String[] times = event.split(",");
            stmt = conn.prepareStatement("INSERT INTO friendfinder.calendar " +
                                         "(email, date_start, date_end, time_start, " +
                                         "time_end) VALUES (?,?,?,?,?)");
            stmt.setString(1, in.getString("user"));
            stmt.setDate(2, Date.valueOf(times[DATE_START]));
            stmt.setDate(3, Date.valueOf(times[DATE_END]));
            stmt.setTime(4, Time.valueOf(times[TIME_START]));
            stmt.setTime(5, Time.valueOf(times[TIME_END]));
            stmt.executeUpdate();
        }
        out.put("success", true);
    }
}
