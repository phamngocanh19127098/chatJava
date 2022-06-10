

package projectchat;

import java.io.*;
import java.net.*;
import java.util.ArrayList;


public class Server {
	private Object object;

	private ServerSocket server;
	private Socket socket;
	static ArrayList<Client> clients = new ArrayList<Client>();
	private String path = "data\\accounts.txt";


	private void loadAccounts() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "utf8"));

			String info = br.readLine();
			while (info != null && !(info.isEmpty())) {
                                String [] splitedString = info.split(",");
				clients.add(new Client(splitedString[0], splitedString[1], false, object));
				info = br.readLine();
			}

			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void saveToData() {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File(path), "utf8");
		} catch (Exception ex ) {
			System.out.println(ex.getMessage());
		}
		for (Client client : clients) {
			pw.print(client.getUsername() + "," + client.getPassword() + "\n");
		}
		pw.println("");
		if (pw != null) {
			pw.close();
		}
	}

	public Server() throws IOException {
		try {
			object = new Object();
			this.loadAccounts();
			server = new ServerSocket(3200);

			while (true) {

				socket = server.accept();

				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				String requestClient = dis.readUTF();
				if (requestClient.equals("Log in")) {
					String username = dis.readUTF();
					String password = dis.readUTF();


					if (isValid(username) == false) {
						dos.writeUTF("This username is not exist in this app, please sign up");
						dos.flush();

					} else {
                                                    for (Client client : clients) {
							if (client.getUsername().equals(username)) {

								if (password.equals(client.getPassword())) {


									Client nextClient = client;
									nextClient.setSocket(socket);
									nextClient.setIsLoggedIn(true);


									dos.writeUTF("Successfully log in");
									dos.flush();


									Thread t = new Thread(nextClient);
									t.start();


									updateOnlineUsers();
								} else {
									dos.writeUTF("Username or Password is not correct");
									dos.flush();
								}
								break;
							}
						}
						
					}
				} else if (requestClient.equalsIgnoreCase("Sign up")) {
                                        String username = dis.readUTF();
					String password = dis.readUTF();

					if (isValid(username) == true) {

                                                dos.writeUTF("This username is being used");
						dos.flush();
						
					} else {

                                                Client nextClient = new Client(socket, username, password, true, object);
						clients.add(nextClient);


						this.saveToData();
						dos.writeUTF("Sign up successful");
						dos.flush();

						Thread t = new Thread(nextClient);
						t.start();

						updateOnlineUsers();
						
					}

					
				}

			}

		} catch (Exception ex){
			System.err.println(ex);
		} finally {
			if (server != null) {
				server.close();
			}
		}
	}


	public boolean isValid(String name) {
		for (Client client:clients) {
			if (client.getUsername().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public static void updateOnlineUsers() {
		String message = " ";
		for (Client client:clients) {
			if (client.getIsLoggedIn() == true) {
				message += ",";
				message += client.getUsername();
			}
		}
		for (Client client:clients) {
			if (client.getIsLoggedIn() == true) {
				try {
					client.getDos().writeUTF("Online users");
					client.getDos().writeUTF(message);
					client.getDos().flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}


class Client implements Runnable{

	private Object object;

	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private String username;
	private String password;
	private boolean isAuth;

	public Client(Socket socket, String username, String password, boolean isAuth, Object object) throws IOException {
		this.socket = socket;
		this.username = username;
		this.password = password;
		this.dis = new DataInputStream(socket.getInputStream());
		this.dos = new DataOutputStream(socket.getOutputStream());
		this.isAuth = isAuth;
		this.object = object;
	}

	public Client(String username, String password, boolean isAuth, Object object) {
		this.username = username;
		this.password = password;
		this.isAuth = isAuth;
		this.object = object;
	}

	public void setIsLoggedIn(boolean IsLoggedIn) {
		this.isAuth = IsLoggedIn;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
		try {
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void closeSocket() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean getIsLoggedIn() {
		return this.isAuth;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public DataOutputStream getDos() {
		return this.dos;
	}

	@Override
	public void run() {

		while (true) {
			try {
                            String message = null;


                            message = dis.readUTF();

				if (message.equals("Log out")) {

                                    dos.writeUTF("Safe to leave");
                                    dos.flush();

                                    socket.close();
                                    this.isAuth = false;

                                    Server.updateOnlineUsers();
                                    break;
				}


				else if (message.equals("Text")){
                                    String receiver = dis.readUTF();
                                    String content = dis.readUTF();

					for (Client client: Server.clients) {
						if (client.getUsername().equals(receiver)) {
                                                    synchronized (object) {
							client.getDos().writeUTF("Text");
							client.getDos().writeUTF(this.username);
							client.getDos().writeUTF(content);
							client.getDos().flush();
							break;
                                                    }
						}
					}
				}

				else if (message.equals("File")) {

                                    String receiver = dis.readUTF();
                                    String filename = dis.readUTF();
                                    int size = Integer.parseInt(dis.readUTF());
                                    int bufferSize = 2048;
                                    byte[] buffer = new byte[bufferSize];

                                    for (Client client: Server.clients) {
                                        if (client.getUsername().equals(receiver)) {
                                                synchronized (object) {
                                                    client.getDos().writeUTF("File");
                                                    client.getDos().writeUTF(this.username);
                                                    client.getDos().writeUTF(filename);
                                                    client.getDos().writeUTF(String.valueOf(size));
                                                    while (size > 0) {
							dis.read(buffer, 0, Math.min(size, bufferSize));
							client.getDos().write(buffer, 0, Math.min(size, bufferSize));
							size -= bufferSize;
							}
							client.getDos().flush();
							break;
                                                    }
						}
                                            }
                                        }

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}