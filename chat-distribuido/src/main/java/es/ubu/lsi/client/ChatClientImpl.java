package es.ubu.lsi.client;

import java.awt.TrayIcon.MessageType;
import java.awt.im.InputContext;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import es.ubu.lsi.common.ChatMessage;

public class   ChatClientImpl implements ChatClient{
	
	private String server;
	private String username;
	private int port;
	private boolean carryOn = true;
	private int id;
	private String line = null;
	
	public ChatClientImpl(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.id = username.hashCode();
		this.carryOn = false;
	}

	@Override
	public boolean start() {
			try {
				Socket socket = new Socket(server, port);
				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
				
				// Enviar el nombre de usuario
				output.writeObject(username);
				
				//Inicializar hilo de escucha
				new Thread(new ChatClientListener(input)).start();
				
				Scanner scanner = new Scanner(System.in);
				while (carryOn) {
					line = scanner.nextLine();
					output.writeObject(new ChatMessage(id, ChatMessage.MessageType.MESSAGE, line));
				}
				socket.close();
				scanner.close();
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return carryOn;
	} 

	@Override
	public void sendMessage(ChatMessage msg) {
		Socket socket;
		try {
			socket = new Socket(server, port);
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			output.writeObject(msg);
			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void disconnect() {
		carryOn = false;
	}
	
	public static void main(String[] args) {
		if (args.length == 2) {
			String server = args[0], username = args[1];
			ChatClientImpl client = new ChatClientImpl(server, 1500, username);
			client.start();
			
			System.out.println("Uso: java ChatClientImpl <servidor> <usuario>");
		}
	}
	
	public class ChatClientListener implements Runnable{
		private ObjectInputStream input;
		
		//Constructor de la clase
		public ChatClientListener(ObjectInputStream input) {
			this.input = input;
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					ChatMessage message = (ChatMessage) input.readObject();
					// Verificar si el remitente está bloqueado
					System.out.println(message.getMessage());
				}catch (Exception ex) {
					System.out.println("Conexión cerrada.");
				}
			}
		}
		
	}
}
