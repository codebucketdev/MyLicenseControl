package de.codebucket.licenseservice.tools;

public enum ResponseCode 
{
	E000("000", "Internal error!", false),
	E001("001", "Product has been successfully activated!", true),
	E011("011", "Product number does not exist", false),
	E111("111", "Invalid license key", false),
	E112("112", "License key already registered", false),
	E122("122", "Generated license key successfully", false),
	E222("222", "License key already exists", false),
	E223("223", "This method is not allowed or does not exist", false),
	E233("233", "Too few arguments", false),
	E333("333", "Authentication is successful", true);
	
	private String code;
	private String messsage;
	private boolean start;
	
	ResponseCode(String code, String message, boolean start)
	{
		this.code = code;
		this.messsage = message;
		this.start = start;
	}
	
	public String getCode()
	{
		return code;
	}
	
	public String getMessage()
	{
		return messsage;
	}
	
	public boolean canStart()
	{
		return start;
	}
	
	public static ResponseCode getByCode(String code)
	{
		for(ResponseCode err : ResponseCode.values())
		{
			if(err.getCode().equalsIgnoreCase(code))
			{
				return err;
			}
		}
		return null;
	}
}
