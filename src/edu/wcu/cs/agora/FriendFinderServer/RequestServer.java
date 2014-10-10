package edu.wcu.cs.agora.FriendFinderServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

/**
 * TODO: Finish me
 */
public class RequestServer
{
    public static final int TIMEOUT = 5000;

    private ServerSocket serverSocket;
    private int port;

    public RequestServer(int port) throws IOException
    {
        this.port = port;
        serverSocket = new ServerSocket(port);
    }

    public void listen() throws IOException
    {
        try (Socket client = serverSocket.accept();
             Scanner in = new Scanner(client.getInputStream());
             OutputStream out = client.getOutputStream())
        {
            client.setSoTimeout(TIMEOUT);
            Request request = Request.requestBuilder(in);
            request.getResponse();
        }
        catch (SocketException se)
        {

        }

    }

}
