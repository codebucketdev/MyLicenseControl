package de.codebucket.licenseservice.io;

public class LicenseKey 
{
	private String name;
	private String issuer;
	private String key;
	
	public LicenseKey(String name, String issuer, String key)
	{
		this.name = name;
		this.issuer = issuer;
		this.key = key;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getIssuer()
	{
		return issuer;
	}
	
	public String getKey()
	{
		return key;
	}
}
