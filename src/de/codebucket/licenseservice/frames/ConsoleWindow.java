package de.codebucket.licenseservice.frames;

import java.awt.Component;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.codebucket.licenseservice.query.Packet;
import de.codebucket.licenseservice.query.Packet.Status;
import de.codebucket.licenseservice.query.exceptions.QueryServiceException;
import de.codebucket.licenseservice.tools.CompleteTask;
import de.codebucket.licenseservice.util.CachedData;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JLabel;

public class ConsoleWindow extends JFrame 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	MainWindow main;
	private JPanel contentPane;
	private JTextArea console;
	private JLabel lastUpdate;
	private DefaultCaret caret;
	private LoadingDialog load;
	private Thread update;

	/**
	 * Create the frame.
	 */
	public ConsoleWindow(final MainWindow main) 
	{
		System.gc();
		
		addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				main.closeWindow(ConsoleWindow.this);
			}
		});
		
		setType(Type.POPUP);
		this.main = main;
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo((Component) main);
		setTitle("Service console - MyLicenseControl v1.5.4");
		setSize(880, 550);
		setAlwaysOnTop(false);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		this.console = new JTextArea();
		console.setWrapStyleWord(true);
		console.setLineWrap(true);
		console.setFont(new Font("DialogInput", Font.PLAIN, 13));
		console.setEditable(false);
		caret = (DefaultCaret) console.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scroll = new JScrollPane(console);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(10, 11, 854, 450);
		contentPane.add(scroll);
		
		JButton btnReload = new JButton(" Reload");
		btnReload.setFont(new Font("Tahoma", btnReload.getFont().getStyle(), 13));
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
		btnReload.setIcon(new ImageIcon(MainWindow.class.getResource("/de/codebucket/licenseservice/resources/icon_refresh.png")));
		btnReload.setBounds(757, 474, 107, 39);
		contentPane.add(btnReload);
		
		this.lastUpdate = new JLabel("Last update:");
		lastUpdate.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 13));
		lastUpdate.setBounds(15, 476, 729, 35);
		contentPane.add(lastUpdate);
		
		this.validate();
	}
	
	public void open()
	{
		setAlwaysOnTop(false);
		setVisible(true);
		loadData();
	}
	
	public void close()
	{
		if(isLoading())
		{
			this.load.close();
		}
		
		this.dispose();
	}
	
	private void loadData()
	{
		this.update = new Thread()
		{	
			@Override
			public void run()
			{
				ConsoleWindow.this.load = new LoadingDialog(ConsoleWindow.this);
				load.revalidate();
				load.open();
				
				console.setText(null);
				try
				{
					Thread.sleep(100L);
				} 
				catch (InterruptedException e) {}
				
				List<String> log = new ArrayList<>();
				try
				{
					log = ConsoleWindow.printLog();
				}
				catch (QueryServiceException ex) {}
				
				for(String line : log)
				{
					console.append(line + "\n");
				}
				
				Calendar c = Calendar.getInstance();
				SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
				String timestamp = f.format(c.getTime());
				lastUpdate.setText("Last update: " + timestamp);
				
				load.close();
				ConsoleWindow.this.load = null;
			}
		};
		update.start();
	}
	
	public boolean isLoading()
	{
		return (this.load != null);
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> printLog() 
	{
		final CachedData data = new CachedData(null);
		CompleteTask task = new CompleteTask(UUID.randomUUID(), Status.REQUEST, "printLog") 
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
				
				java.lang.reflect.Type type = new TypeToken<List<String>>(){}.getType();
				List<String> keys = (List<String>) new Gson().fromJson((String) packet.getArgs()[0], type);
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
		return (List<String>) data.getData();
	}
}
