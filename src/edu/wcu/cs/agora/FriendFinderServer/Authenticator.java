package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

/**
 * Created by tyler on 10/20/2014.
 */
public class Authenticator
{
    private static Authenticator ourInstance;

    public static synchronized Authenticator getInstance ()
    {
        if (ourInstance == null)
        {
            ourInstance = new Authenticator();
        }
        return ourInstance;
    }

    private Authenticator ()
    {
    }

    // In the end should probably return more than true false, maybe a string that could contain a
    // token?
    public boolean authenticateUser(JSONObject json) throws MalformedPacketException, SQLException, JSONException {
        boolean success = false;
        String username = null;
        String password = null;
        //todo: refactor me into a util method once format is locked down please
        if (json.has("user"))
        {
            username = json.getString("user");
        }
        else
        {
            throw new MalformedPacketException("Missing username");
        }
        if (json.has("password"))
        {
            password = json.getString("password");
        }
        else
        {
            throw new MalformedPacketException("Missing password.");
        }
        Connection conn = DriverManager.getConnection("jdbc:postgresql:friendfinder", "tyler",
                                                      "hadouken!");
        ResultSet rs = find_user(conn, username);
        if (rs.next())
        {
            success = password.equals(rs.getString("password_token"));
        }
        System.err.println("Account successfully authenticated? " + success);
        conn.close();
        return success;
    }

    protected static ResultSet find_user(Connection conn, String username) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement("SELECT users.email, users.password_token " +
                                                        "FROM users WHERE email = ?");
        stmt.setString(1, username);
        ResultSet rs;
        return stmt.executeQuery();
    }
}
