package de.codebucket.licenseservice.frames;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.ezware.dialog.task.TaskDialogs;

import de.codebucket.licenseservice.ProductManager;
import de.codebucket.licenseservice.ProductManager.License;
import de.codebucket.licenseservice.ProductManager.Product;
import de.codebucket.licenseservice.ProductManager.SerialKey;
import de.codebucket.licenseservice.daemon.LicenseHandler;
import de.codebucket.licenseservice.io.FileLicense;
import de.codebucket.licenseservice.io.LicenseKey;
import de.codebucket.licenseservice.query.exceptions.QueryServiceException;

public class EditWindow extends JFrame 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JComboBox<String> productComboBox;
	private String product, key, uuid;
	private JTextField txtSerialKey, txtIdentifier;
	private LoadingDialog load;
	private Thread update;
	private boolean reset;
	

	/**
	 * Create the frame.
	 */
	public EditWindow(final MainWindow main, final String product, final String key, final String uuid) 
	{
		System.gc();
		
		setType(Type.POPUP);
		addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				main.closeWindow(EditWindow.this);
				if(EditWindow.this.reset == true)
				{
					main.reload();
				}
			}
		});
		
		this.product = product;
		this.key = key;
		this.uuid = uuid;
		
		setResizable(false);
		setTitle("Edit License - MyLicenseControl v1.5.2");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 337, 220);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel detailsPanel = new JPanel();
		detailsPanel.setBorder(new TitledBorder(null, "Details", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		detailsPanel.setBounds(10, 11, 310, 125);
		contentPane.add(detailsPanel);
		detailsPanel.setLayout(null);
		
		JLabel lblProduct = new JLabel("Product:");
		lblProduct.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 12));
		lblProduct.setBounds(13, 25, 73, 20);
		detailsPanel.add(lblProduct);
		
		JLabel lblSerialKey = new JLabel("Serial Key:");
		lblSerialKey.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 12));
		lblSerialKey.setBounds(13, 55, 73, 20);
		detailsPanel.add(lblSerialKey);
		
		JLabel lblIdentifier = new JLabel("Identifier:");
		lblIdentifier.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 12));
		lblIdentifier.setBounds(13, 85, 73, 20);
		detailsPanel.add(lblIdentifier);
		
		productComboBox = new JComboBox<>();
		productComboBox.setEditable(false);
		productComboBox.setBounds(90, 26, 200, 20);
		detailsPanel.add(productComboBox);
		
		txtSerialKey = new JTextField();
		txtSerialKey.setFont(new Font("DialogInput", Font.PLAIN, 12));
		txtSerialKey.setBounds(90, 56, 200, 20);
		txtSerialKey.setEditable(false);
		detailsPanel.add(txtSerialKey);
		txtSerialKey.setColumns(10);
		
		txtIdentifier = new JTextField();
		txtIdentifier.setFont(new Font("Tahoma", Font.PLAIN, 12));
		txtIdentifier.setBounds(90, 86, 200, 20);
		txtIdentifier.setEditable(false);
		detailsPanel.add(txtIdentifier);
		txtIdentifier.setColumns(10);
		
		JButton btnExport = new JButton("Export");
		btnExport.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					@Override
					public void run() 
					{
						JFileChooser dialog = new JFileChooser();
						dialog.setFileFilter(new FileNameExtensionFilter("License File (.key)","key"));
						int res = dialog.showSaveDialog(contentPane);
						if(res == JFileChooser.APPROVE_OPTION)
						{
							File file = new File(dialog.getSelectedFile() + ".key");
							if(!file.exists())
							{
								try 
								{
									file.createNewFile();
								} 
								catch (IOException ex) 
								{
									TaskDialogs.showException(ex);
								}
							}
							
							Product product = ProductManager.getBySelection((String) productComboBox.getSelectedItem());
							LicenseKey license = new LicenseKey(product.getName(), main.getClient().getAddress().toString(), txtSerialKey.getText());
							FileLicense.writeLicense(license, file);
							
							JOptionPane.showMessageDialog(EditWindow.this, "License file sucessfully created. (" + file.getAbsolutePath() + ")", "MyLicenseControl v1.5.2", JOptionPane.INFORMATION_MESSAGE);
						}
					}
				}.start();
			}
		});
		btnExport.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 13));
		btnExport.setBounds(10, 147, 85, 30);
		contentPane.add(btnExport);
		
		JButton btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				int action = JOptionPane.showConfirmDialog(EditWindow.this, "Do you really want to reset this license?", "MyLicenseControl v1.5.2", JOptionPane.YES_NO_OPTION);  
			    if(action == JOptionPane.YES_OPTION)
			    {
			    	boolean sucess = LicenseHandler.resetUniqueId(new SerialKey() 
			    	{
						@Override
						public String getKey() 
						{
							return key;
						}
					}, uuid);
			    	
			    	if(sucess == true)
			    	{
			    		JOptionPane.showMessageDialog(EditWindow.this, "License key '" + key + "' was sucessfully reset!", "MyLicenseControl v1.5.2", JOptionPane.INFORMATION_MESSAGE);
			    		EditWindow.this.reset = true;
			    		loadData();
			    		return;
			    	}
			    	
			    	EditWindow.this.reset = false;
			    	main.closeWindow(EditWindow.this);
					main.reload();
			    }
			}
		});
		btnReset.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 13));
		btnReset.setBounds(105, 147, 85, 30);
		contentPane.add(btnReset);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				main.closeWindow(EditWindow.this);
				if(EditWindow.this.reset == true)
				{
					main.reload();
				}
			}
		});
		btnCancel.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 13));
		btnCancel.setBounds(246, 147, 75, 30);
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
		this.update = new Thread()
		{	
			@Override
			public void run()
			{
				EditWindow.this.load = new LoadingDialog(EditWindow.this);
				load.revalidate();
				load.open();
				
				productComboBox.removeAllItems();
				txtSerialKey.setText(null);
				txtIdentifier.setText(null);
				
				try
				{
					Thread.sleep(100L);
				} 
				catch (InterruptedException e) {}
				
				Product product = ProductManager.getByName(EditWindow.this.product);
				SerialKey key = new SerialKey() 
				{
					@Override
					public String getKey()
					{
						return EditWindow.this.key;
					}
				};
				
				if(!LicenseHandler.existsLicense(product, key))
				{
					JOptionPane.showMessageDialog(EditWindow.this, "License for product '" + product.getName() + "' with license key '" + key.getKey() + "' not exists.", "MyLicenseControl v1.5.2", JOptionPane.WARNING_MESSAGE);
					MainWindow.getMain().closeWindow(EditWindow.this);
					load.close();
					return;
				}
				
				uuid = LicenseHandler.getUniqueId(product, key);	
				List<License> licenses = new ArrayList<>();
				try
				{
					licenses = LicenseHandler.getLicenses(product);
				}
				catch (QueryServiceException ex) {}
				
				License license = getLicense(licenses);
				productComboBox.addItem(product.getDisplayname() + " (" + product.getName() + ")");
				txtSerialKey.setText(license.getKey().getKey());
				txtIdentifier.setText(license.getUniqueId());
				
				load.close();
				EditWindow.this.load = null;
			}
		};
		update.start();
	}
	
	public License getLicense(List<License> licenses)
	{
		for(License license : licenses)
		{
			if(license.getProduct().getName().equals(product))
			{
				if(license.getKey().getKey().equals(key))
				{
					if(license.getUniqueId().equals(uuid))
					{
						return license;
					}
				}
			}
		}
		return null;
	}
	
	public boolean isLoading()
	{
		return (this.load != null);
	}
}
