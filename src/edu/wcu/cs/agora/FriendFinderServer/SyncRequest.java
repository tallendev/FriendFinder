package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

/**
 * Created by tyler on 11/16/14.
 */
public class SyncRequest extends Request
{
    public SyncRequest(JSONObject in, JSONObject out)
    {
        super(in, out);
    }

    @Override
    protected void getResponse() throws SQLException, JSONException {
        JSONObject in = getJsonIn();
        JSONObject out = getJsonOut();
        Connection conn = DriverManager.getConnection("jdbc:postgresql:friendfinder", "tyler",
                         "hadouken!");
        Statement statement = conn.createStatement();
        statement.execute("set search_path to friendfinder");

        int i = 0;
        while (in.has("table" + i))
        {
            String sql = null;
            switch (in.getString("table" + i))
            {
                case "event":
                {
                    sql = "SELECT *" +
                          "FROM " + "event, attending_event " +
                          "WHERE " +  "attending_event.attendee = " + in.get("user") +
                                      "attending_event.attendee = event.id";
                }
            }

            if (statement.execute(sql))
            {
                ResultSet rs = statement.getResultSet();
                System.err.println("ResultSet:\n" + rs);
                out.put("table" + i, in.getString("table" + i) + "\n" + rs);
            }
        }
    }
}