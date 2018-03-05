/**
 * 
 */
package es.ubu.lsi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.ubu.lsi.common.GameElement;

/**
 * @author Miguel Angel Leon
 *
 */
public class GameServerImpl implements GameServer {

	private int port;

	private ServerSocket serverSocket;

	private Boolean serverStatus;

	private ExecutorService threadExecutor = Executors.newCachedThreadPool();

	private HashMap<Integer, ServerThreadForClient> clientList = new HashMap<Integer, ServerThreadForClient>();

	/**
	 * @param port
	 */
	public GameServerImpl(int port) {
		this.port = port;
	}

	public GameServerImpl() {
		this.port = 1500;
	}

	/* (non-Javadoc)
	 * @see es.ubu.lsi.server.GameServer#startup()
	 */
	public void startup() {
		try {
			this.serverSocket = new ServerSocket(this.port);
			this.serverStatus = true;
			int idClient = 1;
			int idRoom = 1;
			while(serverStatus){
				Socket clientSocket = serverSocket.accept();
				ServerThreadForClient clientThread = new ServerThreadForClient(clientSocket, idClient, idRoom);
				this.clientList.put(idClient, clientThread);
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
				clientThread.in.close();
				clientThread.out.close();
				clientThread.clientSocket.close();
				clientThread.runStatus = false;
			}
			this.threadExecutor.shutdown();
		} catch (IOException e) {
			System.out.println("Listen :" + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see es.ubu.lsi.server.GameServer#broadcastRoom(es.ubu.lsi.common.GameElement)
	 */
	public void broadcastRoom(GameElement element) {
		int clientId = element.getId();
		int roomId = this.clientList.get(clientId).getIdRoom();
		for (ServerThreadForClient clientThread : this.clientList.values()) {
			if (clientThread.getIdRoom() == roomId) {
				// TODO
			}
		}
	}

	/* (non-Javadoc)
	 * @see es.ubu.lsi.server.GameServer#remove(int)
	 */
	public void remove(int id) {
		try {
			ServerThreadForClient clientThread = this.clientList.remove(id);
			clientThread.in.close();
			clientThread.out.close();
			clientThread.clientSocket.close();
			clientThread.runStatus = false;
		} catch (IOException e) {
			System.out.println("Listen :" + e.getMessage());
		} catch (NullPointerException e){
			System.out.println("Error al borrar el cliente: 'El cliente no existe'.");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GameServerImpl server = new GameServerImpl();
		server.startup();
	}

	/**
	 * @author Miguel Angel Leon
	 *
	 */
	public class ServerThreadForClient extends Thread {

		private Socket clientSocket;

		private int idClient;

		private int idRoom;

		private ObjectInputStream in;

		private ObjectOutputStream out;

		private Boolean runStatus;

		/**
		 * @param idRoom
		 */
		private ServerThreadForClient(Socket clientSocket, int idClient, int idRoom) {
			try {
				this.clientSocket = clientSocket;
				this.idClient = idClient;
				this.idRoom = idRoom;
				this.in = new ObjectInputStream(this.clientSocket.getInputStream());
				this.out = new ObjectOutputStream(out);
				this.runStatus = true;
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
				while(this.runStatus){
					GameElement gameElement = (GameElement) in.readObject();
					switch (gameElement.getElement()) {
					case LOGOUT:
						remove(this.idClient);
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
