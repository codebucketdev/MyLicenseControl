package de.codebucket.licenseservice;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.codebucket.licenseservice.query.Packet;
import de.codebucket.licenseservice.query.Packet.Status;
import de.codebucket.licenseservice.query.exceptions.QueryServiceException;
import de.codebucket.licenseservice.tools.CompleteTask;
import de.codebucket.licenseservice.tools.CompleteTask.LinkedData;
import de.codebucket.licenseservice.util.CachedData;

public class ProductManager
{
	@SuppressWarnings("unchecked")
	public static List<JsonProduct> getProducts() throws QueryServiceException
	{
		final CachedData data = new CachedData(null);
		CompleteTask task = new CompleteTask(UUID.randomUUID(), Status.REQUEST, "getProducts") 
		{
			@Override
			public void process(Packet packet) 
			{
				Type type = new TypeToken<List<JsonProduct>>(){}.getType();
				List<JsonProduct> products = (List<JsonProduct>) new Gson().fromJson((String) packet.getArgs()[0], type);
				data.setData(products);
			}
		};
		task.send();
		
		task.waitFor();
		if(data.getData() == null)
		{
			return new ArrayList<>();
		}
		return (List<JsonProduct>) data.getData();
	}
	
	public static LinkedData addProduct(int id, String name, String displayname) throws QueryServiceException
	{
		final CachedData data = new CachedData(null);
		CompleteTask task = new CompleteTask(UUID.randomUUID(), Status.REQUEST, "addProduct", id, name, displayname) 
		{
			@Override
			public void process(Packet packet) 
			{
				data.setData(new LinkedData(packet.getStatus(), packet.getArgs()[0]));
			}
		};
		task.send();
		
		task.waitFor();
		if(data.getData() == null)
		{
			return new LinkedData(Status.ERROR, "Connection to server timed out!");
		}
		return (LinkedData) data.getData();
	}
	
	public static Product getById(int id)
	{
		for(Product product : getProducts())
		{
			if(product.getId() == id)
			{
				return product;
			}
		}
		return null;
	}
	
	public static Product getByName(String name)
	{
		for(Product product : getProducts())
		{
			if(product.getName().equals(name))
			{
				return product;
			}
		}
		return null;
	}
	
	public static Product getBySelection(String selection)
	{
		String name = "default";
		Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(selection);
	    if(m.find()) 
	    {
	    	name = m.group(1);
	    }
		
		for(Product product : getProducts())
		{
			if(product.getName().equals(name))
			{
				return product;
			}
		}
		return null;
	}
	
	public static interface Product
	{
		public int getId();
		
		public String getName();
		
		public String getDisplayname();
	}
	
	public static interface SerialKey
	{
		public String getKey();
	}
	
	public static interface License
	{
		public Product getProduct();
		
		public SerialKey getKey();
		
		public String getUniqueId();
	}
	
	public static class JsonProduct implements Product
	{
		private int id;
		private String name;
		private String displayname;
		
		public JsonProduct(int id, String name, String displayname)
		{
			this.id = id;
			this.name = name;
			this.displayname = displayname;
		}		
		
		@Override
		public int getId() 
		{
			return id;
		}

		@Override
		public String getName() 
		{
			return name;
		}

		@Override
		public String getDisplayname() 
		{
			return displayname;
		}
		
	}
	
	public static class JsonSerialKey implements SerialKey
	{
		private String key;
		
		public JsonSerialKey(String key)
		{
			this.key = key;
		}
		
		@Override
		public String getKey()
		{
			return key;
		}
	}
	
	public static class JsonLicense implements License
	{
		private JsonProduct product;
		private JsonSerialKey key;
		private String uuid;
		
		public JsonLicense(JsonProduct product, JsonSerialKey key, String uuid)
		{
			this.product = product;
			this.key = key;
			this.uuid = uuid;
		}
		
		@Override
		public Product getProduct()
		{
			return product;
		}
		
		@Override
		public SerialKey getKey()
		{
			return key;
		}
		
		@Override
		public String getUniqueId()
		{
			return uuid;
		}
	}
}
