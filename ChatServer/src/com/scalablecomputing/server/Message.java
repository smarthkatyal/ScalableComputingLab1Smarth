package com.scalablecomputing.server;

public class Message {


	String JOIN_CHATROOM;
	String CLIENT_IP;
	String PORT;
	String CLIENT_NAME;
	String errorCode;
	String errorDescription;
	String joinedChatroom;
	String serverIp;
	String roomRef;
	String joinId;

	public String getJOIN_CHATROOM() {
		return JOIN_CHATROOM;
	}
	public void setJOIN_CHATROOM(String jOIN_CHATROOM) {
		JOIN_CHATROOM = jOIN_CHATROOM;
	}
	public String getCLIENT_IP() {
		return CLIENT_IP;
	}
	public void setCLIENT_IP(String cLIENT_IP) {
		CLIENT_IP = cLIENT_IP;
	}
	public String getPORT() {
		return PORT;
	}
	public void setPORT(String pORT) {
		PORT = pORT;
	}
	public String getCLIENT_NAME() {
		return CLIENT_NAME;
	}
	public void setCLIENT_NAME(String cLIENT_NAME) {
		CLIENT_NAME = cLIENT_NAME;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorDescription() {
		return errorDescription;
	}
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
	public String getJoinedChatroom() {
		return joinedChatroom;
	}
	public void setJoinedChatroom(String joinedChatroom) {
		this.joinedChatroom = joinedChatroom;
	}
	public String getServerIp() {
		return serverIp;
	}
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	public String getRoomRef() {
		return roomRef;
	}
	public void setRoomRef(String roomRef) {
		this.roomRef = roomRef;
	}
	public String getJoinId() {
		return joinId;
	}
	public void setJoinId(String joinId) {
		this.joinId = joinId;
	}

	public String joinReplyToString(){
		return "JOINED_CHATROOM: "+joinedChatroom+"\n"
				+"SERVER_IP: "+serverIp+"\n"
				+"PORT: "+PORT+"\n"
				+"ROOM_REF: "+roomRef+"\n"
				+"JOIN_ID: "+joinId+"\n";

	}


}
