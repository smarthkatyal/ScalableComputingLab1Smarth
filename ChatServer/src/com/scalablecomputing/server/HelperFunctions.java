package com.scalablecomputing.server;

import java.io.PrintStream;
import java.util.Map.Entry;
import com.scalablecomputing.server.Message;

public class HelperFunctions {

	public String decode(String readLine) {
		String msg = readLine.replace("$$", "\n");
		return msg;
	}
	public Boolean processJoinMessage(String s1,String s2,String s3,String s4, PrintStream os) {
		System.out.println("******Start "+Thread.currentThread().getId()+" : Processing Join Message******");
		Message msg = new Message();
		if(s1.startsWith("JOIN_CHATROOM") &&
				s2.startsWith("CLIENT_IP") &&
				s3.startsWith("PORT") &&
				s4.startsWith("CLIENT_NAME")) {

			String[] parts = s1.split(": ");
			String s1Val = parts[1];
			parts = s2.split(": ");
			String s2Val = parts[1];
			parts = s3.split(": ");
			String s3Val = parts[1];
			parts = s4.split(": ");
			String s4Val = parts[1];


			//check if chatroom already exists
			if(!Storage.chatRooms.containsKey(s1Val))//Create new chatroom
			{
				Storage.chatRooms.put(s1Val, Storage.charRoomsIndex);
				Storage.chatRoomsInverse.put(Storage.charRoomsIndex, s1Val);
				Storage.charRoomsIndex++;
			}
			msg.setJOIN_CHATROOM(s1Val);
			msg.setCLIENT_IP(s2Val);
			msg.setPORT(s3Val);
			msg.setCLIENT_NAME(s4Val);

			//No Error, Add the client and outputstream to storage
			Storage.writers.put(Integer.parseInt(String.valueOf(Thread.currentThread().getId())),os);
			Storage.clients.put(Integer.parseInt(String.valueOf(Thread.currentThread().getId())), Storage.chatRooms.get(s1Val));


			String outMessage = makeReplyMessage(msg);
			os.print(outMessage);
			System.out.println("\n  "+Thread.currentThread().getId()+" Send Reply for join Message to original client:\n"+outMessage);
			//Send the client name to all members of this chat room
			PrintStream os2;
			String strmsg = "CHAT: "+ Storage.chatRooms.get(s1Val) +
					"\nCLIENT_NAME: "+s4Val+
					"\nMESSAGE: "+ s4Val;
			//os.println(strmsg);
			for (Entry<Integer, PrintStream> entry : Storage.writers.entrySet()) {
				System.out.println(entry.getKey().toString());
				if(String.valueOf(Storage.clients.get(entry.getKey())).equalsIgnoreCase((String.valueOf(Storage.chatRooms.get(s1Val))))) {
					os2 = entry.getValue();
					System.out.println("******Start "+Thread.currentThread().getId()+" : Sending join message to other clients******");
					os2.println(strmsg);
					System.out.println(Thread.currentThread().getId()+" Send join Message to other clients of the chatroom:\n"+strmsg);
					System.out.println("******End  "+Thread.currentThread().getId()+" : Sending join message to other clients******");

				}
			}
			System.out.println("******End  "+Thread.currentThread().getId()+" : Processing Join Message******");
			return true;
		}
		else {
			System.out.println("****ERROR "+Thread.currentThread().getId()+" :  Processing Join Message*****");
			msg.setErrorCode("1");
			msg.setErrorDescription("Input Message not valid");
			return false;
		}

	}
	public String makeReplyMessage(Message inPacket) {
		System.out.println("******Start "+Thread.currentThread().getId()+" : In MakeReplyMessage******");


		Message outPacket = new Message();
		outPacket.setJoinedChatroom(inPacket.JOIN_CHATROOM);
		outPacket.setServerIp(keywords.serverIp);
		outPacket.setPORT(keywords.serverPort);
		outPacket.setRoomRef(Storage.chatRooms.get(inPacket.JOIN_CHATROOM).toString());
		outPacket.setJoinId(String.valueOf(Thread.currentThread().getId()));		//Using ThreadID as join ID
		String reply = outPacket.joinReplyToString();
		System.out.println("******End "+Thread.currentThread().getId()+" : In MakeReplyMessage******");
		return reply;

	}
	public static void loadProperties() {
		/*	try {
			prop.load(new FileInputStream("keywords.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		keywords.serverIp = prop.getProperty("serverIp");
		keywords.serverPort = prop.getProperty("serverPort");*/
		keywords.serverIp = "10.32.102.110";
		keywords.serverPort = "2223";
	}

