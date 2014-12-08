package edu.wcu.cs.agora.FriendFinderServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Tyler Allen
 * @version 12/8/2014
 *          <p>
 *          A builder for obtaining a connection to this application's database.
 * @created 12/8/2014
 */
public class DatabaseConnectionBuilder
{
    /**
     * PSQL location of our database.
     */
    private static String DB_ADDR = "jdbc:postgresql:friendfinder";
    /**
     * User name for accessing database.
     */
    private static String DB_USER = "tyler";
    /**
     * Password to our database.
     */
    private static String DB_PASS = "hadouken!";

    /**
     * Ensures no instances of this object may be built.
     */
    private DatabaseConnectionBuilder ()
    {
    }

    /**
     * Builds a connection to the database.
     *
     * @return The new connection to the database.
     *
     * @throws SQLException If there is an error connecting to the database.
     */
    protected static Connection buildDatabaseConnection () throws SQLException
    {
        return DriverManager.getConnection(DB_ADDR, DB_USER, DB_PASS);
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
