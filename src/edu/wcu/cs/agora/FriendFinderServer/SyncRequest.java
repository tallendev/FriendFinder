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
            System.err.println("in string:\n" + in.getString("table" + i));
            PreparedStatement stmt = null;
            String sql = null;
            String sqlVal1 = null;
            switch (in.getString("table" + i))
            {
                case "event":
                {
                    sql =   "SELECT event_name, event_Date, event_time, location_value " +
                            "FROM " + "event, attending_event " +
                            "WHERE " +  "attendee = ?" +
                            " AND event = id;";
                    sqlVal1 = "user";
                    break;
                }
                case "user_group":
                {
                    sql = " SELECT group_name, group_description " +
                            " FROM user_group, group_member " +
                            " WHERE member_email = ?; ";
                    sqlVal1 = "user";
                    break;
                }
            }
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, in.getString(sqlVal1));
            ResultSet rs;
            if ((rs = stmt.executeQuery()) != null)
            {
                StringBuilder builder = new StringBuilder();
                while (rs.next())
                {
                    int columnCount = rs.getMetaData().getColumnCount();
                    for (int k = 1; k <= columnCount;)
                    {
                        System.out.println("columnName: " + rs.getMetaData().getColumnName(k));
                        builder.append(rs.getMetaData().getColumnName(k));
                        builder.append("=");
                        builder.append(rs.getString(k));
                        if (k++ < columnCount)
                        {
                            builder.append(",");
                        }
                    }
                    builder.append("~");
                }
                System.err.println("ResultSet:\n" + rs);
                out.put("table" + i, in.getString("table" + i) + "~" + builder.toString());
            }
            i++;
        }
        //System.out.println(out.get("table" + 0));
    }

}
