package es.ubu.lsi.client;

import java.io.*;
import java.net.Socket;
import es.ubu.lsi.common.*;

/**
 * Implementa la interfaz GameClient.
 * 
 * @author Antonio de los Mozos Alonso
 * @author Miguel Angel Leon Bardavio
 *
 */
public class GameClientImpl implements GameClient {
	
	/**
	 * Nombre o direccion del host.
	 */
	private String server;
	
	/**
	 * Puerto de conexion al servidor.
	 */
	private int port;
	
	/**
	 * Nickname del jugador.
	 */
	private String username;
	
	/**
	 * ID con el que nos identifica el servidor.
	 */
	private int clientId;
	
	/**
	 * Conector.
	 */
	private Socket clientSocket;
	
	/**
	 * Flujo de salida.
	 */
	private ObjectOutputStream out;
	
	/**
	 * Flujo de entrada.
	 */
	private ObjectInputStream in;
			
	/**
	 * Hilo de escucha al servidor.
	 */
	private GameClientListener listener;
	
	/**
	 * Inicializa los atributos pasados como argumentos.
	 * 
	 * @param server Nombre del servidor.
	 * @param port Puerto de conexion del servidor.
	 * @param username Nombre de usuario.
	 * @author Antonio de los Mozos Alonso
	 */
	public GameClientImpl(String server, int port, String username){
			this.server = server;
			this.port = port;
			this.username = username;
	}
	
	/* (non-Javadoc)
	 * @see es.ubu.lsi.client.GameClient#start()
	 */
	public boolean start(){
		try {
			this.clientSocket = new Socket(this.server, this.port);
			this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
			this.in = new ObjectInputStream(this.clientSocket.getInputStream());
			this.out.writeObject(this.username);
			this.clientId = (Integer) this.in.readObject();
			if (this.clientId == 0) {
				return false;
			}else{
				Thread listenerThread = new Thread(this.listener = new GameClientListener());
				listenerThread.start();
				return true;
			}
		} catch (Exception e) {
			System.out.println("START EXCEPTION:"+e.getMessage());
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see es.ubu.lsi.client.GameClient#sendElement(es.ubu.lsi.common.GameElement)
	 */
	public void sendElement(GameElement element){
		try {
			this.out.writeObject(element);
		} catch (IOException e) {
			System.out.println("SEND ELEMENT IO EXCEPTION:" + e.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see es.ubu.lsi.client.GameClient#disconnect()
	 */
	public void disconnect(){
		try {
			this.listener.listenerRunStatus = false;
			this.clientSocket.close();
		} catch (IOException e) {
			System.out.println("DISCONNECT IO EXCEPTION:" + e.getMessage());
		}
		
	}
	
	/**
	 * Hilo principal del cliente. 
	 * <p>
	 * Instancia al cliente y un hilo de escucha al servidor. Despues espera entrada por consola por parte del
	 * usuario para el envio de la jugada al servidor.
	 * <p>
	 * El puerto de conexion es siempre el 1500. El servidor por defecto es localhost.
	 * <p>
	 * Ejemplo de llamada: 'java es.ubu.lsi.client.GameClientImpl 10.168.168.13 nickname'.
	 * 
	 * @param args Recibe una direccion IP/Nombre de la máquina y un nickname como argumentos.
	 * @author Miguel Angel Leon Bardavio
	 */
	public static void main(String[] args){
		GameClientImpl client = new GameClientImpl(args[0], 1500, args[1]);
		try {
			if(client.start()){
				BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in));
				String userInput;
				while ((userInput = stdIn.readLine().toUpperCase()) != null) {
					if (userInput.equals("PIEDRA") || userInput.equals("PAPEL") || userInput.equals("TIJERA")) {
						client.sendElement(new GameElement(client.clientId, ElementType.valueOf(userInput)));
					}else if (userInput.equals("LOGOUT") || userInput.equals("SHUTDOWN")) {
						client.sendElement(new GameElement(client.clientId, ElementType.valueOf(userInput)));
						client.disconnect();
						break;
					}else {
						System.out.println("INVALID COMMAND");
					}
				}
			}
		} catch (IOException e) {
			System.out.println("MAIN IO EXCEPTION:" + e.getMessage());
		}
	}
	
	/**
	 * Hilo de escucha al servidor y muestra los mensajes que recibe del servidor por pantalla.
	 * 
	 * @author Antonio de los Mozos Alonso
	 */
	private class GameClientListener implements Runnable{	
		
		/**
		 * Estado del listener, true:escuchando, false en caso contrario.
		 */
		private Boolean listenerRunStatus;
		
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run(){
			this.listenerRunStatus = true;
				try {
					while (listenerRunStatus) {
						String result = in.readObject().toString();
						System.out.println(result);
					}
				} catch (IOException e) {
					System.out.println("LISTENER IO EXCEPTION:" + e.getMessage());
				} catch (ClassNotFoundException e) {
					System.out.println("LISTENER CAST EXCEPTION:" + e.getMessage());
				}
		}
	}
}
