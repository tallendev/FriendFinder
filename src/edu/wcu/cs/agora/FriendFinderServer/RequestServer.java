package edu.wcu.cs.agora.FriendFinderServer;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * @author Tyler Allen
 * @created 9/29/2014
 * @version 12/8/2014
 *
 * The server for the application. This class listens for connections from clients and executes
 * their requests.
 *
 * TODO: Refactor me to use threaded responses so that the server may continue to listen for
 * connections.
 */
public class RequestServer
{
    /** Error Codes */

    /** Error associated with the server socket or one of its children. */
    public static final int SOCKET_ERROR = 100;
    /** Errors associated with JDBC, typically an issue loading JDBC. */
    public static final int JDBC_ERROR   = 200;
    /**
     * Error associated with SQL, typically an issue with the database or with an SQL query
     * accessing the DB.
     */
    public static final int SQL_ERROR    = 300;

    /** Amount of time before a socket times out. */
    public static final int TIMEOUT      = 5000;
    /** Default connection port. */
    public static final int DEFAULT_PORT = 1337;

    /** The server socket awaiting connections from clients. */
    private ServerSocket serverSocket;
    /** The port that the ServerSocket is listening on.*/
    private int          port;

    /**
     * Constructor. Builds an SSL socket as the serverSocket if possible.
     * @param port
     * @throws IOException In the event that there is an error creating a server socket.
     */
    public RequestServer (int port) throws IOException
    {
        System.err.println("RequestServer created.");
        this.port = port;
        serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(port);
    }

    /**
     * Main. Builds a requestServer object and continually listens for connections.
     *
     * @param args not used.
     */
    public static void main (String[] args)
    {
        try
        {
            // Loads PSQL driver.
            Class.forName("org.postgresql.Driver");
        }
        catch (ClassNotFoundException cfne)
        {
            System.err.println("JDBC Driver issue:\n" + cfne.getMessage());
            System.exit(JDBC_ERROR);
        }
        // Sets up SSL certificate.
        System.setProperty("javax.net.ssl.keyStore", "../keystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "hadouken!");

        RequestServer requestServer = null;
        try
        {
            requestServer = new RequestServer(DEFAULT_PORT);
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
            System.exit(SOCKET_ERROR);
        }
        listenHelper(requestServer);
    }

    /**
     * Helper method for main. Listens until the ServerSocket dies for some reason.
     * @param requestServer The requestServer object to listen with.
     */
    private static void listenHelper (RequestServer requestServer)
    {
        // Temporary until threads or other system for closing the server is configured.
        while (!requestServer.isSocketClosed())
        {
            try
            {
                requestServer.listen();
            }
            catch (IOException e)
            {
                System.err.println("Issue while listening:\n" + e.getMessage());
            }
            catch (SQLException e)
            {
                System.err.println("SQL issue:\n" + e.getMessage());
                e.printStackTrace();
                //System.exit(SQL_ERROR);
            }
            catch (JSONException e)
            {
                System.err.println("Error reading JSON object:\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * True if listening server socket is closed. return serverSocket.isClosed();
     */
    public boolean isSocketClosed ()
    {
        return serverSocket.isClosed();
    }

    /**
     * Listens for incoming connections from the server. Creates a request object from the
     * information received from the client and appropriately executes it.
     *
     * @throws SQLException Thrown in the event that there is some error in querying the database.
     * @throws IOException Thrown in the event that there is some
     */
    public void listen () throws SQLException, IOException, JSONException
    {
        System.err.println("RequestServer listening for new connection.");
        Socket client = serverSocket.accept();
        System.err.println("RequestServer accepted new connection.");
        client.setSoTimeout(TIMEOUT);
        Scanner in = new Scanner(client.getInputStream()).useDelimiter("\\A");
        if (in.hasNext())
        {
            JSONObject json = new JSONObject(in.next());
            JSONObject jsonOut = new JSONObject();

            Request request = Request.requestBuilder(json, jsonOut);
            request.getResponse();
            PrintStream out = new PrintStream(client.getOutputStream());
            System.out.println(jsonOut);
            out.println(jsonOut);
            out.flush();
            System.err.println("Sent JSON response");
        }
        else
        {
            System.err.println("No message received from client.");
        }
    }
}
