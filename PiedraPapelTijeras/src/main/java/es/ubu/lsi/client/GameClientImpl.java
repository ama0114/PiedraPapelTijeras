package es.ubu.lsi.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.ubu.lsi.common.GameElement;

/**
 * Clase que implementa la interfaz GameClient 
 * y que genera un cliente de juego.
 * @author Antonio de los Mozos Alonso
 * @author Miguel Angel Leon Bardavio
 *
 */
public class GameClientImpl implements GameClient {
	
	private String server;
	
	private int port;
	
	private String username;
	
	private Socket clientSocket;
	
	private ObjectInputStream in;
	
	private ObjectOutputStream out;
		
	private GameClientListener listener;
	
	private ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
	
	/**
	 * @param server nombre del servidor
	 * @param port puerto de conexion del servidor
	 * @param username nombre de usuario
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
		Boolean retorno = false;
		try {
			this.clientSocket = new Socket(server, port);
			this.listener = new GameClientListener();
			this.threadExecutor.execute(this.listener);
			retorno = true;
		} catch (UnknownHostException e) {
			System.out.println("Sock:"+e.getMessage());
		} catch (IOException e) {
			System.out.println("IO:" + e.getMessage());
		}
		return retorno;
		
	}
	
	/* (non-Javadoc)
	 * @see es.ubu.lsi.client.GameClient#sendElement(es.ubu.lsi.common.GameElement)
	 */
	public void sendElement(GameElement element){
		try {
			this.out.writeObject(element);
		} catch (IOException e) {
			System.out.println("IO:" + e.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see es.ubu.lsi.client.GameClient#disconnect()
	 */
	public void disconnect(){
		this.threadExecutor.shutdown();
		try {
			this.in.close();
			this.out.close();
			this.clientSocket.close();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("IO:" + e.getMessage());
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args){
		GameClientImpl client = new GameClientImpl(args[0], 1500, args[1]);
		client.start();
	}
	
	/**
	 * Clase que implementa la interfaz runnable y nos permite 
	 * generar un hilo de conexión al servidor
	 * @author Antonio de los Mozos Alonso
	 * @author Miguel Angel Leon Bardavio
	 */
	private class GameClientListener implements Runnable{
		
		/**
		 * Metodo para ejecutar un hilo de escucha de mensajes al servidor
		 * y mostrar los mensajes entrantes
		 */
		public void run(){
			 		           
		}
	}
	
}
