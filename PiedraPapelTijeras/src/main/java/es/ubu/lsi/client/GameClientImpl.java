package es.ubu.lsi.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

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
	private GameClientListener listener;
	private GameClientImpl impl;
	
	/**
	 * @param server nombre del servidor
	 * @param port puerto de conexion del servidor
	 * @param username nombre de usuario
	 */
	public GameClientImpl(String server, int port, String username){
		this.server = server;
		this.port = port;
		this.username = username;
		listener = new GameClientListener();
	}
	
	/* (non-Javadoc)
	 * @see es.ubu.lsi.client.GameClient#start()
	 */
	public boolean start(){
		try{
		listener.run();
		return true;
		} catch(Exception e){
			return false;
		}
		
	}
	
	/* (non-Javadoc)
	 * @see es.ubu.lsi.client.GameClient#sendElement(es.ubu.lsi.common.GameElement)
	 */
	public void sendElement(GameElement element){
		
	}
	
	/* (non-Javadoc)
	 * @see es.ubu.lsi.client.GameClient#disconnect()
	 */
	public void disconnect(){

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args){

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
			 try {
		            Socket s = new Socket(server, port);		            
		            ObjectInputStream in = new ObjectInputStream(s.getInputStream());
					System.out.println(in.readObject());
						
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		           
		}
	}
	
}
