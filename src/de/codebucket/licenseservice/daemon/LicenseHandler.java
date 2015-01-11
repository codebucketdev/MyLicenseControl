package de.codebucket.licenseservice.daemon;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.codebucket.licenseservice.ProductManager;
import de.codebucket.licenseservice.ProductManager.JsonLicense;
import de.codebucket.licenseservice.ProductManager.JsonProduct;
import de.codebucket.licenseservice.ProductManager.JsonSerialKey;
import de.codebucket.licenseservice.ProductManager.License;
import de.codebucket.licenseservice.ProductManager.Product;
import de.codebucket.licenseservice.ProductManager.SerialKey;
import de.codebucket.licenseservice.tools.ResponseCode;
import de.codebucket.licenseservice.frames.MainWindow;
import de.codebucket.licenseservice.query.Packet;
import de.codebucket.licenseservice.query.Packet.Status;
import de.codebucket.licenseservice.query.exceptions.QueryServiceException;
import de.codebucket.licenseservice.tools.CompleteTask;
import de.codebucket.licenseservice.util.CachedData;

public class LicenseHandler
{	
	public static JsonLicense createLicence(final Product product) throws QueryServiceException
	{
		final CachedData data = new CachedData(null);
		CompleteTask task = new CompleteTask(UUID.randomUUID(), Status.REQUEST, "createLicense", product.getName()) 
		{
			@Override
			public void process(Packet packet) 
			{
				if(packet.getStatus() != Status.RESPONSE)
				{
					RuntimeException ex = new RuntimeException((String) packet.getArgs()[0]);
					data.setData(ex);
					return;
				}
				
				final String serial = (String) packet.getArgs()[0];
				SerialKey key = new SerialKey()
				{
					@Override
					public String getKey() 
					{
						return serial;
					}
				};
				data.setData(key);	
			}
		};
		task.send();
		
		task.waitFor();
		if(data.getData() instanceof RuntimeException)
		{
			throw new QueryServiceException(((RuntimeException) data.getData()).getCause());
		}
		
		if(data.getData() == null)
		{
			return new JsonLicense(new JsonProduct(0, "name", "displayname"), new JsonSerialKey(generateKey().getKey()), "false");
		}
		return new JsonLicense(new JsonProduct(product.getId(), product.getName(), product.getDisplayname()), new JsonSerialKey(((SerialKey) data.getData()).getKey()), "false");
	}
	
	public static ResponseCode registerProduct(Product product, SerialKey key, String uuid) 
	{
		final CachedData data = new CachedData(null);
		CompleteTask task = new CompleteTask(UUID.randomUUID(), Status.REQUEST, "registerProduct", product.getName(), key.getKey(), uuid) 
		{
			@Override
			public void process(Packet packet) 
			{
				data.setData(packet.getArgs()[0]);
			}
		};
		MainWindow.registerTask(task);
		task.send();
		
		task.waitFor();
		if(data.getData() == null)
		{
			return ResponseCode.E000;
		}
		return ResponseCode.valueOf((String) data.getData());
	}

	public static boolean resetUniqueId(SerialKey key, String uuid) throws QueryServiceException
	{
		final CachedData data = new CachedData(null);
		CompleteTask task = new CompleteTask(UUID.randomUUID(), Status.REQUEST, "resetUniqueId", key.getKey(), uuid) 
		{
			@Override
			public void process(Packet packet) 
			{
				if(packet.getStatus() != Status.OK)
				{
					RuntimeException ex = new RuntimeException((String) packet.getArgs()[0]);
					data.setData(ex);
					return;
				}
				data.setData(packet.getArgs()[0]);
			}
		};
		task.send();
		
		task.waitFor();
		if(data.getData() instanceof RuntimeException)
		{
			throw new QueryServiceException(((RuntimeException) data.getData()).getCause());
		}
		
		if(data.getData() == null)
		{
			return false;
		}
		return true;
	}

	public static boolean existsProduct(Product product)
	{
		if(ProductManager.getProducts().contains(product))
		{
			return true;
		}
		return false;
	}
	
