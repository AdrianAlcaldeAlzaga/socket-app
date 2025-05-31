package es.ubu.lsi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.ubu.lsi.common.ChatMessage;

public class ChatServerImpl implements ChatServer {
	
	private static final int DEFAULT_PORT = 1500;
	private int clientId;
//	private SimpleDateFormat sdf;
	private int port;
	private boolean alive;
	ServerSocket serverSocket;
	private Set<String> globalBannedUsers = new HashSet<String>();
	
	private List<ServerThreadForClient> clients = new ArrayList<>();

	public ChatServerImpl(int port) {
		this.port = port;
		this.alive = false;
		this.clientId = 0;
	}

	@Override
	public void startup() {
		try {
			// Arrancamos la escucha de peticiones
			alive = true; 	
			
			// Inicializamos los sockets
			serverSocket = new ServerSocket(port);
			System.out.println("Servidor inicializado en el puerto " + port);
			Socket socket = null;
			
			// Bucle para la escucha del servidor
			while (alive) {
				try {
					// Conexiones entrantes
					socket = serverSocket.accept();
					
					// Flujos de entrada y salida
					ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
					
					// Nombre del cliente
					String username = input.readObject().toString();
					
					// Arrancamos al cliente
					ServerThreadForClient clientThread =  new ServerThreadForClient(clientId++, username, socket, output, input);
					clients.add(clientThread);
					clientThread.start();
				}
				catch (IOException e) {
					if(alive)
						System.out.println("Error al aceptar la conexión: " + e.getMessage());
					else
						System.out.println("Servidor detenido");
					break; // Detenemos el bucle del socket
				}
			} 
		} catch (ClassNotFoundException e) {
			System.err.println("Error al leer el nombre de usuario: " + e.getMessage());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			closeServerSocket();
		}
	}

	@Override
	public void shutdown() {
		System.out.println("Apagando el servidor...");
		alive = false;
		synchronized (clients) {
			// Enviamos mensaje de shutdown a los clientes
			for (ServerThreadForClient client : clients) {
				try {
					ChatMessage shutdownMsg = new ChatMessage(0, ChatMessage.MessageType.SHUTDOWN,
							"El servidor se ha apagado");
					client.output.writeObject(shutdownMsg);
					client.output.flush();
					client.closeClientConnections();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			clients.clear();
		}
		closeServerSocket();
		System.out.println("Servidor apagado correctamente");
	}
	
	private void closeServerSocket() {
		if (serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.err.println("Error al cerrar el Socket del Servidor: " + e.getMessage());
			}
		}
	}

	@Override
	public void broadcast(ChatMessage message) {
		synchronized (clients) {
			for (ServerThreadForClient client: clients) {
				try {
					client.output.writeObject(message);
					client.output.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					clients.remove(client.id);
				}
			}
		}
	}

	@Override
	public void remove(int id) {
		String userElim = null;
		synchronized (clients) {
			for (ServerThreadForClient client : clients) {
				if (client.id == id) {
					userElim = client.username;
				}
			}
			clients.removeIf(client -> client.id == id);
			System.out.println("El usuario " + userElim + " ha sido eliminado");
		}
		
	}
	
	public static void main(String[] args) {
		ChatServerImpl server = new ChatServerImpl(DEFAULT_PORT);
		server.startup();
	}
	
	
	/**
	 * Conexion con cada cliente específico
	 */
	public class ServerThreadForClient extends Thread{
		private int id;
		private String username;
		private Socket socket;
		private ObjectOutputStream output;
		private ObjectInputStream input;
		
		public ServerThreadForClient(int id, String username,  Socket socket, ObjectOutputStream output, ObjectInputStream input) {
			this.id = id;
			this.username = username;
			this.socket = socket;
			this.output = output ;
			this.input = input;
		}
		
		public void run() {
			try {
				System.out.println(username + " se ha conectado con id: " + id);
				
				while(alive) {
					ChatMessage msg = null;
					try {
						msg = (ChatMessage) input.readObject();
						System.out.println(username + ": " + msg.getMessage());
					} catch (SocketException s) {
						System.out.println("Conexion con el socket cerrada");
						break;
					}
					
					if (msg.getType() == ChatMessage.MessageType.LOGOUT) {
						remove(id);
						break;
					} else if (msg.getType() == ChatMessage.MessageType.SHUTDOWN) {
						shutdown();
						break;
					} else if(msg.getMessage().startsWith("ban "))
						banUser(msg.getMessage());
					else if(msg.getMessage().startsWith("unban "))
						unbanUser(msg.getMessage());
					else {
						//Reenviamos el mensaje al resto de clientes
						if (!globalBannedUsers.contains(username)) {
							String original = msg.getMessage();
							String patrocinado = "Adrián patrocina el mensaje: " + original;
							ChatMessage patrocinadoMsg = new ChatMessage(msg.getId(), msg.getType(), patrocinado);
							broadcast(patrocinadoMsg);
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				alive = false;
				closeClientConnections();
			}
		}
		
		private void banUser(String message) {
			String[] parts = message.split(" ");
			String user = parts[1];
			
			if(!globalBannedUsers.contains(user) && !user.equals(username) && !globalBannedUsers.contains(username)) {
				globalBannedUsers.add(user);
				System.out.println(username + " ha baneado a " + user);
			} else if(user.equals(username))
				System.out.println("No se puede banear a uno mismo");
			else if(globalBannedUsers.contains(username))
				System.out.println("Un usuario baneado no puede banear a otro");
			else
				System.out.println(user + " ya está baneado");
		}
		
		private void unbanUser(String message) {
			String[] parts = message.split(" ");
			String user = parts[1];
			
			if(globalBannedUsers.contains(user) && !user.equals(username) && !globalBannedUsers.contains(username)) {
				globalBannedUsers.remove(user);
				System.out.println(username + " ha desbaneado a " + user);
			} else if(user.equals(username))
				System.out.println("No se puede desbanear a uno mismo");
			else if(globalBannedUsers.contains(username))
				System.out.println("Un usuario baneado no puede desbanear a otro");
			else
				System.out.println(user + " no está baneado");
		}

		public void closeClientConnections(){
			try {
				if (input != null) {
					input.close();
				}
				if (output != null) {
					output.close();
				}
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
