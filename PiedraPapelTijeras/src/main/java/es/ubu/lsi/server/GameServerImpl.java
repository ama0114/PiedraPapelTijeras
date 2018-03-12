package es.ubu.lsi.server;

import java.io.*;
import java.net.*;
import java.util.HashMap;

import es.ubu.lsi.common.*;

/**
 * Implementa la interfaz GameServer.
 * 
 * @author Miguel Angel Leon
 * @see GameServer
 */
public class GameServerImpl implements GameServer {

	/**
	 * Puerto al que se asocia el conector o socket.
	 */
	private int port;

	/**
	 * Socket del servidor.
	 */
	private ServerSocket serverSocket;

	/**
	 * Estado del servidor: true-conectado/encendido, false en caso contrario.
	 */
	private Boolean serverRunStatus;

	/**
	 * Mapa que asocia el hilo asociado al cliente con el ID del cliente.
	 */
	private HashMap<Integer, ServerThreadForClient> clientList = new HashMap<Integer, ServerThreadForClient>();

	/**
	 * Lista de salas.
	 * <p>
	 * Ayuda en la funcion broadcastRoom.
	 * 
	 * @see GameServerImpl#broadcastRoom(GameElement)
	 */
	private HashMap<Integer, GameElement> roomList = new HashMap<Integer, GameElement>();
	
	/**
	 * Especifica el puerto al que se asociara el socket del servidor.
	 * 
	 * @param port Por ejemplo: '1500'.
	 * @author Miguel Angel Leon Bardavio
	 */
	public GameServerImpl(int port) {
		this.port = port;
	}

	/* (non-Javadoc)
	 * @see es.ubu.lsi.server.GameServer#startup()
	 */
	public void startup() {
		try {
			this.serverSocket = new ServerSocket(this.port);
			this.serverRunStatus = true;
			int idClient = 1;
			int idRoom = 1;
			while(serverRunStatus){
				Socket clientSocket = serverSocket.accept();
				ServerThreadForClient clientThread = new ServerThreadForClient(clientSocket, idClient, idRoom);
				if (clientThread.idClient != 0) {
					this.clientList.put(idClient, clientThread);
					this.roomList.put(idRoom, null);
					if(idClient % 2 == 0)
						idRoom++;
					idClient++;
				}
			}
		} catch (IOException e) {
			System.err.println("STARTUP IO EXCEPTION:" + e.getMessage());
			System.exit(1);
		}
	}

	/* (non-Javadoc)
	 * @see es.ubu.lsi.server.GameServer#shutdown()
	 */
	public void shutdown() {
		this.serverRunStatus = false;
		try {
			for (ServerThreadForClient clientThread : this.clientList.values()) {
				clientThread.runStatus = false;
				clientThread.clientSocket.close();
			}
			this.serverSocket.close();
		} catch (IOException e) {
			System.err.println("SHUTDOWN IO EXCEPTION:" + e.getMessage());
		}
	}
	
	/**
	 * Envia los resultados a los clientes especificados por sus hilos.
	 * 
	 * @param clientThread Cliente1.
	 * @param oponentThread Cliente2.
	 * @param gameClient Resultado del cliente1.
	 * @param gameOponent Resultado del cliente2.
	 * @throws IOException en caso de fallo del flujo de datos entre cliente y servidor.
	 * @author Antonio de los Mozos Alonso
	 */
	private void sendResult(ServerThreadForClient clientThread, ServerThreadForClient oponentThread, GameResult gameClient, GameResult gameOponent) throws IOException{
		clientThread.out.writeObject(gameClient);
		oponentThread.out.writeObject(gameOponent);
	}
	
	/**
	 * Obtiene el resultado de una confrontacion y lo envia a los jugadores de una sala mediante la llamada
	 * a sendResult.
	 * 
	 * @param clientElement Movimiento del Cliente1.
	 * @param oponentElement Movimiento del CLiente2.
	 * @param clientThread ServerThread del Cliente1.
	 * @param oponentThread ServerThread del Cliente2.
	 * @throws IOException en caso de fallo del flujo de datos entre cliente y servidor.
	 * @author Antonio de los Mozos Alonso
	 * @see ServerThreadForClient
	 * @see GameServerImpl#sendResult(ServerThreadForClient, ServerThreadForClient, GameResult, GameResult)
	 */
	private void getResult(GameElement clientElement, GameElement oponentElement, ServerThreadForClient clientThread, 
							ServerThreadForClient oponentThread) throws IOException{
		if(clientElement.getElement() == ElementType.PAPEL){
			if(oponentElement.getElement() == ElementType.PIEDRA){
				sendResult(clientThread,oponentThread,GameResult.WIN, GameResult.LOSE);
			}else if(oponentElement.getElement() == ElementType.TIJERA){
				sendResult(clientThread,oponentThread,GameResult.LOSE, GameResult.WIN);
			}
		}
		else if(clientElement.getElement() == ElementType.PIEDRA){
			if(oponentElement.getElement() == ElementType.PAPEL){
				sendResult(clientThread,oponentThread,GameResult.LOSE, GameResult.WIN);
			}else if(oponentElement.getElement() == ElementType.TIJERA){
				sendResult(clientThread,oponentThread,GameResult.WIN, GameResult.LOSE);
			}
		}
		else if(clientElement.getElement() == ElementType.TIJERA){
			if(oponentElement.getElement() == ElementType.PIEDRA){
				sendResult(clientThread,oponentThread,GameResult.LOSE, GameResult.WIN);
			}else if(oponentElement.getElement() == ElementType.PAPEL){
				sendResult(clientThread,oponentThread,GameResult.WIN, GameResult.LOSE);
			}
		}
		if(clientElement.getElement() == oponentElement.getElement()){
			sendResult(clientThread,oponentThread,GameResult.DRAW, GameResult.DRAW);
		}
			
	}

