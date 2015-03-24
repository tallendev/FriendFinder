package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

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
            java.util.Calendar cal = new GregorianCalendar();
            DateFormat julian = new SimpleDateFormat("yyyyD");
            String[] times = event.split(",");
            stmt = conn.prepareStatement("INSERT INTO friendfinder.calendar " +
                                         "(email, date_start, date_end, time_start, " +
                                         "time_end) VALUES (?,?,?,?,?)");
            stmt.setString(1, in.getString("user"));
            //cal.setTimeInMillis(Long.parseLong(times[DATE_START]));
            stmt.setDate(2, new java.sql.Date(Date.parse(julian.format(times[DATE_START]))));
            //cal.setTimeInMillis(Long.parseLong(times[DATE_END]));
            stmt.setDate(3, new java.sql.Date(Date.parse(julian.format(times[DATE_END]))));

            long mins = Long.parseLong(times[TIME_START]);
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            String hhmm = String.format("%02d:%02d", mins / 60, mins % 60) + ":00";
            //System.err.println("hhmm: " + hhmm);
            Date dt;
            try
            {
                dt = formatter.parse(hhmm);
            }
            catch (ParseException e)
            {
                throw new MalformedPacketException("Invalid date");
            }
            cal.setTime(dt);
            stmt.setTime(4, new java.sql.Time(cal.getTime().getTime()));

            mins = Long.parseLong(times[TIME_END]);
            hhmm = String.format("%02d:%02d", mins / 60, mins % 60) + ":00";
            try
            {
                dt = formatter.parse(hhmm);
            }
            catch (ParseException e)
            {
                throw new MalformedPacketException("Invalid date");
            }
            cal.setTime(dt);
            stmt.setTime(5, new java.sql.Time(cal.getTime().getTime()));
            stmt.executeUpdate();
        }
        out.put("success", true);
    }
}
