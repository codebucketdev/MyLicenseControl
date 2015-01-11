package de.codebucket.licenseservice.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressUtils 
{
	public static byte[] compress(byte[] data) throws IOException
	{
		Deflater deflater = new Deflater();
		deflater.setLevel(Deflater.BEST_COMPRESSION);
		deflater.setStrategy(Deflater.FILTERED);
		deflater.setInput(data);
		deflater.finish();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) 
		{
			int count = deflater.deflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		
		byte[] output = outputStream.toByteArray();
		deflater.end();
		return output;
	}

	public static byte[] decompress(byte[] data) throws IOException
	{
		Inflater inflater = new Inflater();
		inflater.setInput(data);
		inflater.finished();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		try 
		{
			byte[] buffer = new byte[1024];
			while (!inflater.finished()) 
			{
				int count = inflater.inflate(buffer);
				outputStream.write(buffer, 0, count);
			}
			outputStream.close();
		}
		catch (DataFormatException ex) 
		{
			throw new IOException(ex);
		}
		
		byte[] output = outputStream.toByteArray();
		inflater.end();
		return output;
	}
	
	public static String humanReadableByteCount(long bytes, boolean si) 
	{
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