	public static boolean existsLicense(Product product, SerialKey key)
	{
		for(License license : getLicenses(product))
		{
			if(license.getKey().getKey().equals(key.getKey()))
			{
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public static List<JsonLicense> getLicenses() 
	{
		final CachedData data = new CachedData(null);
		CompleteTask task = new CompleteTask(UUID.randomUUID(), Status.REQUEST, "getLicenses") 
		{
			@Override
			public void process(Packet packet) 
			{
				if(packet.getStatus() != Status.RESPONSE)
				{
					RuntimeException ex = new RuntimeException((String) packet.getArgs()[0]);
					data.setData(ex);
					return;
				}
				
				Type type = new TypeToken<List<JsonLicense>>(){}.getType();
				List<JsonLicense> licenses = (List<JsonLicense>) new Gson().fromJson((String) packet.getArgs()[0], type);
				data.setData(licenses);
			}
		};
		task.send();
		
		task.waitFor();
		if(data.getData() instanceof RuntimeException)
		{
			throw new QueryServiceException(((RuntimeException) data.getData()).getCause());
		}
		
		if(data.getData() == null)
		{
			return new ArrayList<>();
		}
		return (List<JsonLicense>) data.getData();
	}
	
	public static List<License> getLicenses(Product product)
	{
		List<License> licenses = new ArrayList<>();
		for(License license : getLicenses())
		{
			if(license.getProduct().getName().equals(product.getName()))
			{
				licenses.add(license);
			}
		}
		
		return licenses;
	}

	@SuppressWarnings("unchecked")
	public static List<JsonSerialKey> getKeys(Product product) 
	{
		final CachedData data = new CachedData(null);
		CompleteTask task = new CompleteTask(UUID.randomUUID(), Status.REQUEST, "getKeys", product.getName()) 
		{
			@Override
			public void process(Packet packet) 
			{
				if(packet.getStatus() != Status.RESPONSE)
				{
					RuntimeException ex = new RuntimeException((String) packet.getArgs()[0]);
					data.setData(ex);
					return;
				}
				
				Type type = new TypeToken<List<JsonSerialKey>>(){}.getType();
				List<JsonSerialKey> keys = (List<JsonSerialKey>) new Gson().fromJson((String) packet.getArgs()[0], type);
				data.setData(keys);
			}
		};
		task.send();
		
		task.waitFor();
		if(data.getData() instanceof RuntimeException)
		{
			throw new QueryServiceException(((RuntimeException) data.getData()).getCause());
		}
		
		if(data.getData() == null)
		{
			return new ArrayList<>();
		}
		return (List<JsonSerialKey>) data.getData();
	}

	public static boolean removeKey(Product product, SerialKey key) 
	{
		final CachedData data = new CachedData(null);
		CompleteTask task = new CompleteTask(UUID.randomUUID(), Status.REQUEST, "removeKey", product.getName(), key.getKey()) 
		{
			@Override
			public void process(Packet packet) 
			{
				if(packet.getStatus() != Status.OK)
				{
					RuntimeException ex = new RuntimeException((String) packet.getArgs()[0]);
					data.setData(ex);
					return;
				}
				data.setData(packet.getArgs()[0]);
			}
		};
		MainWindow.registerTask(task);
		task.send();
		
		task.waitFor();
		if(data.getData() instanceof RuntimeException)
		{
			throw new QueryServiceException(((RuntimeException) data.getData()).getCause());
		}
		
		if(data.getData() == null)
		{
			return false;
		}
		return true;
	}
	
	public static String getUniqueId(Product product, SerialKey key) 
	{
		final CachedData data = new CachedData(null);
		CompleteTask task = new CompleteTask(UUID.randomUUID(), Status.REQUEST, "getUniqueId", product.getName(), key.getKey()) 
		{
			@Override
			public void process(Packet packet) 
			{
				if(packet.getStatus() != Status.RESPONSE)
				{
					RuntimeException ex = new RuntimeException((String) packet.getArgs()[0]);
					data.setData(ex);
					return;
				}
				data.setData(packet.getArgs()[0]);
			}
		};
		MainWindow.registerTask(task);
		task.send();
		
		task.waitFor();
		if(data.getData() instanceof RuntimeException)
		{
			throw new QueryServiceException(((RuntimeException) data.getData()).getCause());
		}
		
		if(data.getData() == null)
		{
			return "false";
		}
		return (String) data.getData();
	}
	
	public static SerialKey generateKey()
	{
		final CachedData data = new CachedData(null);
		CompleteTask task = new CompleteTask(UUID.randomUUID(), Status.REQUEST, "generateKey") 
		{
			@Override
			public void process(Packet packet) 
			{
				data.setData(packet.getArgs()[0]);
			}
		};
		MainWindow.registerTask(task);
		task.send();
		
		task.waitFor();
		if(data.getData() == null)
		{
			return new JsonSerialKey("AAAA-BBBBB-CCCC-DDDDD");
		}
		
		return new SerialKey() 
		{
			@Override
			public String getKey()
			{
				return (String) data.getData();
			}
		};
	}
}
