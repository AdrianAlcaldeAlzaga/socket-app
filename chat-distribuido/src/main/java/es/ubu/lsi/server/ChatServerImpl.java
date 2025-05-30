package es.ubu.lsi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.ubu.lsi.common.ChatMessage;

public class ChatServerImpl implements ChatServer {
	
	private static final int DEFAULT_PORT = 1500;
	private int clientId;
	private SimpleDateFormat sdf;
	private int port;
	private boolean alive;
	ServerSocket serverSocket;
	
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
			serverSocket.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		alive = false;
		synchronized (clients) {
			for (ServerThreadForClient client : clients) {
				try {
					client.input.close();
					client.output.close();
					client.socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			clients.clear();
			System.out.println("Servidor Apagado.");
		}
	}

	@Override
	public void broadcast(ChatMessage message) {
		synchronized (clients) {
			for (ServerThreadForClient client: clients) {
				try {
					client.output.writeObject(message);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
					ChatMessage msg = (ChatMessage) input.readObject();
					
					
					if (msg.getType() == ChatMessage.MessageType.LOGOUT) {
						remove(id);
						break;
					}else if(msg.getType() == ChatMessage.MessageType.SHUTDOWN)
						shutdown();
					System.out.println(username + ": " + msg.getMessage());
					
					//Reenviamos el mensaje al resto de clientes
					broadcast(msg);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
