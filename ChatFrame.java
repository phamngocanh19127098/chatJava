/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package projectchat;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author phamt
 */
public class ChatFrame extends javax.swing.JFrame {
    private String username;
	private DataInputStream dis;
	private DataOutputStream dos;
	private HashMap<String, JTextPane> showChat = new HashMap<String, JTextPane>();
	Thread receiver;
        private JTextPane chatWindow;
        ArrayList <String> receivedMess ;
	public void autoScroll() {
		showMessage.getVerticalScrollBar().setValue(showMessage.getVerticalScrollBar().getMaximum());
	}
	public void setUsername(String username) {
		this.username = username;
	}
    /**
     * Creates new form ChatFrame
     */
    private void sendMessage(String username, String message, Boolean isAuthenticate) {
                 receivedMess.add(message);
		StyledDocument doc;
		if (username.equals(this.username)) {
			doc = showChat.get(lblotherUsername.getText()).getStyledDocument();
		} else {
			doc = showChat.get(username).getStyledDocument();
		}

		Style userStyle = doc.getStyle("");

		if (userStyle == null) {
			userStyle = doc.addStyle("", null);
			StyleConstants.setBold(userStyle, true);
		}
		
                StyleConstants.setForeground(userStyle, Color.BLACK);

		if (isAuthenticate) {
	    	StyleConstants.setForeground(userStyle, Color.red);
                } 
	    	
	    

	    try {
                    doc.insertString(doc.getLength(), username + ": ", userStyle);
                 }
        catch (BadLocationException e){
                System.err.println("");
}
	    
	    Style messageStyle = doc.getStyle("");
		if (messageStyle == null) {
                    messageStyle = doc.addStyle("", null);
		    StyleConstants.setForeground(messageStyle, Color.BLACK);
		    StyleConstants.setBold(messageStyle, false);
		}
	   

	    try { doc.insertString(doc.getLength(), message + "\n",messageStyle); }
        catch (BadLocationException e){

}
	    
	    autoScroll();
	}
    public ChatFrame(String username, DataInputStream dis, DataOutputStream dos) {
        initComponents();
        receivedMess = new ArrayList<String>();
        setResizable(false);
	setTitle("CHAT CHAT");

	this.username = username;
	this.dis = dis;
	this.dos = dos;
	receiver = new Thread(new Receiver(dis));
        receiver.start();
        onlineUsers.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					lblotherUsername.setText((String) onlineUsers.getSelectedItem());
					if (chatWindow != showChat.get(lblotherUsername.getText())) {
						txtMessage.setText("");
						chatWindow = showChat.get(lblotherUsername.getText());
						showMessage.setViewportView(chatWindow);
						showMessage.validate();
					}
					
					if (lblotherUsername.getText().isEmpty()) {
						btnSend.setEnabled(false);
						btnFile.setEnabled(false);
						txtMessage.setEnabled(false);
					} else {
						btnSend.setEnabled(true);
						btnFile.setEnabled(true);
						txtMessage.setEnabled(true);
					}
				}

			}
		});
        this.getRootPane().setDefaultButton(btnSend);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				
				try {
					dos.writeUTF("Log out");
					dos.flush();
					
					try {
						receiver.join();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					
					if (dos != null) {
						dos.close();
					}
					if (dis != null) {
						dis.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
        btnSend.setEnabled(false);
        btnFile.setEnabled(false);
        lblcurrentUser.setText(username);
    }
    private void sendFile(String username, String filename, byte[] file, Boolean isAuthenticate) {
                
		StyledDocument doc;
		String window = null;
		if (username.equals(this.username)) {
			window = lblotherUsername.getText();
		} else {
			window = username;
		}
		doc = showChat.get(window).getStyledDocument();
		
		Style userStyle = doc.getStyle("");
		if (userStyle == null) {
			userStyle = doc.addStyle("", null);
			StyleConstants.setBold(userStyle, true);
		}
		
		if (isAuthenticate == true) {
	    	StyleConstants.setForeground(userStyle, Color.red);
	    } else {
	    	StyleConstants.setForeground(userStyle, Color.BLUE);
	    }

	    try { doc.insertString(doc.getLength(), username + ": ", userStyle); }
        catch (BadLocationException e){}
		
	    Style linkStyle = doc.getStyle("Link style");
	    if (linkStyle == null) {
	    	linkStyle = doc.addStyle("Link style", null);
	    	StyleConstants.setForeground(linkStyle, Color.BLUE);
			StyleConstants.setUnderline(linkStyle, true);
			StyleConstants.setBold(linkStyle, true);
			linkStyle.addAttribute("link", new SendFileListener(filename, file));
	    }
	    if (showChat.get(window).getMouseListeners() != null) {

			showChat.get(window).addMouseListener(new MouseListener() {
	
				@Override
				public void mouseClicked(MouseEvent e)
		        {
					Element ele = doc.getCharacterElement(chatWindow.viewToModel(e.getPoint()));
		            AttributeSet as = ele.getAttributes();
		            SendFileListener listener = (SendFileListener)as.getAttribute("link");
		            if(listener != null)
		            {
		                listener.execute();
		            }
		        }
				
				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub

				}
				
			});
		}
	    

		try {
			doc.insertString(doc.getLength(),"<" + filename + ">", linkStyle);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		

		try {
			doc.insertString(doc.getLength(), "\n", userStyle);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		
		autoScroll();
	}

    class SendFileListener extends AbstractAction {
		String filename;
		byte[] file;
		
		public SendFileListener(String filename, byte[] file) {
			this.filename = filename;
			this.file = Arrays.copyOf(file, file.length);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			execute();
		}
		
		public  void execute() {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setSelectedFile(new File(filename));
			int rVal = fileChooser.showSaveDialog(jPanel1.getParent());
			if (rVal == JFileChooser.APPROVE_OPTION) {

				File saveFile = fileChooser.getSelectedFile();
				BufferedOutputStream bos = null;
				try {
					bos = new BufferedOutputStream(new FileOutputStream(saveFile));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				

				int nextAction = JOptionPane.showConfirmDialog(null, "Saved file to " + saveFile.getAbsolutePath() + "\nDo you want to open this file?", "Successful", JOptionPane.YES_NO_OPTION);
				if (nextAction == JOptionPane.YES_OPTION) {
					try {
						Desktop.getDesktop().open(saveFile);
					} catch (IOException e) {
						e.printStackTrace();
					} 
				}
				
				if (bos != null) {
					try {
						bos.write(this.file);
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		    }
		}
	}
    class Receiver implements Runnable{

		private DataInputStream dis;
		
		public Receiver(DataInputStream dis) {
			this.dis = dis;
		}
		
		@Override
		public void run() {
			try {		
					
				while (true) {

					String method = dis.readUTF();
					
					if (method.equals("Text")) {

						String sender =	dis.readUTF();
						String message = dis.readUTF();

						sendMessage(sender, message, false);
					}

					
					else if (method.equals("File")) {

						String sender = dis.readUTF();
						String filename = dis.readUTF();
						int size = Integer.parseInt(dis.readUTF());
						int bufferSize = 2048;
						byte[] buffer = new byte[bufferSize];
						ByteArrayOutputStream file = new ByteArrayOutputStream();
						
						while (size > 0) {
							dis.read(buffer, 0, Math.min(bufferSize, size));
							file.write(buffer, 0, Math.min(bufferSize, size));
							size = size -  bufferSize;
						}
						

						sendFile(sender, filename, file.toByteArray(), false);
						
					}
					
					else if (method.equals("Online users")) {

						
						
						String currentUser = lblotherUsername.getText();
						String[] users = dis.readUTF().split(",");

						onlineUsers.removeAllItems();
						boolean isChattingOnline = false;
						
						for (String user: users) {
							if (user.equals(username) == false) {

								onlineUsers.addItem(user);
								if (showChat.get(user) == null) {
									JTextPane temp = new JTextPane();
									temp.setFont(new Font("Arial", Font.PLAIN, 14));
									temp.setEditable(false);
									showChat.put(user, temp);
								}
							}
							if (currentUser.equals(user)||currentUser.equals("")) {
								isChattingOnline = true;
							}
						}
						
						if (isChattingOnline == false) {

							onlineUsers.setSelectedItem(" ");
							JOptionPane.showMessageDialog(null, currentUser + " is out!\nAutomatically redirect to default UI");
                                                        btnSend.setEnabled(false);
                                                        btnFile.setEnabled(false);
						} else {
							onlineUsers.setSelectedItem(currentUser);
						}
						
						onlineUsers.validate();
					}
					
					else if (method.equals("Safe to leave")) {

						break;
					}
					
				}
				
			} catch(IOException ex) {
				System.err.println(ex);
			} finally {
				try {
					if (dis != null) {
						dis.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        lblotherUsername = new javax.swing.JLabel();
        showMessage = new javax.swing.JScrollPane();
        txtMessage = new javax.swing.JTextField();
        btnFile = new javax.swing.JButton();
        btnSend = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        onlineUsers = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        lblcurrentUser = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblotherUsername.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(352, 352, 352)
                .addComponent(lblotherUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblotherUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addContainerGap())
        );

        txtMessage.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        txtMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMessageActionPerformed(evt);
            }
        });

        btnFile.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btnFile.setText("File");
        btnFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFileActionPerformed(evt);
            }
        });

        btnSend.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btnSend.setText("Send");
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setText("Online user");
        jLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        onlineUsers.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel2.setText("   Type");
        jLabel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblcurrentUser.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lblcurrentUser.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Hello");
        jLabel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(lblcurrentUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblcurrentUser, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(onlineUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 539, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(showMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 810, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(55, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(onlineUsers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(63, 63, 63)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(showMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 526, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFile, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMessage)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
        // TODO add your handling code here:
       
				
        try {
        	dos.writeUTF("Text");
        	dos.writeUTF(lblotherUsername.getText());
        	dos.writeUTF(txtMessage.getText());
        	dos.flush();
        } catch (IOException e1) {
        	e1.printStackTrace();
        	sendMessage("ERROR" , "Network error!" , true);
        }
				

        sendMessage(username , txtMessage.getText() , true);
        txtMessage.setText("");
			
        
    }//GEN-LAST:event_btnSendActionPerformed

    private void txtMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMessageActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMessageActionPerformed

    private void btnFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFileActionPerformed
        // TODO add your handling code here:
        
				

	JFileChooser fileChooser = new JFileChooser();
	int rVal = fileChooser.showOpenDialog(jPanel1.getParent());
	if (rVal == JFileChooser.APPROVE_OPTION) {
		byte[] selectedFile = new byte[(int) fileChooser.getSelectedFile().length()];
		BufferedInputStream bis;
		try {
			bis = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()));
			bis.read(selectedFile, 0, selectedFile.length);
			
			dos.writeUTF("File");
			dos.writeUTF(lblotherUsername.getText());
			dos.writeUTF(fileChooser.getSelectedFile().getName());
			dos.writeUTF(String.valueOf(selectedFile.length));
			
			int size = selectedFile.length;
			int bufferSize = 4096;
			int offset = 0;
			while (size > 0) {
				dos.write(selectedFile, offset, Math.min(size, bufferSize));
				offset += Math.min(size, bufferSize);
				size -= bufferSize;
			} 
			dos.flush();
			
			bis.close();
			sendFile(username, fileChooser.getSelectedFile().getName(), selectedFile, true);
		} catch (IOException e1) {
			e1.printStackTrace();
					}
				}
		
    }//GEN-LAST:event_btnFileActionPerformed

    /**
     * @param args the command line arguments
     */
   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFile;
    private javax.swing.JButton btnSend;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblcurrentUser;
    private javax.swing.JLabel lblotherUsername;
    private javax.swing.JComboBox<String> onlineUsers;
    private javax.swing.JScrollPane showMessage;
    private javax.swing.JTextField txtMessage;
    // End of variables declaration//GEN-END:variables
}
