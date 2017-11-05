package com.scalablecomputing.server;

import java.io.PrintStream;
import java.util.Map.Entry;
import com.scalablecomputing.server.Message;

public class HelperFunctions {

	public String decode(String readLine) {
		String msg = readLine.replace("$$", "\n");
		return msg;
	}
	public Message processJoinMessage(String s1,String s2,String s3,String s4, PrintStream os) {
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
			System.out.println("\nAfter decoding: "+outMessage);
			//Send the client name to all members of this chat room
			PrintStream os2;
			String strmsg = "CHAT: "+ Storage.chatRooms.get(s1Val) +
							"\nCLIENT_NAME: "+s4Val+
							"\nMESSAGE: "+ s4Val;
			os.println(strmsg);
			for (Entry<Integer, PrintStream> entry : Storage.writers.entrySet()) {
				System.out.println(entry.getKey().toString());
				if(String.valueOf(Storage.clients.get(entry.getKey()))==s1Val) {
					os2 = entry.getValue();
		        	os2.println(strmsg);
				}
			}

			return msg;
		}
		else {
			msg.setErrorCode("1");
			msg.setErrorDescription("Input Message not valid");
			return msg;
		}
	}
	public String makeReplyMessage(Message inPacket) {



		Message outPacket = new Message();
		outPacket.setJoinedChatroom(inPacket.JOIN_CHATROOM);
		outPacket.setServerIp(keywords.serverIp);
		outPacket.setPORT(keywords.serverPort);
		outPacket.setRoomRef(Storage.chatRooms.get(inPacket.JOIN_CHATROOM).toString());
		outPacket.setJoinId(String.valueOf(Thread.currentThread().getId()));		//Using ThreadID as join ID
		String reply = outPacket.joinReplyToString();

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
		Message msg = new Message();
		if(s1.startsWith("CHAT") && s2.startsWith("JOIN_ID") && s3.startsWith("CLIENT_NAME") && s4.startsWith("MESSAGE")) {
			String[] parts = s1.split(": ");
			String s1Val = parts[1];
			parts = s2.split(": ");
			parts = s3.split(": ");
			String s3Val = parts[1];
			parts = s4.split(": ");
			String s4Val = parts[1];

			//check if chatroom exists
			if(!Storage.chatRooms.containsValue(s1Val)) { //Create new chatroom
				msg.setErrorCode("1");
				msg.setErrorDescription("Input Message not valid");
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

			return true;
		}
		else {
			msg.setErrorCode("1");
			msg.setErrorDescription("Input Message not valid");
			return false;
		}
	}
	public void processHeloMessage(String helo, PrintStream os) {
		String strmsg=null;
		//TODO: Change IP and port
		strmsg = helo + "\nIP: 10.62.0.59\nPort: 8089\nStudentID: 17306092\n";
		System.out.println("Processing Helo msg");
		os.print(strmsg);
		System.out.println("Wrote to OS");
	}
	
}
