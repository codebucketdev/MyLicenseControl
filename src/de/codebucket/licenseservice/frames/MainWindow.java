package de.codebucket.licenseservice.frames;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.codebucket.licenseservice.FrameRunner;
import de.codebucket.licenseservice.ProductManager;
import de.codebucket.licenseservice.ProductManager.JsonLicense;
import de.codebucket.licenseservice.ProductManager.License;
import de.codebucket.licenseservice.ProductManager.Product;
import de.codebucket.licenseservice.ProductManager.SerialKey;
import de.codebucket.licenseservice.daemon.LicenseHandler;
import de.codebucket.licenseservice.query.Client;
import de.codebucket.licenseservice.query.Packet;
import de.codebucket.licenseservice.query.Session;
import de.codebucket.licenseservice.query.Packet.Status;
import de.codebucket.licenseservice.query.exceptions.QueryServiceException;
import de.codebucket.licenseservice.tools.CompleteTask;
import de.codebucket.licenseservice.util.ClassSerialiser;
import de.codebucket.licenseservice.util.CompressUtils;
import de.codebucket.licenseservice.util.InetSocketUtils;

import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.ListSelectionModel;
import javax.swing.DropMode;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.UIManager;

public class MainWindow extends JFrame implements Runnable
{
	private static MainWindow instance;
	
	private JPanel contentPane;
	private JScrollPane scroll;
	private JToolBar status;
	private Thread listen;
	private boolean running;
	private Client client;
	private List<CompleteTask> tasks;
	private LoadingDialog load;
	private List<Window> frames;
	
	private static final long serialVersionUID = 1L;
	private JLabel lblIcon, lblStatus, lblDownload, lblUpload, lblPing;
	private JTable tblLicenses;
	
	/**
	 * Create the frame.
	 */
	public MainWindow(Client client)
	{
		System.gc();
		
		setResizable(false);
		instance = this;
		this.client = client;
		this.tasks = new ArrayList<>();
		this.frames = new ArrayList<>();
		this.createWindow();
	}
	
	private void createWindow()
	{
		this.start();
		
		setTitle(client.getUsername() + "@" + client.getAddress().getHostAddress() + " - MyLicenseControl v1.5.3");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 942, 605);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel components = new JPanel();
		components.setBorder(new EmptyBorder(5, 5, 5, 5));
		components.setLayout(null);
		contentPane.add(components, BorderLayout.CENTER);
		
		tblLicenses = new JTable() 
		{
			private static final long serialVersionUID = 1L;

			@Override
		    public boolean isCellEditable(int row, int column) 
		    {
		        return false;
		    }
		};
		tblLicenses.setDropMode(DropMode.ON);
		tblLicenses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblLicenses.setFont(new Font(UIManager.getFont("FileChooser.listFont").getName(), UIManager.getFont("FileChooser.listFont").getStyle(), 13));
		tblLicenses.setModel(getTableModel(new ArrayList<JsonLicense>()));
		tblLicenses.getTableHeader().setReorderingAllowed(false);
		
