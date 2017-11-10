package com.scalablecomputing.server;

import java.awt.List;
import java.io.PrintStream;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

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
			Set<Integer> roomRefSet = new HashSet<Integer>();
			if(Storage.clients.get(Integer.parseInt(String.valueOf(Thread.currentThread().getId()))) != null){
				roomRefSet = Storage.clients.get(Integer.parseInt(String.valueOf(Thread.currentThread().getId())));
			}
			roomRefSet.add(Storage.chatRooms.get(s1Val));

			Storage.writers.put(Integer.parseInt(String.valueOf(Thread.currentThread().getId())),os);
			Storage.clients.put(Integer.parseInt(String.valueOf(Thread.currentThread().getId())), roomRefSet);
			
			Set<Integer> cidList = new HashSet<Integer>();
			if(null!= Storage.clientNames.get(s4Val)&& !Storage.clientNames.get(s4Val).isEmpty())
				cidList = Storage.clientNames.get(s4Val);
			cidList.add(Integer.parseInt(String.valueOf(Thread.currentThread().getId())));
			Storage.clientNames.put(s4Val,cidList);
			
			String outMessage = makeReplyMessage(msg);
			os.print(outMessage);
			System.out.println("Output  "+os+" JOIN_CHATROOM\n" +  outMessage);
			//System.out.println("\n  "+Thread.currentThread().getId()+" Send Reply for join Message to original client:\n"+outMessage);
			//Send the client name to all members of this chat room
			PrintStream os2;
			String strmsg = "CHAT: "+ Storage.chatRooms.get(s1Val) +
					"\nCLIENT_NAME: "+s4Val+
					"\nMESSAGE: "+ s4Val;
			//os.println(strmsg);
			for (Entry<Integer, PrintStream> entry : Storage.writers.entrySet()) {
				System.out.println(entry.getKey().toString());
				if(Storage.clients.get(entry.getKey()).contains(Storage.chatRooms.get(s1Val))) {
					os2 = entry.getValue();
					if(os!=os2){
						os2.println(strmsg);
						System.out.println("Output  "+os2+"  JOIN_CHATROOM\n" +  strmsg);
					}
					//System.out.println("******Start "+Thread.currentThread().getId()+" : Sending join message to other clients******");
					//os2.print(strmsg);
					//System.out.println(Thread.currentThread().getId()+" Send join Message to other clients of the chatroom:\n"+strmsg);
					//System.out.println("******End  "+Thread.currentThread().getId()+" : Sending join message to other clients******");

				}
			}
			os.println(strmsg);
			System.out.println("Output  "+os+"  JOIN_CHATROOM\n" +  strmsg);
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
		keywords.serverIp = "134.226.50.92";
		keywords.serverPort = "8089";
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
			if(!Storage.chatRoomsInverse.containsKey(Integer.parseInt(s1Val))) { //Create new chatroom
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
			os.print(strmsg);
			//Iterate over all output streams, then check if they belong to this particular chat group and if true send the message
			for (Entry<Integer, PrintStream> entry : Storage.writers.entrySet()) {
				//System.out.println(pair.getKey() + " = " + pair.getValue());
				if(Storage.clients.get(entry.getKey()).contains(Integer.parseInt(s1Val))) {	//Condition when iterator is pointing to a client which is in the same chat room
					os2 = entry.getValue();
					if(os2!=os)	//Dont send message to same client who sent it
						os2.print(strmsg);

					System.out.println("Output CHAT: \n" +  strmsg);
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
		strmsg = helo + "\nIP: 134.226.50.91\nPort: 8089\nStudentID: 17306092";
		os.print(strmsg);
		System.out.println("Output "+os+" HELO: \n" +  strmsg);
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
			if(!Storage.chatRoomsInverse.containsKey(Integer.parseInt(s1Val))) { //Create new chatroom
				/*msg.setErrorCode("1");
				msg.setErrorDescription("Input Message not valid");*/
				System.out.println("****ERROR 1:  Processing Leave  Message*****");
				return false;
			}
			PrintStream os2;
			String strmsg=null;
			strmsg = "LEFT_CHATROOM: "+s1Val +"\n"
					+"JOIN_ID: "+s2Val+"\n";

			//+"MESSAGE: "+ s3Val +"\n\n";
			String strmsg2 = "CHAT: "+s1Val +"\n"
					+"CLIENT_NAME: "+s3Val +"\n"
					+"MESSAGE: "+ s3Val;
			strmsg+=strmsg2;
			System.out.println("Output "+os+" LEAVE_CHATROOM: \n" +  strmsg);
			os.println(strmsg);

			//Alter the clients chatroom set
			Set<Integer> tempSet = Storage.clients.get(Integer.parseInt(s2Val));
			tempSet.remove(s1Val);
			Storage.clients.remove(Integer.parseInt(s2Val));
			Storage.clients.put(Integer.parseInt(s2Val),tempSet);
			Storage.writers.remove(Integer.parseInt(s2Val));
			//if(tempSet!=null && tempSet.size()==0)
				


			int key=-1;
			//Iterate over all output streams, then check if they belong to this particular chat group and if true send the message
			try{

				for (Entry<Integer, PrintStream> entry : Storage.writers.entrySet()) {
					key=-1;
					//System.out.println(pair.getKey() + " = " + pair.getValue());
					//Condition when iterator is pointing to a client which is in the same chat room
					if(Storage.clients.get(entry.getKey()).contains((Integer.parseInt(s1Val)))) {	
						os2 = entry.getValue();
						key = entry.getKey();
						if(os2!=os) {
							os2.println(strmsg2);
							System.out.println("Output "+os2+" LEAVE_CHATROOM: \n" +  strmsg2);
						}
					}
				}
			}catch(Exception se){
				System.out.println("Exception in processing leave message" + se);
				se.printStackTrace();
			}finally{
				if(key>-1){
					Set<Integer> tempSet1 = Storage.clients.get(key);
					Storage.clients.remove(key);
					if(tempSet1!=null){
						tempSet1.remove(s1Val);
						Storage.clients.put(key,tempSet1);
					}
					Storage.writers.remove(key);


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
		System.out.println("Output ERROR:\n " +  msg);
	}
	public void processDisconnectMessage(String s1,String s2, String s3, PrintStream os) {
		if(s1.startsWith("DISCONNECT") 
				&& s2.startsWith("PORT") 
				&& s3.startsWith("CLIENT_NAME")) {
			String[] parts = s1.split(": ");
			String s1Val = parts[1];
			parts = s2.split(": ");
			String s2Val = parts[1];
			parts = s3.split(": ");
			String s3Val = parts[1];



			
			//os.println(strmsg2);

			Set<Integer> cidList = Storage.clientNames.get(s3Val);
			Set<Integer> roomRefSet = new HashSet<Integer>();
			for(Integer cid : cidList){
				if(null!= Storage.clients.get(cid)){
					roomRefSet.addAll(Storage.clients.get(cid));
					System.out.println("Got Null in processing disconnect");
				}
			}
			Integer key=-1;
			for (Entry<Integer, PrintStream> entry : Storage.writers.entrySet()) {
				//key=-1;
				//System.out.println(pair.getKey() + " = " + pair.getValue());
				//Condition when iterator is pointing to a client which is in the same chat room
				for(Integer entry2 : roomRefSet){
					if(Storage.clients.get(entry.getKey()).contains(entry2)) {	
						PrintStream os2 = entry.getValue();
						//key = entry.getKey();
						String strmsg2 = "CHAT: "+entry2.toString()+"\n"
								+"CLIENT_NAME: "+s3Val +"\n"
								+"MESSAGE: "+ s3Val;
						os2.println(strmsg2);
						System.out.println("Output "+os2+" DISCONNECT: \n" +  strmsg2+"\nClient ID: "+ entry.getKey() );
						if(os2==os) {
							//os.println(strmsg2);
							System.out.println("Same");
							//Remove that clients OS
							key = entry.getKey();
						}
					}
				}
			}
			if(key!=-1){
				Storage.writers.remove(key);
				//Remove that client from chatroom list
				Storage.clients.remove(key);
			}
		}
	}

}
