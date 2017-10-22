package com.scalablecomputing.server;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MultiThreadChatClient {

  // The client socket
  private static Socket clientSocket = null;
  // The output stream
  private static PrintStream os = null;
  // The input stream
  private static DataInputStream is = null;

  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  
  /*
   * JOIN_CHATROOM: [chatroom name]
		  CLIENT_IP: [IP Address of client if UDP | 0 if TCP]
		  PORT: [port number of client if UDP | 0 if TCP]
		  CLIENT_NAME: [string Handle to identifier client user]
   */
  public static String join(String chatroomName, String clientName ) {
	  String msg = "JOIN_CHATROOM: " + chatroomName + "\nCLIENT_IP: 0\nPORT: 0\nCLIENT_NAME: " + clientName;
	  return msg;
  }
  public static void main(String[] args) {

    // The default port.
    int portNumber = 2223;
    // The default host.
    String host = "localhost";
    String msg = "";

    if (args.length < 2) {
      System.out
          .println("Usage: java MultiThreadChatClient <host> <portNumber>\n"
              + "Now using host=" + host + ", portNumber=" + portNumber);
    } else {
      host = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }

    /*
     * Open a socket on a given host and port. Open input and output streams.
     */
    try {
      clientSocket = new Socket(host, portNumber);
      msg = join("Whatsapp","firstclient");
      //inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(clientSocket.getOutputStream());
      is = new DataInputStream(clientSocket.getInputStream());
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host " + host);
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to the host "
          + host);
    }

    /*
     * If everything has been initialized then we want to write some data to the
     * socket we have opened a connection to on the port portNumber.
     */
    if (clientSocket != null && os != null && is != null) {
      try {

        /* Create a thread to read from the server. */
        //new Thread(new MultiThreadChatClient()).start();
          os.println(msg);
          
        /*
         * Close the output stream, close the input stream, close the socket.
         */
        os.close();
        is.close();
        clientSocket.close();
      } catch (IOException e) {
        System.err.println("IOException:  " + e);
      }
    }
  }

}