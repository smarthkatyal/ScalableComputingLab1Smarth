package com.scalablecomputing.server;

import java.awt.List;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {

	static  HashMap<String,Integer> chatRooms=new HashMap<String,Integer>();  //Stores chatroom name with chatroom index ::         	||ChatRoomName|ChatroomID||
	static  HashMap<Integer,String> chatRoomsInverse=new HashMap<Integer,String>();  //Stores chatroom id with chatroom name ::         ||ChatRoomID|ChatRoomName||
	static int charRoomsIndex=0;		//Number of chatrooms
	static ConcurrentHashMap<Integer, PrintStream> writers = new ConcurrentHashMap<Integer,PrintStream>();	//Stores the writer for each client id::	    ||ClientID|WriterObj||
	static ConcurrentHashMap<Integer,Set<Integer>> clients = new ConcurrentHashMap<Integer,Set<Integer>>();	//Stores which client id belongs to which chatroom::	||ClientID|ChatroomID||
	static HashMap<String,Set<Integer>> clientNames = new HashMap<String,Set<Integer>>();		//Stores the names of all clients::					||Name|ClientID||
	static int count=0;
}
