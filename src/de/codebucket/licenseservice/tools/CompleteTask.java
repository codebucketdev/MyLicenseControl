package de.codebucket.licenseservice.tools;

import java.util.UUID;

import de.codebucket.licenseservice.frames.MainWindow;
import de.codebucket.licenseservice.query.Client;
import de.codebucket.licenseservice.query.Packet;
import de.codebucket.licenseservice.query.Packet.Status;

public abstract class CompleteTask implements Runnable
{
	MainWindow listener;
	
	private Thread check;
	private UUID uuid;
	private Status status;
	private String method;
	private Object[] args;	
	
	private boolean complete;
	
	public CompleteTask(UUID uuid, Status status, String method, Object... args)
	{
		this.uuid = uuid;
		this.status = status;
		this.method = method;
		this.args = args;
	}
	
	public abstract void process(Packet packet);
	
	public synchronized void send()
	{
		MainWindow.registerTask(this);
		Client client = listener.getClient();
		listener.send(uuid, client, status, method, args);
		
		this.check = new Thread(this);
		check.setName(uuid.toString());
		check.start();
	}
	
	public synchronized void receive(Packet packet)
	{
		if(packet.getUniqueId().equals(uuid))
		{
			this.process(packet);		
			setCompleted(true);
			unregisterTask();
		}
	}
	
	@Override
	public void run()
	{
		try 
        {
			Thread.sleep(10*1000);
		} 
        catch (InterruptedException ex) {}
        
        if(!isCompleted())
        {
        	setCompleted(true);
        	listener.unregisterTask(this);
			synchronized (this) 
			{
				this.notifyAll();
			}
        }
	}
	
	
	public synchronized int waitFor()
	{
		try
		{
			while(!isCompleted()) 
			{
				wait();
			}
		}
		catch (InterruptedException ex) 
		{
			ex.printStackTrace();
			return 0;
		}
		return -1;
	}
	
	public void setCompleted(boolean complete)
	{
		this.complete = complete;
	}
	
	public boolean isCompleted()
	{
		return complete;
	}
	
	public void registerTask(MainWindow listener)
	{
		this.listener = listener;
	}
	
	public void unregisterTask()
	{
		listener.unregisterTask(this);
		synchronized (this) 
		{
			this.notifyAll();
		}
	}
	
	public UUID getUniqueId()
	{
		return uuid;
	}
	
	public String getMethod()
	{
		return method;
	}
	
	public Object[] getArgs()
	{
		return args;
	}
	
	public Packet getPacket()
	{
		MainWindow main = MainWindow.getMain();
		Client client = main.getClient();
		return new Packet(uuid, client.getSession(), Status.REQUEST, method, args);
	}
	
	public static class LinkedData
	{
		private Object a, b;
		
		public LinkedData(Object a, Object b)
		{
			this.a = a;
			this.b = b;
		}
		
		public Object a()
		{
			return a;
		}
		
		public Object b()
		{
			return b;
		}
	}
}
