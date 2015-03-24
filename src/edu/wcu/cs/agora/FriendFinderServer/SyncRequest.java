package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.util.Calendar;

/**
 * @author Tyler Allen
 * @created 11/16/14
 * @version 3/3/2015
 */
public class SyncRequest extends Request
{
    private static final String EVENTS_SQL = "SELECT event_name, event_Date, event_time,  " +
                                       "location_value, creator, id, description " +
                                       "FROM " + "event, attending_event " +
                                       "WHERE " + "event_name ILIKE ?" +
                                       " AND event = id AND attendee = ?" +
                                       " UNION " +
                                       "SELECT event_name, event_Date, event_time, " +
                                       "location_value," +
                                       " creator, id, description " +
                                       "FROM event, pending_event_invite " +
                                       "WHERE event_name ILIKE ? and event = id  and email = ?;";

    private static final String USERS_SQL = " SELECT email, birthday, gender, full_name " +
                                      " FROM users " +
                                      " WHERE email ILIKE ?";

    private static final String USERS_NOT_INVITED = " SELECT email, birthday, gender, full_name " +
                                                    " FROM users WHERE email ILIKE ? and " +
                                                    "  NOT EXISTS  " +
                                                    "   (SELECT email, birthday,  gender, " +
                                                    "full_name" +
                                                    " FROM users, attending_event " +
                                                    " WHERE users.email ILIKE ? and" +
                                                    " " +
                                                    "attendee = users.email and event = ?" +
                                                    " UNION SELECT email, birthday, gender, " +
                                                    "full_name" +
                                                    " FROM users, pending_event_invite" +
                                                    " WHERE users.email ILIKE ? and" +
                                                    " pending_event_invite.email = users" +
                                                    ".email and event = ?);";


    private static final String GROUPS_SQL =
            " SELECT user_group.group_name, group_description, group_photo, owner " +
            " FROM user_group,  group_member " +
            "   WHERE user_group.group_name ILIKE ?";

    private static final String GROUPS_SELF_SQL =
            " SELECT user_group.group_name, group_description, group_photo, owner " +
            " FROM users, user_group,  group_member " +
            " WHERE group_member.member_email = ?" +
            " AND users.email = group_member.member_email" +
            " AND group_member.group_name = user_group.group_name";

    private static final String LIKES_SQL = " SELECT like_label " +
                                      " FROM likes " +
                                      " WHERE like_label ILIKE ?";

    private static final String USERS_GROUP_SQL = " SELECT email, birthday, gender, full_name " +
                                            " FROM users, group_member " +
                                            " WHERE group_member.member_email = users" +
                                            ".email AND group_member.group_name = ?";
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
                    member = conn.prepareStatement("SELECT users.email " +
                                                   "FROM friendfinder.users, " +
                                                   "friendfinder.pending_event_invite " +
                                                   "WHERE users.email = ? AND " +
                                                   "pending_event_invite.email = ? AND event = ?;");
                    member.setString(1, in.getString("user"));
                    member.setString(2, in.getString("user"));
                    member.setInt(3, Integer.parseInt(rs.getString("id")));
                    resultSet = member.executeQuery();
                    builder.append(",invited=");
                    builder.append(resultSet.next());
                }
                else if (in.getString("table" + tableNum).equals("users"))
                {
                    builder.append(",busy=");
                    builder.append(isBusy(in, conn));
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

    private boolean isBusy (JSONObject in, Connection conn) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement("SELECT index " +
                                                       " FROM friendfinder.calendar" +
                                                       " WHERE ? BETWEEN date_start AND date_end " +
                                                       "AND ? BETWEEN time_start AND time_end;");
        Calendar cal = Calendar.getInstance();
        stmt.setDate(1, new java.sql.Date(cal.getTime().getDate()));
        stmt.setTime(2, new java.sql.Time(cal.getTime().getTime()));
        return stmt.executeQuery().next();
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
        PreparedStatement stmt = null;
        String sql = null;
        // boolean basic = true;
        // which search statement do we use?
        switch (in.getString("table" + tableNum))
        {
            case "event":
            {
                System.err.println("case event");
                sql = EVENTS_SQL;
                break;
            }
            case "user_group":
            {
                System.err.println("case user_group");
                sql = GROUPS_SQL;
                user = null;
                break;
            }
            case "user_group_self":
            {
                System.err.println("case user_group_self");
                sql = GROUPS_SELF_SQL;
                search = null;
                break;
            }
            case "likes":
            {
                System.err.println("case likes");
                sql = LIKES_SQL;
                user = null;
                break;
            }
            case "users":
            {
                System.err.println("case users");
                if (groupMember != null)
                {
                    System.err.println("GroupMember != null");
                    search = null;
                    sql = USERS_GROUP_SQL;
                    System.err.println("search: " + search);
                }
                else
                {
                    sql = USERS_SQL;
                }
                user = null;
                break;
            }
            /*case "users_not_invited":
            {
                basic = false;
                sql = USERS_NOT_INVITED;
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, in.getString(search));
                stmt.setString(2, in.getString(search));
                stmt.setInt(3, Integer.parseInt(in.getString("id")));
                stmt.setString(4, in.getString(search));
                stmt.setInt(5, Integer.parseInt(in.getString("id")));
                break;
            }*/
            default:
            {
                throw new MalformedPacketException("Invalid table query.");
            }
        }
        //if (basic)
        //{
        stmt = conn.prepareStatement(sql);
        if (in.getString("table" + tableNum).equals("event"))
        {
            stmt.setString(3, search);
            stmt.setString(4, user);
        }
        setStatementArguments(stmt);
        //}
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
