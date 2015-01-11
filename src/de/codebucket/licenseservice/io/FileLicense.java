package de.codebucket.licenseservice.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileLicense 
{
	public static void writeLicense(LicenseKey license, File file)
	{
		try 
	    {
			writeBytes(file, ClassSerialiser.writeData(license, "UTF-8"));
	    }
		catch (IOException e) {}
	}
	
	private static void writeBytes(File file, byte[] data)
	{
		try 
	    {
	    	FileOutputStream output = new FileOutputStream(file);
	    	output.write(data);
	    	output.close();
	    }
	    catch (IOException e) {}
	}
	
	public static LicenseKey readLicense(File file)
	{
		try 
	    {
			return (LicenseKey) ClassSerialiser.readData(readBytes(file), LicenseKey.class, "UTF-8");
	    }
		catch (IOException e) {}
		return null;
	}
	
	private static byte[] readBytes(File file)
	{
		byte[] buffer = new byte[(int)file.length()];
		try
		{
			FileInputStream input = new FileInputStream(file);
			input.read(buffer);
			input.close();
		}
		catch (IOException e) {}
		return buffer;
	}
}
