package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.util.Scanner;

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
        Statement statement = conn.createStatement();
        statement.execute("set search_path to friendfinder");
        if (statement.execute("SELECT email, password_token " +
                              "FROM users"))
        {
            ResultSet rs = statement.getResultSet();
            boolean userNotFound = true;
            while (rs.next() && userNotFound)
            {
                String temp_user = rs.getString("email");
                if (temp_user.equals(username))
                {
                    userNotFound = false;
                    success = password.equals(rs.getString("password_token"));
                }
            }
        }
        System.err.println("Account successfully authenticated? " + success);
        return success;
    }
}
