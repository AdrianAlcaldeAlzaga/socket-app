package es.ubu.lsi.server;

import java.text.SimpleDateFormat;

import es.ubu.lsi.common.ChatMessage;

public class ChatServerImpl implements ChatServer {
	
	private int DEFAULT_PORT = 1500;
	private int clientId;
	private SimpleDateFormat sdf;
	private int port;
	private boolean alive;
	
	

	public ChatServerImpl(int port) {
		super();
		this.port = port;
	}

	@Override
	public void startup() {
		// TODO Auto-generated method stub
		
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
		
	}
	
	public class ServerThreadForClient {
		public int id;
		public String username;
		
		public void run() {
			
		}
	}
	
}
