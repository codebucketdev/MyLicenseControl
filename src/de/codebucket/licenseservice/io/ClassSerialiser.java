package de.codebucket.licenseservice.io;

import java.io.IOException;
import java.nio.charset.Charset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.codebucket.licenseservice.util.CompressUtils;

public class ClassSerialiser 
{
	public static byte[] writeData(Object object, String charset) throws IOException
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String content = gson.toJson(object);
		return compress(content, Charset.forName(charset));
	}
	
	public static Object readData(byte[] data, Class<?> clazz, String charset) throws IOException
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String content = decompress(data, Charset.forName(charset));
		return gson.fromJson(content, clazz);
	}
	
	private static byte[] compress(String text, Charset charset) throws IOException
	{
		return CompressUtils.compress(text.getBytes(charset));
    }

	private static String decompress(byte[] bytes, Charset charset) throws IOException 
    {
        return new String(CompressUtils.decompress(bytes), charset);
    }
}
