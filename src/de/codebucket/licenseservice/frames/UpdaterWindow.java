package de.codebucket.licenseservice.frames;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;

import de.codebucket.licenseservice.FrameRunner;
import de.codebucket.licenseservice.MainActivity;
import de.codebucket.licenseservice.util.UpdateTask;
import de.codebucket.licenseservice.util.UpdateTask.Download;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class UpdaterWindow extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean cancelled;
	private Download download;
	private JPanel contentPane;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		FrameRunner.run(UpdaterWindow.class);
	}
	
	/**
	 * Create the frame.
	 */
	public UpdaterWindow()
	{		
		setResizable(false);
		setTitle("MyLicenseControl Updater v1.5.2");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 200);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		final JLabel lblStatus = new JLabel();
		lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblStatus.setIcon(new ImageIcon(UpdaterWindow.class.getResource("/de/codebucket/licenseservice/resources/gif_load.gif")));
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatus.setBounds(12, 12, 420, 80);
		contentPane.add(lblStatus);
		
		final JLabel lblDetails = new JLabel();
		lblDetails.setHorizontalAlignment(SwingConstants.CENTER);
		lblDetails.setFont(new Font("Dialog", Font.BOLD, 12));
		lblDetails.setBounds(12, 104, 420, 17);
		contentPane.add(lblDetails);
		setVisible(true);
		
		final JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setBounds(12, 133, 328, 25);
		contentPane.add(progressBar);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0)
			{
				cancelled = true;
				if(download != null && download.getStatus() == Download.DOWNLOADING)
				{
					download.cancel();
					return;
				}
				
				if(getDefaultCloseOperation() == DO_NOTHING_ON_CLOSE)
				{
					return;
				}
				
				new Thread(new Runnable()
				{
					public void run() 
					{
						try 
						{
							Thread.sleep(500);
						} 
						catch (InterruptedException e) {}
						
						if(getDefaultCloseOperation() == DISPOSE_ON_CLOSE)
						{
							dispose();
							return;
						}
						System.exit(1);
					}
				}).start();
			}
		});
		btnCancel.setBounds(352, 133, 80, 25);
		contentPane.add(btnCancel);
		FrameRunner.centerWindow(this);
		
		progressBar.setIndeterminate(true);
		lblStatus.setText("Checking for new updates...");
		lblDetails.setText("Loading data from latest repository on Github...");
		new UpdateTask(UUID.randomUUID(), "https://raw.githubusercontent.com/codebucketdev/MyLicenseControl/master/src/de/codebucket/licenseservice/resources/version.json", UpdateTask.CURRENT_UPDATE) 
		{
			@Override
			public void updateSucess(Update update) 
			{
				if(cancelled == true)
				{
					return;
				}
				
				if(update == null)
				{
					lblStatus.setIcon(null);
					lblStatus.setText("An error occurred while checking for new updates!");
					lblDetails.setText("Nothing has been changed. You can close this window.");
					setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					
					progressBar.setIndeterminate(false);
					JOptionPane.showMessageDialog(null, "An error occurred while checking for new updates.", "MyLicenseControl Updater v1.5.2", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if(update.getName().equals(getCurrent().getName()))
				{
					if(update.getVersion().equalsIgnoreCase(getCurrent().getVersion()))
					{
						lblStatus.setIcon(null);
						lblStatus.setText("No Update found! You have already the newest version.");
						lblDetails.setText("Nothing has been changed. You can close this window.");
						setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						
						progressBar.setIndeterminate(false);
						JOptionPane.showMessageDialog(null, "No Update found! You have already the newest version.", "MyLicenseControl Updater v1.5.2", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					progressBar.setIndeterminate(false);
					lblStatus.setText("Waiting for user response...");
					lblDetails.setText("New Update found! Update version: " + update.getVersion());
					int result = JOptionPane.showConfirmDialog(null, "New Update found!\nUpdate version: " + update.getVersion() + "\nYour version: " + getCurrent().getVersion() + "\n\nDo you want to download the new Update?", "MyLicenseControl Updater v1.5.2", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(result == JOptionPane.YES_OPTION)
					{
						try 
						{
							download = new Download(new URL(update.getUpdate()));
							new Thread(new Runnable() 
							{
								public void run() 
								{
									lblStatus.setText("Downloading update... Please wait.");
									while(download.getStatus() == Download.DOWNLOADING)
									{
										progressBar.setValue((int) download.getProgress());
										int downloaded = (download.getDownloaded() / 1024);
										int size = (download.getSize() / 1024);
										
										lblDetails.setText("Downloading file: " + downloaded + "kB /" + size + "kB (" + (int) download.getProgress() + "%)");
									}
									
									String status = null;
									switch(download.getStatus())
									{
										case Download.COMPLETE:
											status = "Finished! Update sucessfully downloaded.";
											break;
										
										case Download.ERROR:
											status = "An error occurred while trying to download a new update!";
											break;
										
										case Download.PAUSED:
											status = "The update has been paused, no network available.";
											break;
											
										default:
											status = "Update has been cancelled by user.";
											break;
									}
									
									if(download.getStatus() != Download.COMPLETE)
									{
										lblStatus.setIcon(null);
										lblStatus.setText(status);
										lblDetails.setText("Nothing has been changed. You can close this window.");
										setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
										
										File file = new File(download.getFileName());
										if(file.exists())
										{
											file.delete();
										}
										
										if(download.getStatus() == Download.ERROR)
										{
											JOptionPane.showMessageDialog(null, "An error occurred while trying to download a new update.", "MyLicenseControl Updater v1.5.2", JOptionPane.ERROR_MESSAGE);
										}
										
										return;
									}
									
									//Release unused files
									System.gc();
									
									File jar = getJarFile();
									if(jar.exists())
									{
										jar.deleteOnExit();
									}
								
									lblStatus.setIcon(null);
									lblStatus.setText("Finished! Update sucessfully downloaded.");
									lblDetails.setText("File sucessfully downloaded!");
									setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
									progressBar.setValue(100);
									
									int result = JOptionPane.showConfirmDialog(null, "Update sucessfully downloaded!\nTo apply the changes, this program needs to be restarted.\nDo you like to restart this application?", "MyLicenseControl Updater v1.5.2", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
									if(result == JOptionPane.YES_OPTION)
									{
										try
										{
											restartApplication(new File(download.getFileName()));
										}
										catch(Exception ex) 
										{
											System.exit(0);
										}
									}
								}
							}).start();
						}
						catch (Exception ex) {}
					}
					else
					{
						lblStatus.setIcon(null);
						lblStatus.setText("Update has been cancelled by user.");
						lblDetails.setText("Nothing has been changed. You can close this window.");
						setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					}
				}
			}
		}.check();
	}
	
	public void restartApplication() throws IOException, URISyntaxException 
	{
		final File currentJar = new File(MainActivity.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		/* is it a jar file? */
		if (!currentJar.getName().endsWith(".jar"))
			return;
		
		restartApplication(currentJar);
	}
	
	public void restartApplication(File jar) throws IOException
	{
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		
		/* Build command: java -jar application.jar */
		final ArrayList<String> command = new ArrayList<String>();
		command.add(javaBin);
		command.add("-jar");
		command.add(jar.getPath());

		final ProcessBuilder builder = new ProcessBuilder(command);
		builder.start();
		System.exit(0);
	}
	
	public File getJarFile()
	{
		try
		{
			final File currentJar = new File(MainActivity.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (!currentJar.getName().endsWith(".jar"))
				return null;
			
			return currentJar;
		}
		catch (Exception ex) {}
		return null;
	}
	
	public enum UpdateStatus
	{
		CHECK_UPDATE, CONFIRM_UPDATE, DOWNLOAD_UPDATE;
	}
}
