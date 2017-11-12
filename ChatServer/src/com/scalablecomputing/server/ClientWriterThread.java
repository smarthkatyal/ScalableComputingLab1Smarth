package com.scalablecomputing.server;

import java.io.PrintStream;

public class ClientWriterThread extends Thread {
	private PrintStream os = null;
	private String[] s;


	public ClientWriterThread(PrintStream os1, String[] s1) {
		this.s = s1;
		this.os = os1;
	}

	public void run() {
		System.out.println("ClientWriter "+Thread.currentThread().getId()+ " : Created a thread for input "+s[0]);
		HelperFunctions hf = new HelperFunctions();

		if(s[0].startsWith("JOIN_CHATROOM: ")) {
			System.out.println("****Start "+Thread.currentThread().getId()+" WriterThread: In join chatroom if block****");
			hf.processJoinMessage(s[0],s[1],s[2],s[3],os);
			System.out.println("****End "+Thread.currentThread().getId()+" WriterThread: In join chatroom if block****");
		}else if(s[0].startsWith("LEAVE_CHATROOM: ")) {
			System.out.println("****Start "+Thread.currentThread().getId()+"  WriterThread: In leave chatroom if block****");
			hf.processLeaveMessage(s[0],s[1],s[2],os);
			return;
		}else if(s[0].startsWith("CHAT: ")) {
			System.out.println("****Start  "+Thread.currentThread().getId()+" WriterThread: In chat if block****");
			hf.processChatMessage(s[0],s[1],s[2],s[3],os);
			System.out.println("****End "+Thread.currentThread().getId()+"  WriterThread: In chat if block****");
		}else if(s[0].startsWith("HELO ")) {
			System.out.println("****Start  "+Thread.currentThread().getId()+" WriterThread: In hello if block****");
			hf.processHeloMessage(s[0],os);
			System.out.println("****End  "+Thread.currentThread().getId()+" WriterThread: In hello chatroom if block****");
			return;
		}
	}
}