		this.scroll = new JScrollPane(tblLicenses);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setBorder(new TitledBorder(null, "Licenses (" + null + ")", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scroll.setBounds(10, 11, 906, 469);
		components.add(scroll);
		
		JButton btnReload = new JButton("");
		btnReload.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if(!isLoading())
				{
					loadData();
				}
			}
		});
		btnReload.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 11));
		btnReload.setIcon(new ImageIcon(MainWindow.class.getResource("/de/codebucket/licenseservice/resources/icon_refresh.png")));
		btnReload.setBounds(866, 491, 50, 45);
		components.add(btnReload);
		
		JButton btnShowLog = new JButton("");
		btnShowLog.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if(getWindow(ConsoleWindow.class) == null)
				{
					ConsoleWindow window = (ConsoleWindow) openWindow(ConsoleWindow.class, MainWindow.this);
					FrameRunner.centerWindow(window);
					window.open();
				}
				else
				{
					final Window window = getWindow(ConsoleWindow.class);
					EventQueue.invokeLater(new Runnable() 
					{
					    @Override
					    public void run() 
					    {
					    	window.setVisible(true);
					    	window.toFront();
					    	window.repaint();
					    }
					});
				}
			}
		});
		btnShowLog.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 11));
		btnShowLog.setIcon(new ImageIcon(MainWindow.class.getResource("/de/codebucket/licenseservice/resources/icon_uuid.png")));
		btnShowLog.setBounds(806, 491, 50, 45);
		components.add(btnShowLog);
		
		JButton btnCreateLicense = new JButton("   Create License");
		btnCreateLicense.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(getWindow(CreateWindow.class) == null)
				{
					CreateWindow window = (CreateWindow) openWindow(CreateWindow.class, MainWindow.this);
					FrameRunner.centerWindow(window);
					window.open();
				}
				else
				{
					final Window window = getWindow(CreateWindow.class);
					EventQueue.invokeLater(new Runnable() 
					{
					    @Override
					    public void run() 
					    {
					    	window.setVisible(true);
					    	window.toFront();
					    	window.repaint();
					    }
					});
				}
			}
		});
		btnCreateLicense.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 11));
		btnCreateLicense.setIcon(new ImageIcon(MainWindow.class.getResource("/de/codebucket/licenseservice/resources/icon_add.png")));
		btnCreateLicense.setBounds(10, 491, 145, 45);
		components.add(btnCreateLicense);
		
		final JButton btnEditLicense = new JButton("   Edit License");
		btnEditLicense.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if(getWindow(EditWindow.class) == null)
				{
					ListSelectionModel s = tblLicenses.getSelectionModel();
					if(!s.isSelectionEmpty())
					{
						int r = tblLicenses.getSelectedRow();
						Product product = getProduct(parseHtml((String) tblLicenses.getValueAt(r, 0)));
						String key = parseHtml((String) tblLicenses.getValueAt(r, 1));
						String uuid = parseHtml((String) tblLicenses.getValueAt(r, 2));
						
						EditWindow window = (EditWindow) openWindow(EditWindow.class, MainWindow.this, product.getName(), key, uuid);
						FrameRunner.centerWindow(window);
						window.open();
					}
				}
				else
				{
					final Window window = getWindow(EditWindow.class);
					EventQueue.invokeLater(new Runnable() 
					{
					    @Override
					    public void run() 
					    {
					    	window.setVisible(true);
					    	window.toFront();
					    	window.repaint();
					    }
					});
				}
			}
		});
		btnEditLicense.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 11));
		btnEditLicense.setIcon(new ImageIcon(MainWindow.class.getResource("/de/codebucket/licenseservice/resources/icon_edit.png")));
		btnEditLicense.setBounds(165, 491, 134, 45);
		btnEditLicense.setEnabled(false);
		components.add(btnEditLicense);
		
		final JButton btnRemoveLicense = new JButton("   Remove License");
		btnRemoveLicense.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				ListSelectionModel s = tblLicenses.getSelectionModel();
				if(!s.isSelectionEmpty())
				{
					int action = JOptionPane.showConfirmDialog(MainWindow.this, "Do you really want to remove this license?", "MyLicenseControl v1.5.3", JOptionPane.YES_NO_OPTION);  
				    if(action == JOptionPane.YES_OPTION)
				    {
						final int r = tblLicenses.getSelectedRow();
						Product product = getProduct(parseHtml((String) tblLicenses.getValueAt(r, 0)));
						SerialKey key = new SerialKey() 
						{
							@Override
							public String getKey() 
							{
								return parseHtml((String) tblLicenses.getValueAt(r, 1));
							}
						};					
						LicenseHandler.removeKey(product, key);
						MainWindow.this.reload();
				    }
				}
			}
		});
		btnRemoveLicense.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 11));
		btnRemoveLicense.setIcon(new ImageIcon(MainWindow.class.getResource("/de/codebucket/licenseservice/resources/icon_remove.png")));
		btnRemoveLicense.setBounds(309, 491, 155, 45);
		btnRemoveLicense.setEnabled(false);
		components.add(btnRemoveLicense);
		
		JButton btnAddProduct = new JButton("   Add Product");
		btnAddProduct.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				if(getWindow(ManagerWindow.class) == null)
				{
					ManagerWindow window = (ManagerWindow) openWindow(ManagerWindow.class, MainWindow.this);
					FrameRunner.centerWindow(window);
					window.open();
				}
				else
				{
					final Window window = getWindow(ManagerWindow.class);
					EventQueue.invokeLater(new Runnable() 
					{
					    @Override
					    public void run() 
					    {
					    	window.setVisible(true);
					    	window.toFront();
					    	window.repaint();
					    }
					});
				}
			}
		});
		btnAddProduct.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 11));
		btnAddProduct.setIcon(new ImageIcon(MainWindow.class.getResource("/de/codebucket/licenseservice/resources/icon_product.png")));
		btnAddProduct.setBounds(474, 491, 134, 45);
		components.add(btnAddProduct);
		
		final JButton btnRegisterProduct = new JButton("   Register Product");
		btnRegisterProduct.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				if(getWindow(RegisterWindow.class) == null)
				{
					ListSelectionModel s = tblLicenses.getSelectionModel();
					if(!s.isSelectionEmpty())
					{
						int r = tblLicenses.getSelectedRow();
						String name = getProduct(parseHtml((String) tblLicenses.getValueAt(r, 0))).getName();
						String key = parseHtml((String) tblLicenses.getValueAt(r, 1));
						
						RegisterWindow window = (RegisterWindow) openWindow(RegisterWindow.class, MainWindow.this, name, key);
						FrameRunner.centerWindow(window);
						window.open();
					}
				}
				else
				{
					final Window window = getWindow(RegisterWindow.class);
					EventQueue.invokeLater(new Runnable() 
					{
					    @Override
					    public void run() 
					    {
					    	window.setVisible(true);
					    	window.toFront();
					    	window.repaint();
					    }
					});
				}
			}
		});
		btnRegisterProduct.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 11));
		btnRegisterProduct.setIcon(new ImageIcon(MainWindow.class.getResource("/de/codebucket/licenseservice/resources/icon_check.png")));
		btnRegisterProduct.setBounds(618, 491, 165, 45);
		btnRegisterProduct.setEnabled(false);
		components.add(btnRegisterProduct);
		
		this.status = new JToolBar();
		status.setFloatable(false);
		status.setFocusable(false);
		contentPane.add(status, BorderLayout.SOUTH);
		
		this.lblIcon = new JLabel(" ");
		lblIcon.setFocusable(false);
		lblIcon.setHorizontalAlignment(SwingConstants.LEFT);
		lblIcon.setIcon(new ImageIcon(MainWindow.class.getResource("/de/codebucket/licenseservice/resources/icon_connect_established.png")));
		status.add(lblIcon);
		
		this.lblStatus = new JLabel("Connected as " + client.getUsername() + " (ID " + client.getSession().getId() + ") on " + client.getAddress().getHostAddress() + ":" + client.getPort());
		lblStatus.setFocusable(false);
		status.add(lblStatus);
		
		Component spacer0 = Box.createHorizontalGlue();
		status.add(spacer0);
		
		this.lblDownload = new JLabel("Down: (calculating)");
		lblDownload.setFocusable(false);
		status.add(lblDownload);
		
		JLabel split0 = new JLabel(",");
		split0.setFocusable(false);
		status.add(split0);
		
		Component spacer1 = Box.createHorizontalStrut(5);
		status.add(spacer1);
		
		this.lblUpload = new JLabel("Up: (calculating)");
		lblUpload.setFocusable(false);
		status.add(lblUpload);
		
		new Thread("status")
		{	
			@Override
			public void run()
			{
				while(running)
				{
					Client client = MainWindow.this.client;
					if(client == null) 
					{
						return;
					}
					
					lblDownload.setText("Received: " + CompressUtils.humanReadableByteCount(client.getReceived(), true));
					lblUpload.setText("Sent: " + CompressUtils.humanReadableByteCount(client.getSent(), true));
				}
			}
		}.start();
		
		Component spacer2 = Box.createHorizontalStrut(5);
		status.add(spacer2);
		
		this.lblPing = new JLabel("(calculating)");
		lblPing.setFocusable(false);
		status.add(lblPing);
		
		tblLicenses.getSelectionModel().addListSelectionListener(new ListSelectionListener() 
		{
	        public void valueChanged(ListSelectionEvent e)
	        { 
	        	ListSelectionModel lsm = (ListSelectionModel) e.getSource();
	        	if(lsm == null)
	        	{
	        		return;
	        	}	        	
	        	
	        	int r = tblLicenses.getSelectedRow();
	        	if(lsm.isSelectionEmpty() || tblLicenses.getValueAt(r, 0) == null || tblLicenses.getValueAt(r, 1) == null || tblLicenses.getValueAt(r, 2) == null)
	        	{
	        		JButton[] buttons = { btnEditLicense, btnRemoveLicense, btnRegisterProduct };
	        		for(JButton button : buttons)
	        		{
	        			button.setEnabled(false);
	        		}
		            return;
	        	}
	        	
	        	JButton[] buttons = { btnEditLicense, btnRemoveLicense, btnRegisterProduct };
        		for(JButton button : buttons)
        		{
        			if(button.equals(btnRegisterProduct))
        			{
        				boolean enabled = false;
 			            if(parseHtml((String) tblLicenses.getValueAt(r, 2)).equals("false"))
 			            {
 			            	enabled = true;
 			            }
 			            button.setEnabled(enabled);
 			            continue;
        			}
        			button.setEnabled(true);
        		}
	        }
	    });
		
		final Action editAction = new AbstractAction() 
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if(getWindow(EditWindow.class) == null)
				{
					ListSelectionModel s = tblLicenses.getSelectionModel();
					if(!s.isSelectionEmpty())
					{
						int r = tblLicenses.getSelectedRow();
						Product product = getProduct(parseHtml((String) tblLicenses.getValueAt(r, 0)));
						String key = parseHtml((String) tblLicenses.getValueAt(r, 1));
						String uuid = parseHtml((String) tblLicenses.getValueAt(r, 2));
						
						EditWindow window = (EditWindow) openWindow(EditWindow.class, MainWindow.this, product.getName(), key, uuid);
						FrameRunner.centerWindow(window);
						window.open();
					}
				}
			}
		};
		tblLicenses.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doEditAction");
		tblLicenses.getActionMap().put("doEditAction", editAction);
		
		final Action deleteAction = new AbstractAction() 
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				ListSelectionModel s = tblLicenses.getSelectionModel();
				if(!s.isSelectionEmpty())
				{
					int action = JOptionPane.showConfirmDialog(MainWindow.this, "Do you really want to remove this license?", "MyLicenseControl v1.5.3", JOptionPane.YES_NO_OPTION);  
				    if(action == JOptionPane.YES_OPTION)
				    {
				    	final int r = tblLicenses.getSelectedRow();
						Product product = getProduct(parseHtml((String) tblLicenses.getValueAt(r, 0)));
						SerialKey key = new SerialKey() 
						{
							@Override
							public String getKey() 
							{
								return parseHtml((String) tblLicenses.getValueAt(r, 1));
							}
						};					
						LicenseHandler.removeKey(product, key);
						MainWindow.this.reload();
				    }
				}
			}
		};
		tblLicenses.getInputMap().put(KeyStroke.getKeyStroke("DELETE"), "doDeleteAction");
		tblLicenses.getActionMap().put("doDeleteAction", deleteAction);
		
		FrameRunner.centerWindow(this);
		addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e) 
			{
				int action = JOptionPane.showConfirmDialog(MainWindow.this, "Do you really want to disconnect from the server?", "MyLicenseControl v1.5.3", JOptionPane.YES_NO_OPTION);  
			    if(action == JOptionPane.YES_OPTION)
			    {
			    	MainWindow.this.disconnect();
			    }
			}
		});
		setVisible(true);
		loadData();
	}
	
	public void reload()
	{
		if(!isLoading())
		{
			this.loadData();
		}
	}
	
	private void loadData()
	{
		new Thread("reload")
		{	
			@Override
			public void run()
			{
				MainWindow.this.load = new LoadingDialog(MainWindow.this);
				load.revalidate();
				load.open();
				
				tblLicenses.setModel(getTableModel(null));
				try
				{
					Thread.sleep(100L);
				} 
				catch (InterruptedException e) {}
				
				List<JsonLicense> licenses = new ArrayList<>();
				try
				{
					licenses = LicenseHandler.getLicenses();
				}
				catch (QueryServiceException ex) {}
				
				TableModel model = getTableModel(licenses);
				tblLicenses.setModel(model);
				load.close();
				
				setLicenseCount(licenses.size());
				MainWindow.this.load = null;
			}
		}.start();
	}
	
	public boolean isLoading()
	{
		return (this.load != null);
	}
	
	private DefaultTableModel getTableModel(List<JsonLicense> list)
	{
		if(list == null)
		{
			Object[][] data = new Object[0][];
			String[] columns = { "Product", "Serial Key", "Identifier" };
			return new DefaultTableModel(data, columns);
		}
		
		List<Object[]> objects = new ArrayList<>();
		for(License license : list)
		{
			String product = license.getProduct().getDisplayname();
			String serial = license.getKey().getKey();
			String uuid = license.getUniqueId();
			
			Object[] data = { "<html><b>" + product + "</b></html>", "<html><code>" + serial + "</code></html>", "<html>" + uuid + "<html>" };
			objects.add(data);
		}
		
		if(objects.size() < 100)
		{
			int amount = (100 - objects.size());
			for(int i = 0; i < amount; i++)
			{
				objects.add(new Object[]{ null, null, null });
			}
		}
		
		Object[][] data = objects.toArray(new Object[objects.size()][]);
		String[] columns = { "Product", "Serial Key", "Identifier" };
		return new DefaultTableModel(data, columns);
	}
	
	private void setLicenseCount(int count)
	{
		scroll.setBorder(new TitledBorder(null, "Licenses (" + count + ")", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	}
	
	public String parseHtml(String text)
	{
		return text.replaceAll("\\<.*?\\>", "");
	}
	
	private Product getProduct(String displayname)
	{
		for(Product product : ProductManager.getProducts())
		{
			if(product.getDisplayname().equals(displayname))
			{
				return product;
			}
		}
		return null;
	}
	
	public Window openWindow(Class<? extends Window> window, Object... args)
	{
		try
		{
			List<Class<?>> classes = new ArrayList<>();
			for(Object arg : args)
			{
				classes.add(arg.getClass());
			}
			
			Class<?>[] parameterTypes = classes.toArray(new Class<?>[classes.size()]);
			Window frame = (Window) window.getConstructor(parameterTypes).newInstance(args);
			frame.setVisible(true);
			
			frames.add(frame);
			return frame;
		}
		catch (Exception ex)
		{
			return null;
		}
	}
	
	public void closeWindow(Window window)
	{
		if(isWindowOpen(window))
		{
			window.setVisible(false);
			window.dispose();
			frames.remove(window);
		}
	}
	
	public void closeWindows()
	{
		for(Window w : getWindowsList())
		{
			w.setAlwaysOnTop(false);
			w.setVisible(false);
			w.dispose();
		}
		frames.clear();
	}
	
	public boolean isWindowOpen(Window window)
	{
		return this.frames.contains(window);
	}
	
	public Window getWindow(Class<? extends Window> window)
	{
		for(Window w : getWindowsList())
		{
			if(w.getClass().equals(window))
			{
				return w;
			}
		}
		return null;
	}
	
	public List<Window> getWindowsList()
	{
		return frames;
	}
	
	public Client status()
	{
		return client;
	}
	
	public void start()
	{
		running = true;
		listen = new Thread(this, "listen");
		listen.start();
	}
	
	public void stop()
	{
		running = false;
	}

	@Override
	public void run()
	{
		this.manageServer();
		while(running)
		{
			System.gc();
			DatagramPacket packet = client.receive();
			if(packet != null)
			{
				this.process(packet);
			}
		}
	}
	
	private void process(DatagramPacket data)
	{
		try
		{
			byte[] bytes = client.decompress(client.read(data));
			Packet packet = (Packet) ClassSerialiser.read(bytes, Packet.class, "UTF-8");
			if(packet.getUniqueId() == null || packet.getMethod() == null || packet.getSession() == 0)
			{
				return;
			}
			
			if(packet.getStatus() != Status.REQUEST)
			{
				for(int i = 0; i < tasks.size(); i++)
				{
					try
					{
						CompleteTask task = tasks.get(i);
						if(task != null)
						{
							task.receive(packet);
						}
					}
					catch(Exception ex) {}
				}
				return;
			}
			
			if(packet.getMethod().equalsIgnoreCase("echo"))
			{
				ping(client.getSession());
			}
			else if(packet.getMethod().equalsIgnoreCase("close"))
			{
				timeout((String) packet.getArgs()[0]);
			}
			else
			{
				send(packet.getUniqueId(), getClient(), Status.ERROR, "error", "This method is not allowed or does not exist.");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void manageServer() 
	{
		new Thread("manage")
		{
			public void run() 
			{
				while(running) 
				{
					try 
					{
						Thread.sleep(2000L);
					} 
					catch (InterruptedException e) {}
					
					long ping = InetSocketUtils.getPing(client.getAddress(), 7800);
					if(ping > 0 && ping <= 7800) 
					{
						lblPing.setText("(" + ping + " ms)");
					}
					
					if(client.getSession().getAttempts() == 5)
					{
						timeout("Connection to server timed out!");
					}
					else
					{
						client.getSession().setAttempts(client.getSession().getAttempts() +1);
					}
				}
			}
		}.start();
	}
	
	public void send(final byte[] data, final InetAddress address, final int port) 
	{
		byte[] bytes = client.compress(data);
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
		client.send(packet);
	}
	
	public void send(UUID session, InetAddress address, int port, Status status, String method, Object... args)
	{
		try
		{
			Packet packet = new Packet(session, 0, status, method, args);
			byte[] contents = ClassSerialiser.write(packet, "UTF-8");
			send(contents, address, port);
		}
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
	}
	
	public void send(UUID uuid, Client client, Status status, String method, Object... args)
	{
		try
		{
			Packet packet = new Packet(uuid, client.getSession(), status, method, args);
			byte[] contents = ClassSerialiser.write(packet, "UTF-8");
			send(contents, client.getAddress(), client.getPort());
		}
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
	}
	
	public void status(String icon, String status)
	{
		lblIcon.setIcon(new ImageIcon(MainWindow.class.getResource("/de/codebucket/licenseservice/resources/" + icon))); 
		lblStatus.setText(parseHtml("Lost connection to server: " + status));
	}
	
	public void ping(Session session)
	{
		session.resetAttempts();
		int id = session.getId();
		send(UUID.randomUUID(), getClient(), Status.REQUEST, "echo", id);
	}
	
	public void close()
	{
		if(isLoading())
		{
			this.load.close();
		}
		client.close();
		this.closeWindows();
		
		FrameRunner.run(LoginWindow.class, new Class<?>[]{ String[].class }, new Object[]{ new String[0] });
		setVisible(false);
		dispose();
	}
	
	public void disconnect()
	{
		setTitle("Not connected - MyLicenseControl v1.5.3");
		try 
		{
			Thread.sleep(150L);
		} 
		catch (InterruptedException e) {}
		
		running = false;	
		status("icon_connect_no.png", "Disconnected");
		send(UUID.randomUUID(), getClient(), Status.REQUEST, "disconnect");
		this.close();
	}
	
	public void timeout(String reason) 
	{
		setTitle("Not connected - MyLicenseControl v1.5.3");
		try 
		{
			Thread.sleep(150L);
		} 
		catch (InterruptedException e) {}
		
		running = false;	
		status("icon_connect_no.png", reason);
		JOptionPane.showMessageDialog(this, "Lost connection to server: \n" + reason, "MyLicenseControl v1.5.3", JOptionPane.ERROR_MESSAGE);
		this.close();
	}
	
	public static synchronized void registerTask(CompleteTask task)
	{
		if(!instance.tasks.contains(task))
		{
			task.registerTask(instance);
			instance.tasks.add(task);
		}
	}
	
	public synchronized void unregisterTask(CompleteTask task)
	{
		if(tasks.contains(task))
		{
			tasks.remove(task);
			task.unregisterTask();
		}
	}
	
	public Client getClient()
	{
		return client;
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public Thread getThread()
	{
		return listen;
	}
	
	public static MainWindow getMain()
	{
		return instance;
	}
}
