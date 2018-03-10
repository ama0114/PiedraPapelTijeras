package es.ubu.lsi.client;

import es.ubu.lsi.common.GameElement;

/**
 * Interfaz que define la signatura de los metodos de envio de mensaje, 
 * desconexion y arranque.
 * 
 * @author Antonio de los Mozos Alonso
 * @author Miguel Angel Leon Bardavio
 */
public interface GameClient {
	
	/**
	 * Arranca un hilo para escuchar al servidor.
	 * 
	 * @return true si se ha podido iniciar, false si no se ha podido iniciar
	 */
	public boolean start();
	
	/**
	 * Manda un elemento al servidor.
	 * 
	 * @param element Jugada a mandar al servidor.
	 */
	public void sendElement(GameElement element);
	
	/**
	 * Desconecta al cliente del servidor.
	 */
	public void disconnect();
}
