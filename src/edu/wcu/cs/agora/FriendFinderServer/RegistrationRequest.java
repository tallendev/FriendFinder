package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

/**
 * @author Tyler Allen
 * @created 11/29/14
 * @version 12/8/2014
 *
 * Handles a RegistrationRequest from a client.
 */
public class RegistrationRequest extends Request
{
    /**
     * Default constructor. Calls super.
     *
     * @param in Input JSON object from client.
     * @param out Outgoing JSON object to client.
     */
    public RegistrationRequest(JSONObject in, JSONObject out)
    {
        super(in, out);
    }

    /**
     * Attempts to register a user for the application using their provided credentials.
     *
     * @throws SQLException In the event of an SQL error.
     * @throws JSONException In the event of missing information in the JSON in object.
     */
    @Override
    protected void getResponse() throws SQLException, JSONException
    {
        JSONObject in = getJsonIn();
        JSONObject out = getJsonOut();

        String user = in.getString("user");
        String pass = in.getString("password");
        String birthday = in.getString("birthday");
        String gender = in.getString("gender");
        String name = in.getString("name");

        Connection conn = DatabaseConnectionBuilder.buildDatabaseConnection();
        ResultSet rs = Authenticator.findUser(conn, user);
        // user does not exist.
        if (rs.next())
        {
            out.put("success", false);
        }
        // user does exist.
        else
        {
            System.err.println("Birthday: " + birthday);
            // Setting up the statement and its parameters.
            String createUser = "INSERT INTO friendfinder.users VALUES(?, ?, ?, ?, ?);";
            PreparedStatement stmt = conn.prepareStatement(createUser);
            stmt.setString(1, user);
            stmt.setDate(2, Date.valueOf(birthday));
            stmt.setString(3, gender);
            stmt.setString(4, pass);
            stmt.setString(5, name);
            stmt.executeUpdate();
            out.put("success", true);
        }
    }
}
