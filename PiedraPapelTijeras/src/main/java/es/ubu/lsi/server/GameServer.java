package es.ubu.lsi.server;

import es.ubu.lsi.common.GameElement;

/**
 * Define la signatura de los métodos de arranque, multidifusión y eliminación de clientes.
 * 
 * @author Antonio de los Mozos Alonso
 * @author Miguel Angel Leon Bardavio
 *
 */
public interface GameServer {
	
	/**
	 * Inicia un bucle de espera que acepta peticiones de los clientes.
	 */
	public void startup();
	
	/**
	 * Cierra el flujo de entrada/salida del servidor y el correspondiente socket.
	 */
	public void shutdown();
	
	/**
	 * Envia el resultado del juego de una sala, a los dos clientes de esa sala.
	 * 
	 * @param element Element in the game system.
	 */
	public void broadcastRoom(GameElement element);
	
	/**
	 * Elimina un cliente especificado por su id, de la lista de clientes.
	 * 
	 * @param id Id of the client who will be removed.
	 */
	public void remove(int id);
	
}
