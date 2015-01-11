package de.codebucket.licenseservice.util;

public class CachedData 
{
	private Object data;
	
	public CachedData(Object data)
	{
		this.data = data;
	}
	
	public Object getData()
	{
		return data;
	}
	
	public void setData(Object data)
	{
		this.data = data;
	}
}
