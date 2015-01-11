package de.codebucket.licenseservice.query;

public class Session 
{
	private int id;
	private int attempts;
	private long lastpacket;
	
	public Session(int id)
	{
		this.id = id;
		this.attempts = 0;
		this.setLastPacket();
	}
	
	public int getId()
	{
		return id;
	}
	
	public int getAttempts()
	{
		return attempts;
	}
	
	public void setAttempts(int attempts)
	{
		this.attempts = attempts;
	}
	
	public void resetAttempts()
	{
		this.attempts = 0;
	}
	
	public long getLastPacket()
	{
		return lastpacket;
	}
	
	public void setLastPacket()
	{
		this.lastpacket = System.currentTimeMillis();
	}	
}
