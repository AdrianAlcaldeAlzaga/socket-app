package es.ubu.lsi.client;

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
	ObjectInputStream input;
	ObjectOutputStream output;
	private Socket socket;
	private Scanner scanner;
	
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
				socket = new Socket(server, port);

				// Inicializamos los Flujos 
				output = new ObjectOutputStream(socket.getOutputStream());
				output.flush();
				input = new ObjectInputStream(socket.getInputStream());
				
				// Enviar el nombre de usuario
				output.writeObject(username);
				
				//Inicializar hilo de escucha del servidor
				new Thread(new ChatClientListener(input)).start();
				scanner = new Scanner(System.in);
				while (carryOn) {
					line = scanner.nextLine();
					
					if(line.equals("logout")) {
						sendMessage(new ChatMessage(id, ChatMessage.MessageType.LOGOUT, line));
					}else if(line.equals("shutdown"))
						sendMessage(new ChatMessage(id, ChatMessage.MessageType.SHUTDOWN, line));
					else
						sendMessage(new ChatMessage(id, ChatMessage.MessageType.MESSAGE, line));
				}
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				closeClient(scanner);
			}
		return !carryOn;
	} 

	@Override
	public void sendMessage(ChatMessage msg) {
		try {
			if(output != null && carryOn) {
				output.writeObject(msg);
				output.flush();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Método para cerrar todos los recursos de forma segura
	 */
	private void closeClient(Scanner scanner) {
		try {
			if (input != null || output != null || scanner != null) 
				input.close();

			if (scanner != null)
				scanner.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void disconnect() {
		carryOn = false;
		System.out.println("Pulsa cualquier tecla para continuar...");
	}
	
	public static void main(String[] args) {
		if (args.length == 2) {
			String server = args[0], username = args[1];
			ChatClientImpl client = new ChatClientImpl(server, 1500, username);
			client.start();
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
						
					if(message.getType() != ChatMessage.MessageType.MESSAGE) {
						System.out.println("Usuario desconectado: " + username);
						disconnect();
						break;
					}
				}
			}catch (Exception ex) {
				System.out.println("Conexión con " + username + " cerrada.");
				disconnect();
			}
		}
	}
}
