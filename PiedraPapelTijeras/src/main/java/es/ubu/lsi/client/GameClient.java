package es.ubu.lsi.client;

import es.ubu.lsi.common.GameElement;

/**
 * Interfaz que define la signatura de los métodos de envío de mensaje, 
 * desconexion y arranque.
 * @author Antonio de los Mozos Alonso
 * @author Miguel Angel Leon Bardavio
 */
public interface GameClient {
	
	/**
	 * Inicia un cliente
	 * @return true si se ha podido iniciar, false si no se ha podido iniciar
	 */
	public boolean start();
	
	/**
	 * Manda un elemento al servidor
	 * @param element elemento del tipo GameElement
	 */
	public void sendElement(GameElement element);
	
	/**
	 * Desconecta al cliente del servidor
	 */
	public void disconnect();
}
