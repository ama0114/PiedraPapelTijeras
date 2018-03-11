/**
 * 
 */
package es.ubu.lsi.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.HashMap;
import es.ubu.lsi.common.*;

/**
 * @author Miguel Angel Leon
 *
 */
public class GameServerImpl implements GameServer {

	private int port;

	private ServerSocket serverSocket;

	private Boolean serverRunStatus;

	private ExecutorService threadExecutor = Executors.newCachedThreadPool();

	private HashMap<Integer, ServerThreadForClient> clientList = new HashMap<Integer, ServerThreadForClient>();

	private HashMap<Integer, GameElement> roomList = new HashMap<Integer, GameElement>();
	
	/**
	 * @param port
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
				Socket clientSocket = serverSocket.accept();// TODO userName
				ServerThreadForClient clientThread = new ServerThreadForClient(clientSocket, idClient, idRoom);
				this.clientList.put(idClient, clientThread);
				this.roomList.put(idRoom, null);
				if(idClient % 2 == 0)
					idRoom++;
				idClient++;
				threadExecutor.execute(clientThread);
			}
		} catch (IOException e) {
			System.out.println("Listen :" + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see es.ubu.lsi.server.GameServer#shutdown()
	 */
	public void shutdown() {
		try {
			for (ServerThreadForClient clientThread : this.clientList.values()) {
				clientThread.runStatus = false;
				clientThread.clientSocket.close();
			}
			this.threadExecutor.shutdown();
			this.serverRunStatus = false;
			this.serverSocket.close();
		} catch (IOException e) {
			System.out.println("Listen :" + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see es.ubu.lsi.server.GameServer#broadcastRoom(es.ubu.lsi.common.GameElement)
	 */
	public void broadcastRoom(GameElement element) {
		try {
			//Obtener idClient de element
			ServerThreadForClient clientThread = this.clientList.get(element.getClientId());
			GameElement oponentElement = this.roomList.get(clientThread.idRoom);
			//Buscar oponente. ¿Ha respondido?
			if (oponentElement != null){// TODO Refactorizar
				ServerThreadForClient oponentThread = this.clientList.get(oponentElement.getClientId());
				//Si: Determinar resultado de la partida, enviar resultados a ambos, y eliminar respuesta.
				if (element.getElement() == ElementType.PAPEL && oponentElement.getElement() == ElementType.PIEDRA) {
					clientThread.out.writeObject(GameResult.WIN);
					oponentThread.out.writeObject(GameResult.LOSE);
					oponentThread.notify();
				}else if (element.getElement() == ElementType.PIEDRA && oponentElement.getElement() == ElementType.TIJERA) {
					clientThread.out.writeObject(GameResult.WIN);
					oponentThread.out.writeObject(GameResult.LOSE);
				}else if (element.getElement() == ElementType.TIJERA && oponentElement.getElement() == ElementType.PAPEL) {
					clientThread.out.writeObject(GameResult.WIN);
					oponentThread.out.writeObject(GameResult.LOSE);
				}else if (element.getElement() == oponentElement.getElement()) {
					clientThread.out.writeObject(GameResult.DRAW);
					oponentThread.out.writeObject(GameResult.DRAW);
				}
			}else {
				//No: Almacenar respuesta y enviar WAIT
				this.roomList.put(clientThread.idRoom, element);
				clientThread.out.writeObject(GameResult.WAITING);
				clientThread.wait();
			}
		} catch (IOException e) {
			System.out.println("Broadcast IO exception:"+e.getMessage());
		} catch (NullPointerException e) {
			System.out.println("Broadcast NULLPointer exception, client doesn't exists:"+e.getMessage());
		} catch (InterruptedException e) {
			System.out.println("Broadcast Interrupted exception::"+e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see es.ubu.lsi.server.GameServer#remove(int)
	 */
	public void remove(int id) {
		this.roomList.remove(this.clientList.remove(id).idRoom);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GameServerImpl server = new GameServerImpl(1500);
		server.startup();
	}

	/**
	 * @author Miguel Angel Leon
	 *
	 */
	public class ServerThreadForClient extends Thread {

		private int idClient;
		
		private int idRoom;
		
		private Socket clientSocket;

		private ObjectInputStream in;

		private ObjectOutputStream out;

		private Boolean runStatus;

		/**
		 * @param idRoom
		 */
		private ServerThreadForClient(Socket clientSocket, int idClient, int idRoom) {
			try {
				this.idClient = idClient;
				this.idRoom = idRoom;
				this.clientSocket = clientSocket;
				this.in = new ObjectInputStream(this.clientSocket.getInputStream());
				this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
				this.out.writeInt(this.idClient);// Enviar idClient al cliente
			} catch (IOException e) {
				System.out.println("ServerThreadForClient:"+e.getMessage());
			}
		}

		/**
		 * @return Room ID.
		 */
		public int getIdRoom() {
			return idRoom;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				this.runStatus = true;
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
				System.out.println("ServerThreadForClient:"+e.getMessage());
			} catch (ClassNotFoundException e) {
				System.out.println("ServerThreadForClient:"+e.getMessage());
			}
		}
	}
}
