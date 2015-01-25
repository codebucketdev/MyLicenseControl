package de.codebucket.licenseservice;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.swing.JOptionPane;

import de.codebucket.licenseservice.frames.UpdaterWindow;
import de.codebucket.licenseservice.util.UpdateTask;

public class MainActivity 
{
	public static void main(String[] args) 
	{
		Locale.setDefault(Locale.ENGLISH);
		if(isSet("-lowmemory", args))
		{
			final List<String> command = new ArrayList<>();
			command.addAll(Arrays.asList(args));
			command.remove("-lowmemory");
			
			try
			{
				restartApplication(getJarFile(), command.toArray(new String[command.size()]), new String[]{ "-Xmx256M", "-Xms128M", "-XX:PermSize=128m", "-XX:MaxPermSize=256m", "-XX:+DisableExplicitGC", "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC", "-XX:+UseNUMA", "-XX:+CMSParallelRemarkEnabled", "-XX:MaxGCPauseMillis=50", "-XX:+UseAdaptiveGCBoundary", "-XX:-UseGCOverheadLimit", "-XX:+UseBiasedLocking", "-XX:UseSSE=3", "-XX:+UseStringCache", "-XX:+UseCompressedOops", "-XX:+OptimizeStringConcat", "-XX:+UseFastAccessorMethods" });
			} 
			catch (IOException ex) 
			{
				throw new RuntimeException(ex);
			}
			
			return;
		}
		
		if(isSet("-updater", args))
		{
			try
			{
				FrameRunner.run(de.codebucket.licenseservice.frames.UpdaterWindow.class);
			}
			catch (Exception ex)
			{
				throw new RuntimeException(ex);
			}
			
			return;
		}
		
		try
		{
			FrameRunner.run(de.codebucket.licenseservice.frames.LoginWindow.class, new Class<?>[]{ String[].class }, new Object[]{ args });
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		new Thread("update")
		{
			@Override
			public void run() 
			{
				try
				{
					Thread.sleep(3000L);
				}
				catch (InterruptedException ex) {}
				
				new UpdateTask(UUID.randomUUID(), "https://raw.githubusercontent.com/codebucketdev/MyLicenseControl/master/src/de/codebucket/licenseservice/resources/version.json", UpdateTask.CURRENT_UPDATE) 
				{
					@Override
					public void updateSucess(Update update) 
					{
						if(update == null)
						{
							return;
						}
						
						if(update.getName().equals(getCurrent().getName()))
						{
							if(Update.compareVersions(getCurrent().getVersion(), update.getVersion()))
							{
								int result = JOptionPane.showConfirmDialog(null, "A new update is available! Would you like to update now?", "MyLicenseControl v1.5.4", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
								if(result == JOptionPane.YES_OPTION)
								{
									FrameRunner.run(UpdaterWindow.class);
								}
							}
						}
					}
				}.check();
			}
		}.start();
	}
	
	private static boolean isSet(String param, String[] args)
	{
		for(String arg : args)
		{
			if(arg.startsWith(param))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("unused")
	private static String getValue(String param, String[] args)
	{
		for(int i = 0; i < args.length; i++)
		{
			if(args[i].equalsIgnoreCase(param))
			{
				if((i + 1) >= args.length)
				{
					return null;
				}
				
				String value = args[i + 1];
				return (!value.startsWith("-") ? value : null);
			}
		}
		
		return null;
	}
	
	public static void restartApplication() throws IOException, URISyntaxException 
	{
		final File currentJar = new File(MainActivity.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		/* is it a jar file? */
		if(!currentJar.getName().endsWith(".jar"))
		{
			return;
		}
		
		restartApplication(currentJar, new String[0], new String[]{ "-Xms1024m" });
	}
	
	public static void restartApplication(File jar, String[] args, String[] parameters) throws IOException
	{
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		
		/* Build command: java -jar application.jar */
		final ArrayList<String> command = new ArrayList<String>();
		command.add(javaBin);
		command.addAll(Arrays.asList(parameters));
		command.add("-jar");
		command.add(jar.getPath());
		command.addAll(Arrays.asList(args));

		final ProcessBuilder builder = new ProcessBuilder(command);
		builder.start();
		System.exit(0);
	}
	
	public static File getJarFile()
	{
		try
		{
			final File currentJar = new File(MainActivity.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if(!currentJar.getName().endsWith(".jar"))
			{
				return null;
			}
			
			return currentJar;
		}
		catch (Exception ex) {}
		return null;
	}
}
