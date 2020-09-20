package com.qingclass.bigbay.entity.zebra;

public class AccessTokenAndTicket {
	
	private String accessToken;
	private String ticket;
	
	
	public AccessTokenAndTicket() {
		super();
	}
	
	public AccessTokenAndTicket(String accessToken, String ticket) {
		super();
		this.accessToken = accessToken;
		this.ticket = ticket;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
	

}