	/* (non-Javadoc)
	 * @see es.ubu.lsi.server.GameServer#broadcastRoom(es.ubu.lsi.common.GameElement)
	 */
	public void broadcastRoom(GameElement element) {
		try {
			//Obtener idClient de element
			ServerThreadForClient clientThread = this.clientList.get(element.getClientId());
			GameElement oponentElement = this.roomList.remove(clientThread.idRoom);
			//Buscar oponente. ¿Ha respondido?
			if (oponentElement != null){
				ServerThreadForClient oponentThread = this.clientList.get(oponentElement.getClientId());
				getResult(element, oponentElement, clientThread, oponentThread);
			}else {
				//No: Almacenar respuesta y enviar WAIT
				this.roomList.put(clientThread.idRoom, element);
				clientThread.out.writeObject(GameResult.WAITING);
			}
		} catch (IOException e) {
			System.err.println("BROADCAST IO EXCEPTION:"+e.getMessage());
		} catch (NullPointerException e) {
			System.err.println("BROADCAST NULLPointer exception-client doesn't exists:"+e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see es.ubu.lsi.server.GameServer#remove(int)
	 */
	public void remove(int id) {
		this.roomList.remove(this.clientList.remove(id).idRoom);
	}
	
	/**
	 * Instancia el servidor en el puerto 1500 y lo arranca mediante startup().
	 * 
	 * @param args No espera arumentos.
	 * @author Miguel Angel Leon Bardavio
	 * @see GameServerImpl#startup()
	 * @see GameServerImpl#GameServerImpl(int)
	 */
	public static void main(String[] args) {
		GameServerImpl server = new GameServerImpl(1500);
		server.startup();
	}

	/**
	 * Hilo de escucha e interaccion con el cliente.
	 * 
	 * @author Miguel Angel Leon
	 *
	 */
	public class ServerThreadForClient extends Thread {

		/**
		 * ID del cliente.
		 */
		private int idClient;
		
		/**
		 * Nickname del jugador.
		 */
		private String username;
		
		/**
		 * Sala asociada al jugador.
		 */
		private int idRoom;
		
		/**
		 * Conector del jugador
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
		 * Estado del hilo del cliente, true:escuchando.
		 */
		private Boolean runStatus;

		
		/**
		 * Inicializa los atributos, abre los flujos de entrada/salida y recibe el nickname del cliente y
		 * envia el id asignado al cliente.
		 * 
		 * @param clientSocket Conector aceptado por el servidor.
		 * @param idClient Id del cliente.
		 * @param idRoom Id de la sala asociada al cliente.
		 * @author Miguel Angel Leon Bardavio
		 */
		private ServerThreadForClient(Socket clientSocket, int idClient, int idRoom) {
			try {
				//Inicializar atributos
				this.idClient = idClient;
				this.idRoom = idRoom;
				this.clientSocket = clientSocket;
				//Crear flujos de entrada/salida
				this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
				this.in = new ObjectInputStream(this.clientSocket.getInputStream());
				//Recibir username y enviar clientId
				this.username = this.in.readObject().toString();
				for (ServerThreadForClient clientThread : clientList.values()) {
					if (clientThread.username == this.username) {
						this.idClient = 0;
						break;
					}
				}
				this.out.writeObject(this.idClient);
				if (this.idClient != 0) {
					this.start();		
				}
			} catch (IOException e) {
				System.err.println("ServerThreadForClient:"+e.getMessage());
			} catch (ClassNotFoundException e) {
				System.err.println("ServerThreadForClient:"+e.getMessage());
			}
		}

		/**
		 * Devuelve elID de la sala asociada al cliente.
		 * 
		 * @return Room ID.
		 * @author Miguel Angel Leon Bardavio
		 */
		public int getIdRoom() {
			return idRoom;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			this.runStatus = true;
			try {
				while(this.runStatus){
					GameElement gameElement = (GameElement) in.readObject();
					switch (gameElement.getElement()) {
					case LOGOUT:
						remove(this.idClient);
						this.clientSocket.close();
						this.runStatus = false;
						break;
					case SHUTDOWN:
						shutdown();
						break;
					default:
						broadcastRoom(gameElement);
						break;
					}
				}
			} catch (IOException e) {
				System.err.println("ServerThreadForClient:"+e.getMessage());
				remove(this.idClient);
				this.runStatus = false;
			} catch (ClassNotFoundException e) {
				System.err.println("ServerThreadForClient:"+e.getMessage());
			}
		}
	}
}
