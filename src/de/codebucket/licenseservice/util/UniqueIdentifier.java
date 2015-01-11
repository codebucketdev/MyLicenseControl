package de.codebucket.licenseservice.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UniqueIdentifier 
{
	private static List<Integer> ids = new ArrayList<Integer>();
	private static final int RANGE = 10000;

	private static int index = 0;

	static 
	{
		for (int i = 0; i < RANGE; i++) 
		{
			ids.add(i);
		}
		Collections.shuffle(ids);
	}

	public static int getIdentifier() 
	{
		if (index > ids.size() - 1) 
		{
			index = 0;
		}
		return ids.get(index++);
	}
	
	public static class LoginData 
	{
		private String address, username, password;
		
		public LoginData(String address, String username, String password)
		{
			this.address = address;
			this.username = username;
			this.password = password;
		}
		
		public String getAddress()
		{
			return address;
		}
		
		public void setAddress(String address)
		{
			this.address = address;
		}
		
		public String getUsername()
		{
			return username;
		}
		
		public void setUsername(String username)
		{
			this.username = username;
		}
		
		public String getPassword()
		{
			return password;
		}
		
		public void setPassword(String password)
		{
			this.password = password;
		}
		
		public void saveFile(File file) throws IOException
		{
			FileManager.writeBytes(file, ClassSerialiser.write(this, "UTF-8"));
		}
		
		public static LoginData loadFile(File file) throws IOException
		{
			return (LoginData) ClassSerialiser.read(FileManager.readBytes(file), LoginData.class, "UTF-8");
		}
	}
}
