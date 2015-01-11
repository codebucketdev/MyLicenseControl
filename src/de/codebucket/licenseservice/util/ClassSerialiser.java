package de.codebucket.licenseservice.util;

import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ClassSerialiser 
{
	public static byte[] write(Object object, String charset) throws IOException
	{
		Gson gson = new GsonBuilder().create();
		String content = gson.toJson(object);
		return content.getBytes(charset);
	}
	
	public static Object read(byte[] data, Class<?> clazz, String charset) throws IOException
	{
		Gson gson = new GsonBuilder().create();
		String content = new String(data, charset);
		return gson.fromJson(content, clazz);
	}
}
