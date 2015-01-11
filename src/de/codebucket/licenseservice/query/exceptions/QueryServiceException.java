package de.codebucket.licenseservice.query.exceptions;

public class QueryServiceException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public QueryServiceException(String arg0) 
	{
		super(arg0);
	}

	public QueryServiceException(Throwable arg0)
	{
		super(arg0);
	}

	public QueryServiceException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

	public QueryServiceException(String arg0, Throwable arg1, boolean arg2, boolean arg3)
	{
		super(arg0, arg1, arg2, arg3);
	}

}
