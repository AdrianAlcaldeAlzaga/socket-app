package es.ubu.lsi.client;

import java.awt.TrayIcon.MessageType;
import java.awt.im.InputContext;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
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
	ObjectInputStream input;
	ObjectOutputStream output;
	
	public ChatClientImpl(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.id = username.hashCode();
		this.carryOn = true;
	}

	@Override
	public boolean start() {
			try {
				carryOn = true;
				// Conexion con el servidor
				Socket socket = new Socket(server, port);

				// Inicializamos los Flujos 
				output = new ObjectOutputStream(socket.getOutputStream());
				output.flush();
				input = new ObjectInputStream(socket.getInputStream());
				
				// Enviar el nombre de usuario
				output.writeObject(username);
				
				//Inicializar hilo de escucha del servidor
				new Thread(new ChatClientListener(input)).start();
				Scanner scanner = new Scanner(System.in);
				while (carryOn) {
					line = scanner.nextLine();
					
					if(line.equals("logout")) {
						sendMessage(new ChatMessage(id, ChatMessage.MessageType.LOGOUT, line));
						disconnect();
					}else if(line.equals("shutdown"))
						sendMessage(new ChatMessage(id, ChatMessage.MessageType.SHUTDOWN, line));
					else
						sendMessage(new ChatMessage(id, ChatMessage.MessageType.MESSAGE, line));
				}
				socket.close();
				output.close();
				input.close();
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
		try {
			output.writeObject(msg);
			output.flush();
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
		System.out.println("Se ha desconectado el usuario: " + username);
	}
	
	public static void main(String[] args) {
		if (args.length == 2) {
			String server = args[0], username = args[1];
			ChatClientImpl client = new ChatClientImpl(server, 1500, username);
			client.start();
			
			System.out.println("Uso: java ChatClientImpl <servidor> <usuario>");
		}else {
			System.out.println("Error: Se deben proporcionar dos argumentos en la entrada del cliente ");
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
			try {
				while(carryOn) {
					ChatMessage message = (ChatMessage) input.readObject();
					System.out.println(message.getMessage());
				}
			}catch (Exception ex) {
				System.out.println("Conexión cerrada.");
			}
		}
		
	}
}
