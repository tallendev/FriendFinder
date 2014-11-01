package edu.wcu.cs.agora.FriendFinderServer;

import java.sql.*;
import java.util.Scanner;

/**
 * Created by tyler on 10/20/2014.
 */
public class Authenticator
{
    private static Authenticator ourInstance;

    public static synchronized Authenticator getInstance () throws ClassNotFoundException
    {
        if (ourInstance == null)
        {
            ourInstance = new Authenticator();
        }
        return ourInstance;
    }

    private Authenticator () throws ClassNotFoundException
    {
        Class.forName("com.mysql.jdbc.Driver");
    }

    // In the end should probably return more than true false, maybe a string that could contain a
    // token?
    public boolean AuthenticateUser(Scanner in) throws MalformedPacketException, SQLException
    {
        boolean success = false;
        String username = null;
        String password = null;
        //todo: refactor me into a util method once format is locked down please
        if (in.hasNext())
        {
            username = in.next();
        }
        else
        {
            throw new MalformedPacketException("Missing username");
        }
        if (in.hasNext())
        {
            password = in.next();
        }
        else
        {
            throw new MalformedPacketException("Missing password.");
        }
        Connection conn = DriverManager.getConnection("friendfinder", "tyler", "hadouken!");
        Statement statement = conn.createStatement();
        success = statement.execute("SELECT username, password " +
                                    "FROM users");

        //TODO finish me when dummy acounts are made.
        System.err.println("Account succesfully authenticated: " + success);
        return success; //FIXME
    }
}
