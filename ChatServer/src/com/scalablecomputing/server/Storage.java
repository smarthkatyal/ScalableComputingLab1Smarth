package com.scalablecomputing.server;

import java.io.PrintStream;
import java.util.HashMap;

public class Storage {

	static  HashMap<String,Integer> chatRooms=new HashMap<String,Integer>();  //Stores chatroom name with chatroom index ::         	||ChatRoomName|ChatroomID||
	static int charRoomsIndex=0;		//Number of chatrooms
	static HashMap<Integer,PrintStream> writers = new HashMap<Integer,PrintStream>();	//Stores the writer for each client id::	    ||ClientID|WriterObj||
	static HashMap<Integer,Integer> clients = new HashMap<Integer,Integer>();	//Stores which client id belongs to which chatroom::	||ClientID|ChatroomID||
}
