package edu.wcu.cs.agora.FriendFinderServer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
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
    public static final int SOCKET_ERROR = 1;

    public static final int TIMEOUT = 5000;
    public static final int DEFAULT_PORT = 1337;

    private ServerSocket serverSocket;
    private int port;

    public RequestServer(int port) throws IOException
    {
        System.err.println("RequestServer created.");
        this.port = port;
        serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(port);
        //serverSocket = new ServerSocket(port);
    }

    public void listen() throws IOException
    {
        System.err.println("RequestServer listening for new connection.");
        Socket client = serverSocket.accept();
        System.err.println("RequestServer accepted new connection.");
        client.setSoTimeout(TIMEOUT);
        Scanner in = new Scanner(client.getInputStream()).useDelimiter("\\A");
        while (in.hasNext())
        {
            System.out.println(in.next());
        }
        //Request request = Request.requestBuilder(in);
       // request.getResponse();
    }

    public static void main(String[] args)
    {
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

        while (true)
        {
            try
            {
                requestServer.listen();
            }
            catch (IOException e)
            {
                System.err.println("Issue while listening.");
            }
        }
    }
}
