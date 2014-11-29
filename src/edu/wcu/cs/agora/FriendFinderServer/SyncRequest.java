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
            executeStatement(buildStatement(conn, i), i);
            i++;
        }
        //System.out.println(out.get("table" + 0));
    }

    private void executeStatement(PreparedStatement stmt, int tableNum) throws SQLException, JSONException
    {
        JSONObject in = getJsonIn();
        JSONObject out = getJsonOut();
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
            out.put("table" + tableNum, in.getString("table" + tableNum) + "~" + builder.toString());
        }
    }

    private PreparedStatement buildStatement(Connection conn, int tableNum) throws JSONException, SQLException {
        PreparedStatement stmt = null;
        String sql = null;
        JSONObject in = getJsonIn();
        String search = in.has("search") ? in.getString("search") : null;
        String user = in.getString("user");
        switch (in.getString("table" + tableNum))
        {
            case "event":
            {
                sql =   "SELECT event_name, event_Date, event_time, location_value, creator " +
                        "FROM " + "event, attending_event " +
                        "WHERE " +  "attendee = ?" +
                        " AND event = id AND event_name ILIKE ?;";
                //sqlVal1 = "user";
                break;
            }
            case "user_group":
            {
                sql = " SELECT user_group.group_name, group_description " +
                        " FROM user_group, group_member " +
                        " WHERE user_group.group_name ILIKE ?";
                user = null;
                        // +
                        // below is code for user-specfic groups.
                        //" WHERE member_email = ? AND user_group.group_name = group_member.group_name; ";
               // sqlVal1 = "user";
                break;
            }
            case "likes":
            {
                sql = " SELECT like_label " +
                        " FROM likes " +
                        " WHERE like_label ILIKE ?"; //+
                user = null;
                        //" WHERE member_email = ? AND user_group.group_name = group_member.group_name; ";
                break;
            }
            case "users":
            {
                sql = " SELECT email, birthday, gender, full_name " +
                        " FROM likes " +
                        " WHERE like_label ILIKE ?"; //+
                //" WHERE member_email = ? AND user_group.group_name = group_member.group_name; ";
                user = null;
                break;
            }
        }
        stmt = conn.prepareStatement(sql);
        if (search != null)
        {
            stmt.setString(1, search);
        }
        if (user != null) //FIXME not a great design
        {
            stmt.setString(2, user);
        }

        return stmt;
    }

}
