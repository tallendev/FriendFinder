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
    protected void getResponse() throws SQLException, JSONException
    {
        JSONObject in = getJsonIn();
        JSONObject out = getJsonOut();
        Connection conn = DriverManager.getConnection("jdbc:postgresql:friendfinder", "tyler",
                         "hadouken!");
        Statement statement = conn.createStatement();
        statement.execute("set search_path to friendfinder");

        int i = 0;
        System.err.println("getResponse");
        while (in.has("table" + i))
        {
            String sql = null;
            System.err.println("in string:\n" + in.getString("table" + i));
            PreparedStatement stmt = null;
            switch (in.getString("table" + i))
            {
                case "event":
                {
                    sql = "SELECT * " +
                          "FROM " + "event, attending_event " +
                          "WHERE " +  "attendee = ?" +
                                      " AND attendee IS event.id;";
                    stmt = conn.prepareStatement(sql);
                    System.err.println("in.getString(user): " + in.getString("user"));
                    stmt.setString(1, in.getString("user"));
                }
            }
            ResultSet rs;
            if ((rs = stmt.executeQuery()) != null)
            {
                System.err.println("ResultSet:\n" + rs);
                out.put("table" + i, in.getString("table" + i) + "\n" + rs);
            }
        }
    }
}
