package de.codebucket.licenseservice;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.UIManager;

import com.ezware.dialog.task.TaskDialogs;

public class FrameRunner 
{
	static
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception ex) {};
	}
	
	public static void centerWindow(Window frame) 
	{
	    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
	    int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
	    frame.setLocation(x, y);
	}
	
	public static void run(Class<? extends Window> clazz)
	{
		try 
		{
			executeInstance((Window) clazz.newInstance());
		}
		catch (InstantiationException ex)
		{
			ex.printStackTrace();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void run(Object obj)
	{
		try 
		{
			executeInstance((Window) obj.getClass().newInstance());
		}
		catch (InstantiationException ex)
		{
			ex.printStackTrace();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void run(Class<? extends Window> clazz, Class<?>[] args, Object[] param)
	{
		try 
		{
			executeInstance((Window) clazz.getConstructor(args).newInstance(param));
		} 
		catch (InstantiationException ex)
		{
			ex.printStackTrace();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void executeInstance(final Window window)
	{
		EventQueue.invokeLater(new Runnable()
	    {
			public void run()
		    {
				try
		        {
					window.setAutoRequestFocus(window.isAutoRequestFocus());
					window.setAlwaysOnTop(window.isAlwaysOnTop());
		    		window.setVisible(true);
		        }
		        catch (Exception ex)
		        {
		        	TaskDialogs.showException(ex);
		        }
		    }
	    });
	}
}
