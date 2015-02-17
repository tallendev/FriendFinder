package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

/**
 * @author Tyler Allen
 * @created 11/16/14
 * @version 2/15/2015
 */
public class SyncRequest extends Request
{
    private static String EVENTS_SQL = "SELECT event_name, event_Date, event_time,  " +
                                       "location_value, creator, id, description " +
                                       "FROM " + "event, attending_event " +
                                       "WHERE " + "event_name ILIKE ?" +
                                       " AND event = id AND attendee = ?;";

    private static String USERS_SQL = " SELECT email, birthday, gender, full_name " +
                                      " FROM users " +
                                      " WHERE email ILIKE ?";

    private static String GROUPS_SQL =
            " SELECT user_group.group_name, group_description, group_photo, owner " +
            " FROM user_group,  group_member " +
            "   WHERE user_group.group_name ILIKE ?";

    private static String GROUPS_SELF_SQL =
            " SELECT user_group.group_name, group_description, group_photo, owner " +
            " FROM user_group, group_member " +
            " WHERE user_group.group_name ILIKE ? " +
            " AND group_member.member_email = ?";

    private static String LIKES_SQL = " SELECT like_label " +
                                      " FROM likes " +
                                      " WHERE like_label ILIKE ?";

    private static String USERS_GROUP_SQL = " SELECT email, birthday, gender, full_name " +
                                            " FROM users, group_member " +
                                            " WHERE group_member.member_email = ?" +
                                            " AND users.email = ?" +
                                            "AND group_member.group_name = ?";
    private String     search;
    private String     user;
    private String     groupMember;
    private Connection conn;

    /**
     * Default constructor calling super.
     *
     * @param in Input JSON object from client.
     * @param out Output JSON object preparing to be sent to client.
     */
    public SyncRequest (JSONObject in, JSONObject out)
    {
        super(in, out);
    }

    /**
     * Executes the synchronization request.
     *
     * @throws SQLException If an error connection to the SQL database or there was an error with a
     * DB query.
     * @throws JSONException If an error with a JSONObject was encountered.
     */
    @Override
    protected void getResponse () throws SQLException, JSONException, MalformedPacketException
    {
        JSONObject in = getJsonIn();
        JSONObject out = getJsonOut();
        conn = DatabaseConnectionBuilder.buildDatabaseConnection();
        Statement statement = conn.createStatement();
        statement.execute("set search_path to friendfinder");

        int i = 0;
        System.err.println("getResponse");
        // for each statement execute the query
        while (in.has("table" + i))
        {
            System.err.println("in string:\n" + in.getString("table" + i));
            executeStatement(buildStatement(conn, i), i);
            i++;
        }
        conn.close();
    }

