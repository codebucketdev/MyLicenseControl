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
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JTextField;

import de.codebucket.licenseservice.ProductManager;
import de.codebucket.licenseservice.query.Packet.Status;
import de.codebucket.licenseservice.tools.CompleteTask.LinkedData;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ManagerWindow extends JFrame 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtProductId, txtProductName, txtDisplayname;
	
	/**
	 * Create the dialog.
	 */
	public ManagerWindow(final MainWindow main) 
	{
		System.gc();
		
		setType(Type.POPUP);
		addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				main.closeWindow(ManagerWindow.this);
			}
		});
		
		setResizable(false);
		setTitle("Add Product - MyLicenseControl v1.5.3");
		setBounds(100, 100, 305, 220);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setAlwaysOnTop(false);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(null);
		setContentPane(contentPanel);
		
		JPanel registerPanel = new JPanel();
		registerPanel.setBorder(new TitledBorder(null, "Options..", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		registerPanel.setBounds(10, 10, 280, 130);
		contentPanel.add(registerPanel);
		registerPanel.setLayout(null);
		
		JLabel lblProduct = new JLabel("Product ID:");
		lblProduct.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 12));
		lblProduct.setBounds(13, 25, 108, 15);
		registerPanel.add(lblProduct);
		
		JLabel lblSerialKey = new JLabel("Product name:");
		lblSerialKey.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 12));
		lblSerialKey.setBounds(13, 55, 108, 15);
		registerPanel.add(lblSerialKey);
		
		JLabel lblIdentifier = new JLabel("Displayname:");
		lblIdentifier.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 12));
		lblIdentifier.setBounds(13, 85, 108, 15);
		registerPanel.add(lblIdentifier);
		
		txtProductId = new JTextField();
		txtProductId.setFont(new Font("DialogInput", Font.PLAIN, 12));
		txtProductId.setBounds(115, 23, 150, 20);
		registerPanel.add(txtProductId);
		txtProductId.setColumns(10);
		
		txtProductName = new JTextField();
		txtProductName.setFont(new Font("Tahoma", Font.PLAIN, 12));
		txtProductName.setBounds(115, 53, 150, 20);
		registerPanel.add(txtProductName);
		txtProductName.setColumns(10);
		
		txtDisplayname = new JTextField();
		txtDisplayname.setFont(new Font("Tahoma", Font.PLAIN, 12));
		txtDisplayname.setBounds(115, 83, 150, 20);
		registerPanel.add(txtDisplayname);
		txtDisplayname.setColumns(10);
		
		JButton btnAdd = new JButton("Add..");
		btnAdd.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(!isInteger(txtProductId.getText()) || txtProductName.getText().length() == 0 || txtDisplayname.getText().length() == 0)
				{
					getToolkit().beep();
					return;
				}
				
				int id = Integer.parseInt(txtProductId.getText());
				String name = txtProductName.getText();
				String displayname = txtDisplayname.getText();
				
				LinkedData response = ProductManager.addProduct(id, name, displayname);
				if(response == null)
				{
					JOptionPane.showMessageDialog(ManagerWindow.this, "An internal error has occoured while executing the process!", "MyLicenseControl v1.5.3", JOptionPane.ERROR_MESSAGE);
					main.closeWindow(ManagerWindow.this);
					main.reload();
					return;
				}
				
				if(response.a() == Status.ERROR)
				{
					JOptionPane.showMessageDialog(ManagerWindow.this, response.b(), "MyLicenseControl v1.5.3", JOptionPane.ERROR_MESSAGE);
					main.closeWindow(ManagerWindow.this);
					main.reload();
					return;
				}
				
				JOptionPane.showMessageDialog(ManagerWindow.this, response.b(), "MyLicenseControl v1.5.3", JOptionPane.INFORMATION_MESSAGE);
				main.closeWindow(ManagerWindow.this);
				main.reload();
				return;
			}
		});
		btnAdd.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 13));
		btnAdd.setBounds(102, 151, 89, 30);
		contentPanel.add(btnAdd);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0)
			{
				main.closeWindow(ManagerWindow.this);
			}
		});
		btnCancel.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 13));
		btnCancel.setBounds(201, 151, 89, 30);
		contentPanel.add(btnCancel);
		
		this.validate();
	}
	
	public void open()
	{
		this.revalidate();
		setAlwaysOnTop(false);
		setVisible(true);
	}
	
	public void close()
	{
		this.dispose();
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
