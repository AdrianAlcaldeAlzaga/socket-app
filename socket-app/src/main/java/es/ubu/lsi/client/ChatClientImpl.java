package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

public class ChatClientImpl implements ChatClient{
	
	private String server;
	private String username;
	private int port;
	private boolean carryOn = true;
	private int id;
	
	public ChatClientImpl(String server, int port, String username) {
		
	}

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void sendMessage(ChatMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		
	}
	
	public class ChatClientListener {
		public void run() {
			
		}
	}

}
