package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

/**
 * Created by tyler on 11/29/14.
 */
public class RegistrationRequest extends Request
{
    public RegistrationRequest(JSONObject in, JSONObject out)
    {
        super(in, out);
    }

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

        Connection conn = DriverManager.getConnection("jdbc:postgresql:friendfinder", "tyler",
                                                      "hadouken!");
        ResultSet rs = Authenticator.findUser(conn, user);
        if (rs.next())
        {
            out.put("success", false);
        }
        else
        {
            String createUser = "INSERT INTO friendfinder.users VALUES(?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(createUser);
            stmt.setString(1, user);
            stmt.setString(2, birthday);
            stmt.setString(3, gender);
            stmt.setString(4, pass);
            stmt.setString(5, name);
            stmt.executeQuery();
            out.put("success", true);
        }
    }
}
