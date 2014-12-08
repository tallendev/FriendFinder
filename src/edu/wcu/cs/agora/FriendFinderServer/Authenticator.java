package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Tyler Allen
 * @created 10/20/2014
 * @version 12/8/2014
 *
 * This class performs the authentication of a user. This class is a Singleton because it requires
 * no state.
 */
public class Authenticator
{

    /**
     * The only instance of this authenticator.
     */
    private static Authenticator ourInstance;

    /**
     * Stub to ensure that the only constructor is private.
     */
    private Authenticator ()
    {
        // do nothing
    }

    /**
     * Lazy creation of Authenticator when requested.
     */
    public static synchronized Authenticator getInstance ()
    {
        if (ourInstance == null)
        {
            ourInstance = new Authenticator();
        }
        return ourInstance;
    }

    /**
     * Authenticate a user using their credentials.
     *
     * @param json JSON object received from server containing user's credentials.
     *
     * @return If a user's credentials match those in the database.
     *
     * @throws MalformedPacketException If user/password are missing.
     * @throws SQLException If an error with the database queries is encountered.
     * @throws JSONException If an error with the JSONObject is encountered.
     */
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
        // create connection with database.
        Connection conn = DatabaseConnectionBuilder.buildDatabaseConnection();
        ResultSet rs = findUser(conn, username);
        if (rs.next())
        {
            // validate password.
            success = password.equals(rs.getString("password_token"));
        }
        System.err.println("Account successfully authenticated? " + success);
        conn.close();
        return success;
    }

    /**
     * Helper method that will find a user in the DB using an open DB connection and the user's
     * username.
     *
     * @param conn An open DB connection.
     * @param username The username to search for.
     *
     * @return The ResultSet containing the user's information.
     *
     * @throws SQLException If an SQL query error is encountered.
     */
    protected static ResultSet findUser(Connection conn, String username) throws SQLException
    {
        String sql = "SELECT friendfinder.users.email, friendfinder.users.password_token " +
                     "FROM friendfinder.users WHERE email = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        return stmt.executeQuery();
    }

    /**
     * Ensures this object cannot be cloned.
     *
     * @return Nothing
     *
     * @throws CloneNotSupportedException Always.
     */
    @Override
    protected Object clone () throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("Cloning of builder not supported.");
    }
}
