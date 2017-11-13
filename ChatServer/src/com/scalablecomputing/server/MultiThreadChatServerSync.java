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

	// This chat server can accept up to maxClientsCount clients' connections.
	public static final int maxClientsCount = 10;
	public static final clientThread[] threads = new clientThread[maxClientsCount];
	public static Boolean cont = true;

	public static void main(String args[]) {

		HelperFunctions.loadProperties();
		Storage.charRoomsIndex=0;

		// The default port number.
		int portNumber = 8089;
		if (args.length < 1) {
			System.out.println("Usage: java MultiThreadChatServerSync <portNumber>\n"
					+ "Now using port number=" + portNumber +"\nAnd IP= ");
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
				if(cont)
				{
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
				}else{
					System.out.println("Stopping");
					serverSocket.close();
					clientSocket.close();
					System.exit(0);
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

	private Socket clientSocket = null;
	BufferedReader is;
	PrintStream os;
	private final clientThread[] threads;
	private int maxClientsCount;
	
	public clientThread(Socket clientSocket, clientThread[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		 maxClientsCount = threads.length;
	}

	public void run() {
		System.out.println("Main Thread "+Thread.currentThread().getId()+" : Created a thread");
		clientThread[] threads = this.threads;
		String[] s = new String[100] ;
		try {
			is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			os = new PrintStream(clientSocket.getOutputStream());
			while(true) {

				//Storage.count++;
				//System.out.println("******Start  "+Thread.currentThread().getId()+"  MainThread: Looped for count: "+ Storage.count+ "********");
				//System.out.println("******  "+Thread.currentThread().getId()+"  MainThread: ReadyState: "+ is.ready()+ "********");
				System.out.println("Waiting for input");
				s[0]=is.readLine();
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
					//s[3] = is.readLine();
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
					MultiThreadChatServerSync.cont=false;
					MultiThreadChatServerSync.serverSocket.close();
					MultiThreadChatServerSync.clientSocket.close();
					is.close();
					os.close();
					clientSocket.close();
					
					 /*
				       * Clean up. Set the current thread variable to null so that a new client
				       * could be accepted by the server.
				       */
				      synchronized (this) {
				        for (int i = 0; i < maxClientsCount; i++) {
				          if (threads[i] != null && threads[i] !=this) {
				        	  threads[i].is.close();
				        	  threads[i].os.close();
				        	  threads[i].clientSocket.close();
				            threads[i] = null;
				          }
				        }
				      }
				      
					System.exit(0);
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
					System.out.println("Input NULL Message:\n"+s[0]);
					MultiThreadChatServerSync.cont=false;
					MultiThreadChatServerSync.serverSocket.close();
					MultiThreadChatServerSync.clientSocket.close();
					is.close();
					os.close();
					clientSocket.close();
					
					 /*
				       * Clean up. Set the current thread variable to null so that a new client
				       * could be accepted by the server.
				       */
				      synchronized (this) {
				        for (int i = 0; i < maxClientsCount; i++) {
				          if (threads[i] != null && threads[i] !=this) {
				        	  threads[i].is.close();
				        	  threads[i].os.close();
				        	  threads[i].clientSocket.close();
				        	  threads[i] = null;
				          }
				        }
				      }
				      
					System.exit(0);
					return;
				}else {
					System.out.println("Input ERROR Message:\n"+s[0]);
					HelperFunctions hf = new HelperFunctions();
					hf .processErrorMessage(s[0],os);
				}
				new ClientWriterThread(os,s).start();
			}

		} catch (IOException e) {
			System.out.println("IO Exception in main Thread::"+e +"::");
			e.printStackTrace();
		}finally{
			try {
				System.out.println("In finally");
				clientSocket.close();
			} catch (IOException e) {
				System.out.println("IO Exception in main Thread Finally block::"+e +"::");
				e.printStackTrace();
			}catch(NullPointerException npe){
				System.out.println("NullPointerException in main Thread Finally block::"+npe +"::");
				npe.printStackTrace();
			}


		}
	}


}
