package de.codebucket.licenseservice.query;

import java.util.UUID;

public class Packet
{
	private UUID uuid;
	private int session;
	private Status status;
	private String method;
	private Object[] args;
	
	public Packet(UUID uuid, String method, Object[] args)
	{
		this(uuid, 0, Status.OK, method, args);
	}
	
	public Packet(UUID uuid, Session session, Status status, String method, Object[] args)
	{
		this(uuid, session.getId(), status, method, args);
	}
	
	public Packet(UUID uuid, int session, Status status, String method, Object[] args)
	{
		this.uuid = uuid;
		this.session = session;
		this.status = status;
		this.method = method;
		this.args = args;
	}
	
	public UUID getUniqueId()
	{
		return uuid;
	}
	
	public int getSession()
	{
		return session;
	}
	
	public Status getStatus()
	{
		return status;
	}
	
	public String getMethod()
	{
		return method;
	}
	
	public Object[] getArgs()
	{
		return args;
	}
	
	public enum Status
	{
		OK,
		ERROR,
		REQUEST,
		RESPONSE,
		INVALID;
	}
}