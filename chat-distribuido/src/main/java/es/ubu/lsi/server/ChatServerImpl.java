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
	
	private List<ServerThreadForClient> clients = Collections.synchronizedList(new ArrayList<>());

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
			ServerSocket serverSocket = new ServerSocket(port);
			System.out.println("Servidor inicializado en el puerto " + port);
			Socket socket = null;
			while (alive) {
				socket = serverSocket.accept();
				
				ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
				
				String username = input.readObject().toString();
				
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void broadcast(ChatMessage message) {
		synchronized (clients) {
			for (ServerThreadForClient client: clients) {
				try {
					client.outputStream.writeObject(message);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void remove(int id) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		ChatServerImpl server = new ChatServerImpl(DEFAULT_PORT);
		server.startup();
	}
	
	public class ServerThreadForClient extends Thread{
		private int id;
		private String username;
		private Socket socket;
		private ObjectOutputStream outputStream;
		private ObjectInputStream inputStream;
		
		public ServerThreadForClient(int id, String username,  Socket socket, ObjectOutputStream outputStream, ObjectInputStream inputStream) {
			this.id = id;
			this.username = username;
			this.socket = socket;
			this.outputStream = outputStream;
			this.inputStream = inputStream;
		}
		
		public void run() {
			try {
				System.out.println(username + " se ha conectado con id: " + id);
				synchronized (clients) {
					clients.add(this);
				}
				
				while(alive) {
					ChatMessage msg = (ChatMessage) inputStream.readObject();
					if (msg.getType() == ChatMessage.MessageType.LOGOUT)
						break;
					System.out.println(username + ": " + msg.getMessage());
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
