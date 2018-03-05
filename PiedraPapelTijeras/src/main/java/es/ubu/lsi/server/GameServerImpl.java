/**
 * 
 */
package es.ubu.lsi.server;

import java.io.IOException;
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
				if(idClient % 2 == 0)
					idRoom++;
				idClient++;
				
			}
		} catch (IOException e) {
			System.out.println("Listen :" + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see es.ubu.lsi.server.GameServer#shutdown()
	 */
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see es.ubu.lsi.server.GameServer#broadcastRoom(es.ubu.lsi.common.GameElement)
	 */
	public void broadcastRoom(GameElement element) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see es.ubu.lsi.server.GameServer#remove(int)
	 */
	public void remove(int id) {
		// TODO Auto-generated method stub

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
		
		/**
		 * @param idRoom
		 */
		private ServerThreadForClient(Socket clientSocket, int idClient, int idRoom) {
			this.clientSocket = clientSocket;
			this.idClient = idClient;
			this.idRoom = idRoom;
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
			// TODO Auto-generated method stub
		}
		
	}
	
}
