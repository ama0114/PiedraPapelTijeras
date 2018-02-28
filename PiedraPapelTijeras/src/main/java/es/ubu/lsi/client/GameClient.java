package es.ubu.lsi.client;

import es.ubu.lsi.common.GameElement;

public interface GameClient {
	public boolean start();
	public void sendElement(GameElement element);
	public void disconnect();
}
