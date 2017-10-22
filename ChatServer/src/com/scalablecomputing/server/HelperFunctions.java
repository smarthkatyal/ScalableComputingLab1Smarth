package com.scalablecomputing.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import com.scalablecomputing.server.Message;

public class HelperFunctions {

	public String decode(String readLine) {
		String msg = readLine.replace("$$", "\n");
		return msg;
	}
	public Message processJoinMessage(String s1,String s2,String s3,String s4) {
		Message msg = new Message();
		if((s1.indexOf("JOIN_CHATROOM")>0) && (s2.indexOf("CLIENT_IP")>0) && (s3.indexOf("PORT")>0) && (s4.indexOf("CLIENT_NAME")>0)) {
			int i1 = s1.indexOf("JOIN_CHATROOM"); 
			int i2 = s2.indexOf("CLIENT_IP");
			int i3 = s3.indexOf("PORT");
			int i4 = s4.indexOf("CLIENT_NAME");
			
			String s1Val = s1.substring(i1);
			String s2Val = s2.substring(i2);
			String s3Val = s3.substring(i3);
			String s4Val = s4.substring(i4);
			
			msg.setJOIN_CHATROOM(s1Val);
			msg.setCLIENT_IP(s2Val);
			msg.setPORT(s3Val);
			msg.setCLIENT_NAME(s4Val);
			return msg;
		}
		else {
			msg.setErrorCode("1");
			msg.setErrorDescription("Input Message not valid");
			return msg;
		}
	}
	public Message makeReplyMessage(Message inPacket) {
		
		//Generate JoinID
		
		
		Message outPacket = new Message();
		outPacket.setJoinedChatroom(inPacket.JOIN_CHATROOM);
		outPacket.setServerIp(keywords.serverIp);
		outPacket.setPORT(keywords.serverPort);
		outPacket.setRoomRef(keywords.roomRef);
		outPacket.setJoinId(String.valueOf(Thread.currentThread().getId()));
		
		return inPacket;
		// TODO Auto-generated method stub
		
	}
	public static void loadProperties() {
		Properties prop = new Properties();

		try {
		    prop.load(new FileInputStream("conf/keywords.properties"));
		} catch (IOException e) {
		    e.printStackTrace();
		}
		keywords.serverIp = prop.getProperty("serverIp");
		keywords.serverPort = prop.getProperty("serverPort");
	}
}