	public boolean processChatMessage(String s1, String s2, String s3, String s4, PrintStream os) {
		System.out.println("******Start: In processChatMessage******");
		Message msg = new Message();
		if(s1.startsWith("CHAT") && s2.startsWith("JOIN_ID") && s3.startsWith("CLIENT_NAME") && s4.startsWith("MESSAGE")) {
			String[] parts = s1.split(": ");
			String s1Val = parts[1];
			parts = s2.split(": ");
			String s2Val = parts[1];
			parts = s3.split(": ");
			String s3Val = parts[1];
			parts = s4.split(": ");
			String s4Val = parts[1];

			//check if chatroom exists
			if(!Storage.chatRooms.containsValue(s1Val)) { //Create new chatroom
				msg.setErrorCode("1");
				msg.setErrorDescription("Input Message not valid");
				System.out.println("****ERROR 1:  Processing Chat Message*****");
				return false;
			}
			PrintStream os2;
			String strmsg=null;
			strmsg = "CHAT: "+s1Val +"\n"
					+"CLIENT_NAME: "+s3Val +"\n"
					+"MESSAGE: "+ s4Val +"\n\n";
			//Iterate over all output streams, then check if they belong to this particular chat group and if true send the message
			for (Entry<Integer, PrintStream> entry : Storage.writers.entrySet()) {
				//System.out.println(pair.getKey() + " = " + pair.getValue());
				if(String.valueOf(Storage.clients.get(entry.getKey()))==s1Val) {	//Condition when iterator is pointing to a client which is in the same chat room
					os2 = entry.getValue();
					if(os2!=os)	//Dont send message to same client who sent it
						os2.println(strmsg);
				}
			}
			System.out.println("******End: In processChatMessage******");

			return true;
		}
		else {
			System.out.println("****ERROR 2:  Processing Chat Message*****");
			msg.setErrorCode("1");
			msg.setErrorDescription("Input Message not valid");
			return false;
		}
	}
	public void processHeloMessage(String helo, PrintStream os) {
		System.out.println("******Start "+Thread.currentThread().getId()+" : In processHeloMessage******");
		String strmsg=null;
		//TODO: Change IP and port
		strmsg = helo + "\nIP: 10.62.0.59\nPort: 8089\nStudentID: 17306092\n";
		os.print(strmsg);
		System.out.println("******End  "+Thread.currentThread().getId()+" : In processHeloMessage******");
	}

	public Boolean processLeaveMessage(String s1, String s2, String s3, PrintStream os) {
		System.out.println("******Start: In processLeaveMessage******");
		if(s1.startsWith("LEAVE_CHATROOM") 
				&& s2.startsWith("JOIN_ID") 
				&& s3.startsWith("CLIENT_NAME")) {
			String[] parts = s1.split(": ");
			String s1Val = parts[1];
			parts = s2.split(": ");
			String s2Val = parts[1];
			parts = s3.split(": ");
			String s3Val = parts[1];


			//check if chatroom exists
			if(!Storage.chatRooms.containsValue(s1Val)) { //Create new chatroom
				/*msg.setErrorCode("1");
				msg.setErrorDescription("Input Message not valid");*/
				System.out.println("****ERROR 1:  Processing Leave  Message*****");
				return false;
			}
			PrintStream os2;
			String strmsg=null;
			strmsg = "LEFT_CHATROOM: "+s1Val +"\n"
					+"JOIN_ID: "+s2Val +"\n"
					+"MESSAGE: "+ s3Val +"\n\n";
			//Iterate over all output streams, then check if they belong to this particular chat group and if true send the message
			for (Entry<Integer, PrintStream> entry : Storage.writers.entrySet()) {
				//System.out.println(pair.getKey() + " = " + pair.getValue());
				if(String.valueOf(Storage.clients.get(entry.getKey()))==s1Val) {	//Condition when iterator is pointing to a client which is in the same chat room
					os2 = entry.getValue();
					os2.println(strmsg);
					if(os2==os) {
						//Remove that clients OS
						Storage.writers.remove(entry.getKey());
						//Remove that client from chatroom list
						Storage.clients.remove(entry.getKey());
					}

				}
			}
			System.out.println("******End: In processLeaveMessage******");
			return true;
		}
		else {
			System.out.println("****ERROR 2:  Processing leave Message*****");
			/*msg.setErrorCode("1");
			msg.setErrorDescription("Input Message not valid");*/
			return false;
		}

	}
	public void processErrorMessage(String string, PrintStream os) {

		String msg = "ERROR_CODE: 1\nERROR_DESCRIPTION: Invalid Input\n";
		os.print(msg);
		System.out.println("Error MessageSent: \n"+ msg);
	}

}
