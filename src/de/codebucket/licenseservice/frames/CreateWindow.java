package de.codebucket.licenseservice.frames;

import java.awt.BorderLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.border.TitledBorder;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import java.awt.SystemColor;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.SwingConstants;

import de.codebucket.licenseservice.ProductManager;
import de.codebucket.licenseservice.ProductManager.JsonLicense;
import de.codebucket.licenseservice.ProductManager.JsonProduct;
import de.codebucket.licenseservice.ProductManager.Product;
import de.codebucket.licenseservice.daemon.LicenseHandler;
import de.codebucket.licenseservice.query.exceptions.QueryServiceException;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

public class CreateWindow extends JFrame 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JComboBox<String> productComboBox;
	private JTextField txtSerialKey;
	private LoadingDialog load;
	private boolean created;

	/**
	 * Create the frame.
	 */
	public CreateWindow(final MainWindow main) 
	{
		System.gc();
		
		setResizable(false);
		addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				main.closeWindow(CreateWindow.this);
				if(CreateWindow.this.created == true)
				{
					main.reload();
				}
			}
		});
		
		setTitle("Create License - MyLicenseControl v1.5.4");
		setType(Type.POPUP);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 335, 200);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblChooseProduct = new JLabel("Choose Product:");
		lblChooseProduct.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 12));
		lblChooseProduct.setBounds(10, 11, 100, 20);
		contentPane.add(lblChooseProduct);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Serial Key", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(10, 42, 300, 70);
		contentPane.add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		txtSerialKey = new JTextField();
		txtSerialKey.setEditable(false);
		txtSerialKey.setFont(new Font("DialogInput", Font.PLAIN, 18));
		txtSerialKey.setHorizontalAlignment(SwingConstants.CENTER);
		txtSerialKey.setBorder(null);
		txtSerialKey.setBackground(SystemColor.control);
		panel.add(txtSerialKey, BorderLayout.CENTER);
		txtSerialKey.setColumns(10);
		
		final JButton btnGenerate = new JButton("Generate");
		btnGenerate.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				new Thread("create")
				{
					@Override
					public void run() 
					{
						CreateWindow.this.load = new LoadingDialog(CreateWindow.this);
						load.revalidate();
						load.open();
						
						Product product = ProductManager.getBySelection((String) productComboBox.getSelectedItem());
						try
						{
							Thread.sleep(500L);
						} 
						catch (InterruptedException ex) {}
						
						try
						{
							JsonLicense license = LicenseHandler.createLicence(product);
							txtSerialKey.setText(license.getKey().getKey());
							copyClipboard(license.getKey().getKey());
							btnGenerate.setEnabled(false);
							CreateWindow.this.created = true;
						}
						catch (QueryServiceException ex)
						{
							JOptionPane.showMessageDialog(CreateWindow.this, "An internal error has occoured while executing the process! \n" + ex.getMessage(), "MyLicenseControl v1.5.4", JOptionPane.ERROR_MESSAGE);
							main.closeWindow(CreateWindow.this);
							return;
						}
						
						load.close();
						CreateWindow.this.load = null;
					}
				}.start();
			}
		});
		btnGenerate.setEnabled(false);
		btnGenerate.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 13));
		btnGenerate.setBounds(140, 123, 85, 30);
		contentPane.add(btnGenerate);
		
		productComboBox = new JComboBox<>();
		productComboBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0) 
			{
				if(!created)
				{
					btnGenerate.setEnabled((productComboBox.getSelectedItem() != null));
				}
			}
		});
		productComboBox.setBounds(120, 12, 190, 20);
		contentPane.add(productComboBox);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				main.closeWindow(CreateWindow.this);
				if(CreateWindow.this.created == true)
				{
					main.reload();
				}
			}
		});
		btnCancel.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 13));
		btnCancel.setBounds(235, 123, 75, 30);
		contentPane.add(btnCancel);
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
		new Thread("reload")
		{	
			@Override
			public void run()
			{
				CreateWindow.this.load = new LoadingDialog(CreateWindow.this);
				load.revalidate();
				load.open();
				
				productComboBox.removeAllItems();
				try
				{
					Thread.sleep(100L);
				} 
				catch (InterruptedException e) {}
				
				List<JsonProduct> products = new ArrayList<>();
				try
				{
					products = ProductManager.getProducts();
				}
				catch (QueryServiceException ex) {}
				
				for(JsonProduct product : products)
				{
					productComboBox.addItem(product.getDisplayname() + " (" + product.getName() + ")");
				}
				
				load.close();
				CreateWindow.this.load = null;
			}
		}.start();
	}
	
	public boolean isLoading()
	{
		return (this.load != null);
	}
	
	private void copyClipboard(String content)
	{
		StringSelection stringSelection = new StringSelection(content);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}
}