    /**
     * Helper to execute a query. Builds a JSON object from the results and stores it in the output
     * JSON object.
     *
     * @param stmt The statement that has been prepared to be executed.
     * @param tableNum The table we are currently operating on.
     *
     * @throws SQLException If an error in SQL is present.
     * @throws JSONException If an error with a JSONObject is encountered.
     */
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
                    // retrieve data from query
                    System.out.println("columnName: " + rs.getMetaData().getColumnName(k));
                    builder.append(rs.getMetaData().getColumnName(k));
                    builder.append("=");
                    builder.append(rs.getString(k));
                    if (k++ < columnCount)
                    {
                        builder.append(",");
                    }
                }
                if (in.getString("table" + tableNum).equals("user_group") ||
                    in.getString("table" + tableNum).equals("user_group_self"))
                {
                    PreparedStatement member = conn.prepareStatement("SELECT email " +
                                                                     "FROM friendfinder" +
                                                                     ".users, friendfinder" +
                                                                     ".group_member " +
                                                                     "WHERE email = ? AND " +
                                                                     "member_email = ? AND " +
                                                                     "group_name = ?;");
                    member.setString(1, in.getString("user"));
                    member.setString(2, in.getString("user"));
                    member.setString(3, (rs.getString("group_name")));
                    ResultSet resultSet = member.executeQuery();
                    builder.append(",member=");
                    builder.append(resultSet.next());
                }
                else if (in.getString("table" + tableNum).equals("event"))
                {
                    PreparedStatement member = conn.prepareStatement("SELECT email " +
                                                                     "FROM friendfinder" +
                                                                     ".users, friendfinder" +
                                                                     ".attending_event " +
                                                                     "WHERE email = ? AND " +
                                                                     "attendee  = ? AND " +
                                                                     "event = ?;");
                    member.setString(1, in.getString("user"));
                    member.setString(2, in.getString("user"));
                    member.setInt(3, Integer.parseInt(rs.getString("id")));
                    ResultSet resultSet = member.executeQuery();
                    builder.append(",attending=");
                    builder.append(resultSet.next());
                }

                builder.append("~");
            }
            System.err.println("ResultSet:\n" + rs);
            // place result of building JSON statement into a JSON object.
            out.put("table" + tableNum, in.getString("table" + tableNum) + "~" + builder.toString());
        }
    }

    /**
     * Builds a JSON statement to be executed.
     * @param conn The connection to the current database.
     * @param tableNum The number of tables.
     * @return A prepared sql statement based on the input information.
     * @throws JSONException Thrown in the event of an error with a JSON object
     * @throws SQLException Thrown with
     * TODO: Would be good to refactor if it can be done without compromising security.
     */
    private PreparedStatement buildStatement (Connection conn, int tableNum)
            throws JSONException, SQLException, MalformedPacketException
    {
        PreparedStatement stmt = null;
        JSONObject in = getJsonIn();
        System.err.println("buildStatement: jsonIn value: " + in.toString());
        search = in.has("search") ? in.getString("search") : null;
        user = in.getString("user");
        groupMember = in.has("group_name") ? in.getString("group_name") : null;
        return assignSQL(in, tableNum, conn);
    }

    /**
     * Selects the appropriate SQL statement to use for this query.
     *
     * @param in Input JSON object.
     * @param tableNum The current table number.
     * @param conn The connection with the database.
     *
     * @return A prepared statement with the appropriate query and arguments.
     *
     * @throws JSONException If an error is encountered with a json object.
     * @throws SQLException If an error is encountered building the prepared statement.
     */
    private PreparedStatement assignSQL (JSONObject in, int tableNum, Connection conn)
            throws JSONException, SQLException, MalformedPacketException
    {
        PreparedStatement stmt;
        String sql = null;
        // which search statement do we use?
        switch (in.getString("table" + tableNum))
        {
            case "event":
            {
                sql = EVENTS_SQL;
                break;
            }
            case "user_group":
            {
                sql = GROUPS_SQL;
                user = null;
                break;
            }
            case "user_group_self":
            {
                sql = GROUPS_SELF_SQL;
                System.err.println("Groups_self_sql");
                break;
            }
            case "likes":
            {
                sql = LIKES_SQL;
                user = null;
                break;
            }
            case "users":
            {
                if (groupMember != null)
                {
                    search = user; //null
                    sql = USERS_GROUP_SQL;
                    System.err.println("search: " + search);
                }
                else
                {
                    sql = USERS_SQL;
                    user = null;
                }
                break;
            }
            default:
            {
                throw new MalformedPacketException("Invalid table query.");
            }
        }
        stmt = conn.prepareStatement(sql);
        setStatementArguments(stmt);
        return stmt;
    }

    /**
     * Assign arguments to prepared statement.
     * @param stmt Statement to apply arguments to.
     *
     * @throws SQLException If there is an error adding the arguments to the SQL query.
     */
    private void setStatementArguments (PreparedStatement stmt)
            throws SQLException
    {
        int setStringVal = 1;
        if (search != null)
        {
            System.err.println("SyncRequest: Search is not null: " + search);
            stmt.setString(setStringVal, search);
            setStringVal++;
        }
        if (user != null) //FIXME not a great design
        {
            System.err.println("SyncRequest: User is not null.");
            stmt.setString(setStringVal, user);
            setStringVal++;
        }
        if (groupMember != null)
        {
            System.err.println("SyncRequest: GroupMember is not null");
            stmt.setString(setStringVal, groupMember);
            setStringVal++;
        }
    }
}
