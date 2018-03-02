package es.ubu.lsi.server;

import es.ubu.lsi.common.GameElement;

/**
 * Define la signatura de los métodos de arranque, multidifusión y eliminación de clientes.
 * 
 * @author Miguel Angel Leon Bardavio
 *
 */
public interface GameServer {
	
	/**
	 * It starts a loop that waits and accepts requests from clients.
	 */
	public void startup();
	
	/**
	 * Closes the server's input/output flow and the corresponding socket to each client.
	 */
	public void shutdown();
	
	/**
	 * Sends the result of the game only to the two clients of a certain room.
	 * 
	 * @param element Element in the game system.
	 */
	public void broadcastRoom(GameElement element);
	
	/**
	 * Removes a client from the list specified by the Client's ID.
	 * 
	 * @param id Id of the client who will be removed.
	 */
	public void remove(int id);
	
}
