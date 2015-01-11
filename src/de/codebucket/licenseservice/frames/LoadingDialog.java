package de.codebucket.licenseservice.frames;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;

public class LoadingDialog extends JDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private Component component;
	
	/**
	 * Create the dialog.
	 */
	public LoadingDialog(Component c) 
	{
		System.gc();
		this.component = c;
		
		setVisible(false);	
		setType(Type.POPUP);
		setResizable(false);
		setTitle("MyLicenseControl v1.5.2");
		setBounds(100, 100, 250, 100);
		setLocationRelativeTo(c);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setAlwaysOnTop(false);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(null);
		setContentPane(contentPanel);
		
		JLabel lblLoading = new JLabel("   Loading data...");
		lblLoading.setIcon(new ImageIcon(LoadingDialog.class.getResource("/de/codebucket/licenseservice/resources/gif_ajax.gif")));
		lblLoading.setHorizontalAlignment(SwingConstants.CENTER);
		lblLoading.setFont(new Font(UIManager.getFont("Panel.font").getName(), UIManager.getFont("Panel.font").getStyle(), 18));
		lblLoading.setBounds(10, 11, 224, 54);
		contentPanel.add(lblLoading);
		
		this.validate();
	}
	
	public void open()
	{
		component.setEnabled(false);
		setAlwaysOnTop(false);
		setVisible(true);
	}
	
	public void close()
	{
		component.setEnabled(true);
		setAlwaysOnTop(false);
		this.dispose();
	}
}
