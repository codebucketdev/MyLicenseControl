package de.codebucket.licenseservice.awt;

import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JFrame;

public abstract class JFrameScreen extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private FrameProperties properties;
	private boolean fullscreen;
	private int width;
	private int height;
	private WindowState state;
	private GraphicsDevice device;
	private DisplayMode display;
	private FrameProperties bounds;
	
	public JFrameScreen(boolean fullscreen)
	{
		this(fullscreen, (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight());
	}
	
	public JFrameScreen(int width, int height)
	{
		this(false, width, height);
	}
	
	public JFrameScreen(boolean fullscreen, int width, int height)
	{
		this.state = WindowState.UNKNOWN;
		this.fullscreen = fullscreen;
		this.width = width;
		this.height = height;
		if(bounds == null)
		{
			this.bounds = new FrameProperties(0, 0, 0, 0, true);
		}
		
		//TODO AUTO-GENERATED STUFF HERE
		this.generate();
	}
	
	private void generate()
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        this.device = ge.getDefaultScreenDevice();
        this.display = copy(device.getDisplayMode());
        this.state = WindowState.WINDOW;
        this.setLocationRelativeTo(null);
        this.setUndecorated(false);
        if(!device.isFullScreenSupported())
        {
        	this.fullscreen = false;
        }
        
        this.build();        
        this.validate();
        this.setVisible(true);
	}
	
	private DisplayMode copy(DisplayMode mode)
	{
		return new DisplayMode(mode.getWidth(), mode.getHeight(), mode.getBitDepth(), mode.getRefreshRate());
	}
	
	public abstract void build();
	
	public void switchFullscreen()
	{
		if(fullscreen == false)
		{
			throw new IllegalStateException("This window not supports fullscreen mode!");
		}
		
		if(state == WindowState.WINDOW)
		{
			Rectangle rc = this.getBounds();
			boolean resizable = this.isResizable();
			this.properties = new FrameProperties(getX(), getY(), (int)rc.getWidth(), (int)rc.getHeight(), resizable);
		}
		
		if(device.getFullScreenWindow() == null)
		{
			FrameProperties properties = new FrameProperties(0, 0, getFullWidth(), getFullHeight(), false);
			this.setExtendedState(JFrame.MAXIMIZED_BOTH);
			this.state = WindowState.FULLSCREEN;
			properties.append(this);
			
			device.setFullScreenWindow(this);
			updateDisplay();
			return;
		}
		device.setFullScreenWindow(null);
		this.setExtendedState(JFrame.NORMAL);
		this.state = WindowState.WINDOW;
		properties.append(this);
		updateDisplay();
	}
	
	public FrameProperties getProperties()
	{
		return properties;
	}
	
	public boolean hasFullscreen()
	{
		return fullscreen;
	}
	
	public int getFullWidth()
	{
		return width;
	}
	
	public int getFullHeight()
	{
		return height;
	}
	
	public void setFullWidth(int width)
	{
		this.width = width;
	}
	
	public void setFullHeight(int height)
	{
		this.height = height;
	}
	
	public int getWidth()
	{
		return bounds.getWidth();
	}
	
	public int getHeight()
	{
		return bounds.getHeight();
	}
	
	public void setWidth(int width)
	{
		bounds.setWidth(width);
	}
	
	public void setHeight(int height)
	{
		bounds.setHeight(height);
	}
	
	public boolean isResizable()
	{
		return bounds.isResizable();
	}
	
	public void setResizable(boolean resizable)
	{
		bounds.setResizable(resizable);
	}
	
	public FrameProperties getFrameProperties()
	{
		return bounds;
	}
	
	public void setFrameProperties(FrameProperties properties)
	{
		this.properties = properties;
	}
	
	public WindowState getWindowState()
	{
		return state;
	}
	
	public GraphicsDevice getGraphicsDevice()
	{
		return device;
	}
	
	public DisplayMode getDisplayMode(WindowState state)
	{
		if(state == WindowState.UNKNOWN)
		{
			return device.getDisplayMode();
		}
		else if(state == WindowState.WINDOW)
		{
			return new DisplayMode(display.getWidth(), display.getHeight(), display.getBitDepth(), display.getRefreshRate());
		}
		else if(state == WindowState.FULLSCREEN)
		{
			return new DisplayMode(getFullWidth(), getFullHeight(), display.getBitDepth(), display.getRefreshRate());
		}
		return null;
	}
	
	public void updateDisplay()
	{
		try
		{
			device.setDisplayMode(getDisplayMode(getWindowState()));
		}
		catch(Exception ex) {};
	}
	
	public void updateFrame()
	{
		this.generate();
	}
	
	public void exit()
	{
		System.exit(1);
	}
	
	public enum WindowState
	{
		WINDOW(1),
		UNKNOWN(0),
		FULLSCREEN(2);
		
		private int id;
		
		WindowState(int id)
		{
			this.id = id;
		}
		
		public int getId()
		{
			return id;
		}
	}
	
	public static class FrameProperties
	{
		private int x;
		private int y;
		private int width;
		private int height;
		private boolean resizable;
		
		public FrameProperties(int x, int y, int width, int height, boolean resizable)
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.resizable = resizable;
		}
		
		public int getX()
		{
			return x;
		}
		
		public void setX(int x)
		{
			this.x = x;
		}
		
		public int getY()
		{
			return y;
		}
		
		public void setY(int y)
		{
			this.y = y;
		}
		
		public int getWidth()
		{
			return width;
		}
		
		public void setWidth(int width)
		{
			this.width = width;
		}
		
		public int getHeight()
		{
			return height;
		}
		
		public void setHeight(int height)
		{
			this.height = height;
		}
		
		public boolean isResizable()
		{
			return resizable;
		}
		
		public void setResizable(boolean resizable)
		{
			this.resizable = resizable;
		}
		
		public void append(Frame frame)
		{
			frame.setBounds(x, y, width, height);
			frame.setResizable(resizable);
			frame.validate();
		}
		
		public static FrameProperties getFrom(Frame frame)
		{
			Rectangle rc = frame.getBounds();
			boolean rs = frame.isResizable();
			return new FrameProperties((int)rc.getX(), (int)rc.getY(), (int)rc.getWidth(), (int)rc.getHeight(), rs);
		}
	}
}
