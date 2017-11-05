package com.scalablecomputing.server;
import java.io.DataInputStream;

import com.scalablecomputing.server.Message;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.net.ServerSocket;

/*
 * A chat server that delivers public and private messages.
 */
public class MultiThreadChatServerSync {

	// The server socket.
	private static ServerSocket serverSocket = null;
	// The client socket.
	private static Socket clientSocket = null;

	// This chat server can accept up to maxClientsCount clients' connections.
	private static final int maxClientsCount = 10;
	private static final clientThread[] threads = new clientThread[maxClientsCount];

	public static void main(String args[]) {

		HelperFunctions.loadProperties();
		Storage.charRoomsIndex=0;
		String ip = "127.0.0.1";
		// The default port number.
		int portNumber = 8089;
		if (args.length < 1) {
			System.out.println("Usage: java MultiThreadChatServerSync <portNumber>\n"
					+ "Now using port number=" + portNumber +"\nAnd IP= "+ ip);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
		}


		/*
		 * Open a server socket on the portNumber (default 2222). Note that we can
		 * not choose a port less than 1023 if we are not privileged users (root).
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
			System.out.println("Server Started");

		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 * Create a client socket for each connection and pass it to a new client
		 * thread.
		 */
		while (true) {
			try {
				System.out.println("Waiting for connection");
				clientSocket = serverSocket.accept();
				System.out.println("Got a connetion creating thread");
				int i = 0;
				for (i = 0; i < maxClientsCount; i++) {
					if (threads[i] == null) {
						(threads[i] = new clientThread(clientSocket, threads)).start();
						break;
					}
				}
				if (i == maxClientsCount) {
					PrintStream os = new PrintStream(clientSocket.getOutputStream());
					os.println("Server too busy. Try later.");
					os.close();
					clientSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. The thread broadcast the incoming messages to all clients and
 * routes the private message to the particular client. When a client leaves the
 * chat room this thread informs also all the clients about that and terminates.
 */
class clientThread extends Thread {

	private DataInputStream is = null;
	private PrintStream os = null;
	private Socket clientSocket = null;
	private final clientThread[] threads;
	private int maxClientsCount;


	public clientThread(Socket clientSocket, clientThread[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientsCount = threads.length;

	}

	public void run() {
		System.out.println("Created a thread");
		int maxClientsCount = this.maxClientsCount;
		clientThread[] threads = this.threads;
		HelperFunctions hf = new HelperFunctions();
		Message inPacket = new Message();
		String outMessage =null;
		String[] s = new String[4];
		try {
			/*
			 * Create input and output streams for this client.
			 */
			is = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());
			//System.out.println("\nRead Line from client: "+is.readLine());
			String line = null;
			int lines=0;
			//==line = is.readLine();
			//line = is.readLine();

			//is.readFully(buffer)

			s[0]=is.readLine();
			lines++;
			System.out.println("FirstLine: "+line);

			/*
			@SuppressWarnings("deprecation")
			String s1= is.readLine();
			@SuppressWarnings("deprecation")
			String s2= is.readLine();
			@SuppressWarnings("deprecation")
			String s3= is.readLine();
			@SuppressWarnings("deprecation")
			String s4= is.readLine();
						while(true) {
				line = is.readLine();
				s[lines]=line;
				lines++;
				System.out.println(line);
				if(line==null||line.isEmpty()||(!line.contains("\n")))
					break;
			}*/
			if(s[0].startsWith("JOIN_CHATROOM: ")) {
				System.out.println("In join chatroom");
				s[1] = is.readLine();
				s[2] = is.readLine();
				s[3] = is.readLine();
				inPacket = hf.processJoinMessage(s[0],s[1],s[2],s[3],os);
				//If pre-processing is successful send reply
				//outMessage = hf.makeReplyMessage(inPacket);
				//os.print(outMessage);
				//System.out.println("\nAfter decoding: "+outMessage);
			}else if(s[0].startsWith("LEFT_CHATROOM: ")) {
				System.out.println("in leave block");
			}else if(s[0].startsWith("CHAT: ")) {
				System.out.println("In Chat Block");
				//Pre-Processing
				s[1] = is.readLine();
				s[2] = is.readLine();
				s[3] = is.readLine();
				hf.processChatMessage(s[0],s[1],s[2],s[3],os);
				//If pre-processing is successful send reply
				//outMessage = hf.makeChatReplyMessage(inPacket);
				//os.print(outMessage);
				//System.out.println("\nAfter decoding: "+outMessage);
			}else if(s[0].startsWith("HELO ")) {
				System.out.println("In hello block");
				hf.processHeloMessage(s[0],os);
			}
			else if(s[0].contains("KILL_SERVICE")) {
				System.out.println("In Kill Service");
				synchronized (this) {
					for (int i = 0; i < maxClientsCount; i++) {
						if (threads[i] == this) {
							threads[i] = null;
						}
					}
				}
				/*
				 * Close the output stream, close the input stream, close the socket.
				 */
				is.close();
				os.close();
				clientSocket.close();
				System.exit(0);
			}else {
				System.out.println("Error");
			}




			/*
			 * Clean up. Set the current thread variable to null so that a new client
			 * could be accepted by the server.
			 */
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			/*
			 * Close the output stream, close the input stream, close the socket.
			 */
			is.close();
			os.close();
			clientSocket.close();
		} catch (IOException e) {
			System.out.println("IO Exception::"+e +"::");
			e.printStackTrace();
		}
	}


}
