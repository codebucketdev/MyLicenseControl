package de.codebucket.licenseservice.frames;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import de.codebucket.licenseservice.FrameRunner;
import de.codebucket.licenseservice.query.Client;
import de.codebucket.licenseservice.query.Packet;
import de.codebucket.licenseservice.query.Session;
import de.codebucket.licenseservice.query.Packet.Status;
import de.codebucket.licenseservice.tools.PasswordUtils;
import de.codebucket.licenseservice.util.ClassSerialiser;
import de.codebucket.licenseservice.util.UniqueIdentifier;
import de.codebucket.licenseservice.util.UniqueIdentifier.LoginData;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

import com.ezware.dialog.task.TaskDialogs;

public class LoginWindow extends JFrame implements Runnable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField address, username;
	private JPasswordField password;
	private JCheckBox saveLogin;
	private JButton btnConnect;
	
	private Client client;
	private Thread connect;
	private boolean connecting;
	
	public static void main(String[] args) 
	{
		FrameRunner.run(LoginWindow.class, new Class<?>[]{ String[].class }, new Object[]{ args });
	}
	
	/**
	 * Create the frame.
	 */
	public LoginWindow(String[] args) 
	{
		System.gc();
		
		setType(Type.POPUP);
		setResizable(false);
		setTitle("MyLicenseControl v1.5.2");
		setSize(276, 365);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		final Action loginAction = new AbstractAction() 
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if(address.getText().length() == 0 || address.getText().split(":").length < 2 || address.getText().split(":")[0].length() == 0 || !isInteger(address.getText().split(":")[1]) || username.getText().length() == 0)
				{
					getToolkit().beep();
					return;
				}
				
				String[] split = address.getText().split(":");
				String hostname = split[0];
				int port = Integer.parseInt(split[1]);
				InetSocketAddress address = new InetSocketAddress(hostname, port);
				
				try 
				{
					connect(address, username.getText(), new String(password.getPassword()));
				} 
				catch (Exception ex)
				{
					getToolkit().beep();
					toggleButton(true);
					JOptionPane.showMessageDialog(LoginWindow.this, "Could not connect to server " + client.getAddress().getHostAddress() + ":" + client.getPort() + "!", "MyLicenseControl v1.5.2", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		};
		contentPane.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doLoginAction");
		contentPane.getActionMap().put("doLoginAction", loginAction);
		
		JLabel lblTitle = new JLabel("MyLicenseControl");
		lblTitle.setFont(new Font(UIManager.getFont("MenuBar.font").getName(), UIManager.getFont("MenuBar.font").getStyle(), 24));
		lblTitle.setBounds(10, 11, 249, 32);
		contentPane.add(lblTitle);
		
		JLabel lblAbout = new JLabel("By Codebucket. Version 1.5.2");
		lblAbout.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 11));
		lblAbout.setBounds(12, 47, 247, 14);
		contentPane.add(lblAbout);
		
		File settings = new File(getUserDirectory(), "lastlogin.nbt");
		LoginData login = new LoginData("127.0.0.1:1557", "admin", PasswordUtils.encodeString("password"));
		if(settings.exists())
		{
			try 
			{
				login = LoginData.loadFile(settings);
			} 
			catch (IOException ex)
			{
				TaskDialogs.showException(ex);
			}
		}
		else
		{
			try
			{
				if(settings.createNewFile())
				{
					login.saveFile(settings);
				}
			}
			catch (IOException ex)
			{
				TaskDialogs.showException(ex);
			}
		}
		
		if(isSet("-autologin", args)) 
		{
			new Thread()
			{
				@Override
				public void run() 
				{
					try
					{
						Thread.sleep(300L);
					}
					catch (InterruptedException ex) {}
					loginAction.actionPerformed(null);
				}
			}.start();
		}
		
		JPanel loginPanel = new JPanel();
		loginPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Login", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		loginPanel.setBounds(10, 71, 249, 255);
		contentPane.add(loginPanel);
		loginPanel.setLayout(null);
		
		JLabel lblAddress = new JLabel("Server IP:");
		lblAddress.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 11));
		lblAddress.setBounds(10, 25, 229, 14);
		loginPanel.add(lblAddress);
		
		address = new JTextField(login.getAddress());
		address.setColumns(10);
		address.setBounds(10, 45, 229, 20);
		address.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doLoginAction");
		address.getActionMap().put("doLoginAction", loginAction);
		loginPanel.add(address);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 11));
		lblUsername.setBounds(10, 76, 229, 14);
		loginPanel.add(lblUsername);
		
		username = new JTextField(login.getUsername());
		username.setColumns(10);
		username.setBounds(10, 95, 229, 20);
		username.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doLoginAction");
		username.getActionMap().put("doLoginAction", loginAction);
		loginPanel.add(username);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 11));
		lblPassword.setBounds(10, 126, 229, 14);
		loginPanel.add(lblPassword);
		
		password = new JPasswordField(PasswordUtils.decodeString(login.getPassword()));
		password.setColumns(10);
		password.setBounds(10, 145, 229, 20);
		password.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doLoginAction");
		password.getActionMap().put("doLoginAction", loginAction);
		loginPanel.add(password);
		
		this.saveLogin = new JCheckBox("Save login credentials");
		saveLogin.setFont(new Font(UIManager.getFont("InternalFrame.titleFont").getName(), UIManager.getFont("InternalFrame.titleFont").getStyle(), 12));
		saveLogin.setSelected(true);
		saveLogin.setFocusable(false);
		saveLogin.setBounds(10, 172, 229, 23);
		loginPanel.add(saveLogin);
		
		this.btnConnect = new JButton("Connect!");
		btnConnect.setIcon(null);
		btnConnect.addActionListener(loginAction);
		btnConnect.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 13));
		btnConnect.setBounds(10, 207, 229, 37);
		loginPanel.add(btnConnect);
		
		new Thread("check")
		{
			@Override
			public void run() 
			{
				while(1 > 0)
				{
					try
					{
						Thread.sleep(100L);
					} 
					catch (InterruptedException e) {}
					
					if(!connecting)
					{
						if(address.getText().length() == 0 || address.getText().split(":").length < 2 || address.getText().split(":")[0].length() == 0 || !isInteger(address.getText().split(":")[1]) || username.getText().length() == 0)
						{
							btnConnect.setEnabled(false);
						}
						else
						{
							btnConnect.setEnabled(true);
						}
					}
				}
			}
		}.start();
		
		this.validate();
	}
	
	private boolean isSet(String param, String[] args)
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
	private String getValue(String param, String[] args)
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
	
	@Override
	public void run()
	{
		try 
        {
			Thread.sleep(10*1000);
		} 
        catch (InterruptedException e) 
        {
			e.printStackTrace();
		}
        
        if(connecting == true)
        {
        	connecting = false;
        	this.toggleButton(true);
			JOptionPane.showMessageDialog(this, "Failed to connect to server " + client.getAddress().getHostAddress() + ":" + client.getPort() + "!", "MyLicenseControl v1.5.2", JOptionPane.ERROR_MESSAGE);
        }
	}
	
	public void connect(final InetSocketAddress address, final String username, final String password) throws Exception
	{
		this.connecting = true;
		this.toggleButton(false);
		this.client = new Client(username, password, address.getAddress(), address.getPort(), new Session(-1));
		
		if(client.openConnection())
		{
			checkConnection();
			new Thread("open")
			{
				@Override
				public void run() 
				{
					UUID uuid = UUID.randomUUID();
					Integer id = UniqueIdentifier.getIdentifier();
					send(uuid, client, Status.REQUEST, "connect", id, username, encrypt(password + "_query"));
					
					while (connecting)
					{
						DatagramPacket data = client.receive();
						if(data != null)
						{
							try
							{
								byte[] bytes = client.decompress(client.read(data));
								Packet packet = (Packet) ClassSerialiser.read(bytes, Packet.class, "UTF-8");						
								if(packet.getUniqueId() == null || packet.getMethod() == null || packet.getSession() == 0)
								{
									return;
								}
								
								if(packet.getMethod().equalsIgnoreCase("connect"))
								{
									connecting = false;	
									toggleButton(true);
									if(packet.getStatus() == Status.OK)
									{
										Session session = (Session) ClassSerialiser.read(((String) packet.getArgs()[0]).getBytes(), Session.class, "UTF-8");
										client.setSession(session);
										
										if(saveLogin.isSelected())
										{
											File settings = new File(getUserDirectory(), "lastlogin.nbt");
											LoginData login = new LoginData(address.getAddress().getHostAddress() + ":" + address.getPort(), username, PasswordUtils.encodeString(password));
											try
											{
												if(!settings.exists())
												{
													settings.createNewFile();
												}
												login.saveFile(settings);
											}
											catch (IOException ex) {}
										}
										
										LoginWindow.this.dispose();
										FrameRunner.run(MainWindow.class, new Class<?>[]{ Client.class }, new Object[]{ client });
									}	
									else if(packet.getStatus() == Status.ERROR)
									{
										JOptionPane.showMessageDialog(LoginWindow.this, "Lost connection to server: \n" + packet.getArgs()[0], "MyLicenseControl v1.5.2", JOptionPane.ERROR_MESSAGE);
									}
									return;
								}
							}
							catch (Exception ex)
							{
								TaskDialogs.showException(ex);
							}
						}
					}
				}
			}.start();
			
		}
		else
		{
			connecting = false;
			this.toggleButton(true);
			JOptionPane.showMessageDialog(this, "Could not connect to server " + client.getAddress().getHostAddress() + ":" + client.getPort() + "!", "MyLicenseControl v1.5.2", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void checkConnection()
	{
		this.connect = new Thread(this);
		connect.setName("connect");
		connect.start();
	}
	
	private void toggleButton(boolean active)
	{
		btnConnect.setEnabled(active);
		if(!active)
		{
	    	btnConnect.setText("Connecting...");
	    	btnConnect.setIcon(new ImageIcon(LoginWindow.class.getResource("/de/codebucket/licenseservice/resources/gif_small.gif")));
	    	return;
		}
		btnConnect.setText("Connect!");
    	btnConnect.setIcon(null);
	}
	
	public void send(UUID uuid, Client client, Status status, String method, Object... args)
	{
		try
		{
			Packet packet = new Packet(uuid, client.getSession(), status, method, args);
			byte[] contents = ClassSerialiser.write(packet, "UTF-8");
			send(contents, client, client.getPort());
		}
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
	}
	
	private void send(final byte[] data, final Client client, final int port) 
	{
		byte[] bytes = client.compress(data);
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length, client.getAddress(), port);
		client.send(packet);
	}
	
	public File getUserDirectory()
	{
		File dir = new File(System.getProperty("user.home") + File.separator + ".mycontrol" + File.separator);
		if(!dir.exists())
		{
			dir.mkdir();
		}
		
		return dir;
	}
	
	private String encrypt(String stringInput)
	{		
		try
		{
			byte[] buffer = stringInput.getBytes();
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(buffer);
			
			String hexMessageEncode = "";
			byte[] messageDigestBytes = messageDigest.digest();
			for (int index = 0; index < messageDigestBytes.length; index++) 
			{
				int countEncode = messageDigestBytes[index] & 0xff;
				if (Integer.toHexString(countEncode).length() == 1)
				{
					hexMessageEncode = hexMessageEncode + "0";
				}			
				hexMessageEncode = hexMessageEncode + Integer.toHexString(countEncode);
			}
			return hexMessageEncode;
		}
		catch (NoSuchAlgorithmException ex)
		{
			return stringInput;
		}
	}
	
	private boolean isInteger(String integer)
	{
		try
		{
			Integer.parseInt(integer);
		}
		catch (NumberFormatException ex)
		{
			return false;
		}
		return true;
	}
	
}
