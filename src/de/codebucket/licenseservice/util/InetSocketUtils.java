package de.codebucket.licenseservice.util;

import java.io.*;
import java.net.*;
import java.util.*;

public class InetSocketUtils 
{
	public static long getPing(InetAddress address, long timeout)
	{
		if(System.getProperty("os.name").startsWith("Windows"))
		{
			return pingWindows(address, timeout);
		}		
		
		boolean reached = false;
		long start = System.currentTimeMillis();
		long end = (start -1);
		
		try
		{
			if(address.isReachable(Long.valueOf(timeout).intValue()))
			{
				end = System.currentTimeMillis();
			}
		}
		catch (IOException ex)
		{
			if(reached)
			{
				return calculatePingDelay(1, 0);
			}
		}
		
		return calculatePingDelay(start, end);
	}
	
	private static long pingWindows(InetAddress address, long timeout)
	{
		boolean reached = false;
		long start = System.currentTimeMillis();
		long end = (start -1);
		
		try
		{
			String line;
			ProcessBuilder processBuilder = new ProcessBuilder(new String[] { "ping", address.getHostAddress(), "-w", String.valueOf(timeout), "-n", "1" });
	        Process process = processBuilder.start();
	        
	        int processing = 0;
	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        while ((line = bufferedReader.readLine()) != null) 
	        {
	        	processing++;
	        	if(processing == 2)
	        	{
	        		start = System.currentTimeMillis();
	        	}
	        	
	        	else if(processing == 3)
	        	{
	        		if(line.contains(address.getHostAddress()))
	        		{
	        			end = System.currentTimeMillis();
	        		}
	        	}
	        }
	        
	        if(calculatePingDelay(start, end) > timeout)
			{
	        	return calculatePingDelay(1, 0);
			}
	        
	        bufferedReader.close();
		}
	    catch (IOException ex)
		{
	    	if(reached)
			{
				return calculatePingDelay(1, 0);
			}
		}
		
		return calculatePingDelay(start, end);
	}
	
	public static InetAddress getHostGateway()
	{
		@SuppressWarnings("unused")
		String _gateway = null, _ip = null;
		if(System.getProperty("os.name").startsWith("Windows"))
		{
			return parseWindows();
		}
		
		try
        {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/net/route"));
            
            String line;
            while((line = reader.readLine())!=null)
            {
                line = line.trim();
                String [] tokens = line.split(" ");
                if(tokens.length > 1 && tokens[1].equals("00000000"))
                {
                    String gateway = tokens[2]; //0102A8C0
                    if(gateway.length() == 8)
                    {
                        String[] s4 = new String[4];
                        s4[3] = String.valueOf(Integer.parseInt(gateway.substring(0, 2), 16));
                        s4[2] = String.valueOf(Integer.parseInt(gateway.substring(2, 4), 16));
                        s4[1] = String.valueOf(Integer.parseInt(gateway.substring(4, 6), 16));
                        s4[0] = String.valueOf(Integer.parseInt(gateway.substring(6, 8), 16));
                        _gateway = s4[0] + "." + s4[1] + "." + s4[2] + "." + s4[3];
                    }
                    
                    String iface = tokens[0];
                    NetworkInterface nif = NetworkInterface.getByName(iface);
                    Enumeration<InetAddress> addrs = nif.getInetAddresses();
                    while(addrs.hasMoreElements())
                    {
                        Object obj = addrs.nextElement();
                        if(obj instanceof Inet4Address)
                        {
                            _ip = obj.toString();
                            if(_ip.startsWith("/")) 
                            	_ip = _ip.substring(1);
                        }
                    }
                }
            }
            reader.close();
            return InetAddress.getByName(_ip);
        }
        catch(Exception ex) {}
		
		return null;
	}
	
	private static InetAddress parseWindows()
	{
		try
        {
			@SuppressWarnings("unused")
			String _gateway = null, _ip = null;
            Process pro = Runtime.getRuntime().exec("route print");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pro.getInputStream())); 
 
            String line;
            while((line = bufferedReader.readLine()) != null)
            {
            	line = line.trim();
                String[] tokens = line.split(" ");
                List<String> args = new ArrayList<>();
                for(String l : tokens)
                {
                	if(!(l.equals("") || l.equalsIgnoreCase(" ") || l.startsWith(" ")))
                	{
                		args.add(l);
                	}
                }
                
                tokens = args.toArray(new String[args.size()]);
                if(tokens.length == 5 && tokens[0].equals("0.0.0.0"))
                {
                	_gateway = tokens[2];
                    _ip = tokens[3];
                	return InetAddress.getByName(tokens[2]);
                }
            }  
        }
        catch(IOException ex) {}
		
		return null;
	}
	
	public static boolean isNetworkAvailable() 
	{
        try 
        {
            URL url = new URL("http://www.yahoo.com");
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            Object objData = urlConnection.getContent();
            return (objData != null);
        } 
        catch(Exception ex) {}
        return false;
    }
	
	private static long calculatePingDelay(long start, long end)
	{
		return (end - start);
	}
}
