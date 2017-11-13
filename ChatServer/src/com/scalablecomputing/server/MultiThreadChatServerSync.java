package com.scalablecomputing.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * A chat server that delivers public and private messages.
 */
public class MultiThreadChatServerSync {

	// The server socket.
	public static ServerSocket serverSocket = null;
	// The client socket.
	public static Socket clientSocket = null;
	public static BufferedReader is=null;
	// This chat server can accept up to maxClientsCount clients' connections.
	public static final int maxClientsCount = 50;
	//Data Structure to store all threads
	public static final clientThread[] threads = new clientThread[maxClientsCount];
	//Flag to check continuation
	public static Boolean cont = true;

	//Args will take port number as the first argument and the ip address as the second argument
	public static void main(String args[]) {

		
		Storage.charRoomsIndex=0;
		// The default port number and IP
		int portNumber = 8089;
		String ip = "127.0.0.1";
		
		
		if (args.length < 2) {
			System.out.println("ERROR: If You can see this error, there is a problem in the startup.sh script."
					+ " But the server is still running on localhost and port 8089 "
					+ "\nUsage: java MultiThreadChatServerSync <portNumber> <I.P>\n");
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
			ip = args[1];
		}
		if(ip.isEmpty()){
			System.out.println("IP address is empty");
			return;
		}
		if(portNumber<1024){
			System.out.println("Invalid Port Number");
			return;
		}
		HelperFunctions.loadProperties(ip,portNumber);

		/*
		 * Open a server socket on the portNumber. Note that we can
		 * not choose a port less than 1023 if we are not privileged users (root).
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
			System.out.println("Server Started on IP: "+ ip+ " Port: "+portNumber);

		} catch (IOException e) {
			System.out.println("Error Opening socket");
			System.out.println(e);
			e.printStackTrace();
		}

		/*
		 * Create a client socket for each connection and pass it to a new client reader
		 * thread.
		 */
		while (cont) {
			try {
				System.out.println("Waiting for connection");
				if((cont==true) && (null!=serverSocket) && !(serverSocket.isClosed()))
				{
					clientSocket = serverSocket.accept();
					is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					String s = new String();
					s = is.readLine();
					System.out.println("Got a connetion creating thread");
					int i = 0;
					for (i = 0; i < maxClientsCount; i++) {
						if (threads[i] == null) {
							(threads[i] = new clientThread(clientSocket, threads,s,is)).start();
							break;
						}
					}

				}else{
					System.out.println("Stopping");
					return;
				}
			} catch (IOException e) {
				System.out.println("Exception in main method:::");
				System.out.println(e);
				e.printStackTrace();
				System.out.println("Exception in main method:::");
				return;
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
 * It reads all messages as per protocol and creates a new thread for the processing.
 * The response is also done by the other thread.
 * This thread can be treated as a reader
 */
class clientThread extends Thread {

	Socket clientSocket = null;
	BufferedReader is;
	PrintStream os;
	private final clientThread[] threads;
	private int maxClientsCount;
	String[] s = new String[100] ;
	Boolean flag = false;
	public clientThread(Socket clientSocket, clientThread[] threads,String s,BufferedReader is) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		this.maxClientsCount = threads.length;
		this.s[0]=s; 
		this.is=is;
	}

	public void run() {
		System.out.println("Main Thread "+Thread.currentThread().getId()+" : Created a thread");
		flag=false;
		try {
			os = new PrintStream(clientSocket.getOutputStream());
			while(true) {
				System.out.println("Waiting for input");
				if(flag){
					s[0]=is.readLine();
				}
				System.out.println("FirstLine: "+s[0]+"\n");
				if(null != s[0] && s[0].startsWith("JOIN_CHATROOM: ")) {
					s[1] = is.readLine();
					s[2] = is.readLine();
					s[3] = is.readLine();
					System.out.println("Input JOIN_CHATROOM Message:\n"+s[0]+s[1]+s[2]+s[3]);
				}else if(null != s[0] && s[0].startsWith("LEAVE_CHATROOM: ")) {
					s[1] = is.readLine();
					s[2] = is.readLine();
					System.out.println("Input LEAVE_CHATROOM Message:\n"+s[0]+s[1]+s[2]);
				}else if(null != s[0] && s[0].startsWith("CHAT: ")) {
					s[1] = is.readLine();
					s[2] = is.readLine();
					int i = 3;
					while(true){
						s[i] = is.readLine();
						if(s[i].isEmpty()){
							break;
						}
						i++;
					}
					System.out.println("Input CHAT Message:\n"+s[0]+s[1]+s[2]+s[3]+s[4]);
				}else if(null != s[0] && s[0].startsWith("KILL_SERVICE")) {
					System.out.println("Input KILL_SERVICE Message:\n"+s[0]);
					/*
					* Clean up. 
					*/
					synchronized (this) {
						for (int i = 0; i < maxClientsCount; i++) {
							if (threads[i] != null && threads[i] !=this) {
								System.out.println("Closing thread: "+i);
								System.out.println("PRE: socket closed?: "+threads[i].clientSocket.isClosed());
								threads[i].is.close();
								threads[i].os.close();
								threads[i].clientSocket.close();
								System.out.println("POST: socket closed?: "+threads[i].clientSocket.isClosed());
								threads[i] = null;
							}
						}
					}
					MultiThreadChatServerSync.cont=false;
					System.out.println("Closing IS");
					is.close();
					System.out.println("Closing OS");
					os.close();
					System.out.println("Closing IS in main");
					MultiThreadChatServerSync.is.close();
					System.out.println("Closing clientsocket in main");
					MultiThreadChatServerSync.clientSocket.close();
					System.out.println("Closing serversocket in main");
					MultiThreadChatServerSync.serverSocket.close();
					System.out.println("Closing serversocket in main again");
					MultiThreadChatServerSync.serverSocket.close();
					System.out.println("Setting it to null");
					MultiThreadChatServerSync.serverSocket=null;
					System.out.println("Done everything");
					System.exit(0);
					break;
				}else if(null != s[0] && s[0].startsWith("HELO ")) {
					System.out.println("Input HELO Message:\n"+s[0]);
				}else if(null != s[0] && s[0].startsWith("DISCONNECT: ")){
					s[1] = is.readLine();
					s[2] = is.readLine();
					HelperFunctions hf = new HelperFunctions();
					hf .processDisconnectMessage(s[0],s[1],s[2],os);
					clientSocket.close();
					return;
				}else if(null == s[0]){
					break;
				}else {
					System.out.println("Input ERROR Message:\n"+s[0]);
					HelperFunctions hf = new HelperFunctions();
					hf .processErrorMessage(s[0],os);
				}
				new ClientWriterThread(os,s).start();
				flag=true;
			}
		} catch (IOException e) {
			System.out.println("IO Exception in main Thread::"+e +"::");
			e.printStackTrace();
		}
	}
}
