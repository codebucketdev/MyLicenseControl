package de.codebucket.licenseservice.query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;

import de.codebucket.licenseservice.query.exceptions.QueryServiceException;
import de.codebucket.licenseservice.util.CompressUtils;

public class Client 
{
	private String username, password;
	private DatagramSocket socket;
	private InetAddress address;
	private Thread send;
	private int port;
	private Session session;
	
	public static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	
	public Client(String username, String password, InetAddress address, int port, Session session)
	{
		this.username = username;
		this.password = password;
		this.address = address;
		this.port = port;
		this.session = session;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public InetAddress getAddress()
	{
		return address;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public DatagramSocket getSocket()
	{
		return socket;
	}
	
	public Session getSession()
	{
		return session;
	}
	
	public void setSession(Session session)
	{
		this.session = session;
	}

	public boolean openConnection() 
	{
		try 
		{
			socket = new DatagramSocket();
		} 
		catch (SocketException e) 
		{
			return false;
		}
		return true;
	}
	
	private int received = 0;

	public DatagramPacket receive() 
	{
		byte[] data = new byte[40960];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		try 
		{
			socket.receive(packet);
			if(packet.getLength() > 0)
			{
				received += packet.getLength();
			}
		} 
		catch (IOException e) {}
		
		return packet;
	}
	
	public int getReceived()
	{
		return received;
	}
	
	private int sent = 0;
	
	public void send(final DatagramPacket packet) 
	{
		send = new Thread("send")
		{
			public void run() 
			{
				try 
				{
					socket.send(packet);
					if(packet.getLength() > 0)
					{
						sent += packet.getLength();
					}
				} 
				catch (IOException e) {}
			}
		};
		send.start();
	}
	
	public int getSent()
	{
		return sent;
	}
	
	public byte[] compress(byte[] data)
	{
		try
    	{
    		return CompressUtils.compress(data);
    	}
    	catch (IOException ex)
    	{
    		throw new QueryServiceException(ex);
    	}
	}

    public byte[] decompress(byte[] data) 
    {
    	try
    	{
    		return CompressUtils.decompress(data);
    	}
    	catch (IOException ex)
    	{
    		throw new QueryServiceException(ex);
    	}
    }
    
    public byte[] read(DatagramPacket packet)
    {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	baos.write(packet.getData(), 0, packet.getLength());
    	return baos.toByteArray();
    }

	public void close() 
	{
		new Thread() 
		{
			public void run()
			{
				synchronized (socket) 
				{
					socket.close();
				}
			}
		}.start();
	}
}
