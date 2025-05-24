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

import es.ubu.lsi.client.*;
import es.ubu.lsi.common.*;

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
		clientId = 0;
	}

	@Override
	public void startup() {
		try {
			// Arrancamos la escucha de peticiones
			alive = true;
			
			// Inicializamos los sockets
			ServerSocket serverSocket = new ServerSocket(port);
			Socket socket;
			while (alive) {
				socket = serverSocket.accept();
				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
				//new ServerThreadForClient(socket, 1, input).start();
				
				String message = input.readUTF();
				
				System.out.println(message);
				
				socket.close();
				
			}
		} catch (IOException e) {
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
		// TODO Auto-generated method stub
		
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
		private ObjectOutputStream output;
		
		public ServerThreadForClient(Socket socket, int id, ObjectOutputStream output) {
			this.id = id;
			this.socket = socket;
			this.output = output;
		}
		
		public void run() {
			try {
				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
				
				username = input.readObject().toString();
				
				output.writeInt(id);
				
				while(alive) {
					ChatMessage msg = (ChatMessage) input.readObject();
					System.out.println(msg.getMessage());
					// broadcast(msg);
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
