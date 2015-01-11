package de.codebucket.licenseservice.frames;

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import de.codebucket.licenseservice.ProductManager;
import de.codebucket.licenseservice.ProductManager.JsonSerialKey;
import de.codebucket.licenseservice.ProductManager.Product;
import de.codebucket.licenseservice.ProductManager.SerialKey;
import de.codebucket.licenseservice.tools.ResponseCode;
import de.codebucket.licenseservice.daemon.LicenseHandler;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class RegisterWindow extends JFrame 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JComboBox<String> productComboBox;
	private JTextField txtSerialKey;
	private JTextField txtIdentifier;
	private String product, key;
	private LoadingDialog load;
	private MainWindow main;
	
	/**
	 * Create the dialog.
	 */
	public RegisterWindow(final MainWindow main, final String product, final String key) 
	{
		System.gc();
		
		setType(Type.POPUP);
		addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				main.closeWindow(RegisterWindow.this);
			}
		});
		
		this.main = main;
		this.product = product;
		this.key = key;
		
		setResizable(false);
		setTitle("Register Product - MyLicenseControl v1.5.2");
		setBounds(100, 100, 410, 220);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setAlwaysOnTop(false);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(null);
		setContentPane(contentPanel);
		
		JPanel registerPanel = new JPanel();
		registerPanel.setBorder(new TitledBorder(null, "Options..", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		registerPanel.setBounds(10, 10, 384, 130);
		contentPanel.add(registerPanel);
		registerPanel.setLayout(null);
		
		JLabel lblProduct = new JLabel("Choose Product:");
		lblProduct.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblProduct.setBounds(13, 25, 108, 15);
		registerPanel.add(lblProduct);
		
		JLabel lblSerialKey = new JLabel("Serial Key:");
		lblSerialKey.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblSerialKey.setBounds(13, 55, 108, 15);
		registerPanel.add(lblSerialKey);
		
		JLabel lblIdentifier = new JLabel("Identifier:");
		lblIdentifier.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblIdentifier.setBounds(13, 85, 108, 15);
		registerPanel.add(lblIdentifier);
		
		productComboBox = new JComboBox<>();
		productComboBox.setEditable(false);
		productComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		productComboBox.setBounds(131, 23, 230, 20);
		registerPanel.add(productComboBox);
		
		txtSerialKey = new JTextField();
		txtSerialKey.setEditable(false);
		txtSerialKey.setFont(new Font("DialogInput", Font.PLAIN, 12));
		txtSerialKey.setBounds(131, 53, 230, 20);
		registerPanel.add(txtSerialKey);
		txtSerialKey.setColumns(10);
		
		txtIdentifier = new JTextField();
		txtIdentifier.setFont(new Font("Tahoma", Font.PLAIN, 12));
		txtIdentifier.setBounds(131, 83, 230, 20);
		registerPanel.add(txtIdentifier);
		txtIdentifier.setColumns(10);
		
		JButton btnRegister = new JButton("Register");
		btnRegister.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(productComboBox.getSelectedItem() == null || txtSerialKey.getText().length() == 0 || txtIdentifier.getText().length() == 0)
				{
					getToolkit().beep();
					return;
				}
				
				Product product = ProductManager.getBySelection((String) productComboBox.getSelectedItem());
				SerialKey key = new JsonSerialKey(txtSerialKey.getText());
				String uuid = txtIdentifier.getText();
				
				ResponseCode response = LicenseHandler.registerProduct(product, key, uuid);
				if(response == null)
				{
					JOptionPane.showMessageDialog(RegisterWindow.this, "An internal error has occoured while executing the process!", "MyLicenseControl v1.5.2", JOptionPane.ERROR_MESSAGE);
					main.closeWindow(RegisterWindow.this);
					main.reload();
					return;
				}
				
				if(response == ResponseCode.E001)
				{
					JOptionPane.showMessageDialog(RegisterWindow.this, "Product '" + product.getName() + "' has been registered with the key '" + key.getKey() + "'!", "MyLicenseControl v1.5.2", JOptionPane.INFORMATION_MESSAGE);
				}
				else if(response == ResponseCode.E333)
				{
					JOptionPane.showMessageDialog(RegisterWindow.this, "Product '" + product.getName() + "' is authenticated with key '" + key.getKey() + "'.", "MyLicenseControl v1.5.2", JOptionPane.WARNING_MESSAGE);
				}
				else if(response == ResponseCode.E112)
				{
					JOptionPane.showMessageDialog(RegisterWindow.this, "Product '" + product.getName() + "' with license key '" + key.getKey() + "' already registered.", "MyLicenseControl v1.5.2", JOptionPane.ERROR_MESSAGE);
				}
				else if(response == ResponseCode.E111)
				{
					JOptionPane.showMessageDialog(RegisterWindow.this, "License for product '" + product.getName() + "' with license key '" + key.getKey() + "' not exists.", "MyLicenseControl v1.5.2", JOptionPane.WARNING_MESSAGE);
				}
				else if(response == ResponseCode.E011)
				{
					JOptionPane.showMessageDialog(RegisterWindow.this, "Product '" + product.getName() + "' not exists!", "MyLicenseControl v1.5.2", JOptionPane.ERROR_MESSAGE);
				}
				else
				{
					JOptionPane.showMessageDialog(RegisterWindow.this, "An internal error has occoured while executing the process!", "MyLicenseControl v1.5.2", JOptionPane.ERROR_MESSAGE);
				}
				
				main.closeWindow(RegisterWindow.this);
				main.reload();
				return;
			}
		});
		btnRegister.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnRegister.setBounds(206, 151, 89, 30);
		contentPanel.add(btnRegister);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0)
			{
				RegisterWindow.this.main.closeWindow(RegisterWindow.this);
			}
		});
		btnCancel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnCancel.setBounds(305, 151, 89, 30);
		contentPanel.add(btnCancel);
		
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
		new Thread()
		{	
			@Override
			public void run()
			{
				RegisterWindow.this.load = new LoadingDialog(RegisterWindow.this);
				load.revalidate();
				load.open();
				
				productComboBox.removeAllItems();
				try
				{
					Thread.sleep(100L);
				} 
				catch (InterruptedException e) {}
				
				Product product = ProductManager.getByName(RegisterWindow.this.product);
				productComboBox.addItem(product.getDisplayname() + " (" + product.getName() + ")");
				txtSerialKey.setText(RegisterWindow.this.key);
				
				load.close();
				RegisterWindow.this.load = null;
			}
		}.start();
	}
	
	public boolean isLoading()
	{
		return (this.load != null);
	}
}
