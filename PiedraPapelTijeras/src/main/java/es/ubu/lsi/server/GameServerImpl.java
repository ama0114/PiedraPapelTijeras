package es.ubu.lsi.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import es.ubu.lsi.common.GameElement;
import es.ubu.lsi.common.GameElement.ElementType;
import es.ubu.lsi.common.GameResult;

public class GameServerImpl implements GameServer {

	/**
	 * Port where is executed the server.
	 */
	private int port;

	/**
	 * List of clients.
	 */
	private HashMap<Integer, ServerThreadForClient[]> clientRecord = new HashMap<Integer, ServerThreadForClient[]>();

	private ExecutorService executor = Executors.newCachedThreadPool();
	
	/**
	 * Constructor of the class that assign the port passed as argument as port
	 * where is executed the server.
	 * 
	 * @param port
	 *            Port where is executed the server.
	 */
	public GameServerImpl(int port) {
		this.port = port;
	}

	/**
	 * Constructor of the class that assign the port 1500 as port where is
	 * executed the server.
	 */
	public GameServerImpl() {
		this.port = 1500;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.ubu.lsi.server.GameServer#startup()
	 */
	public void startup() {

		try {

			ServerSocket serverSocket = new ServerSocket(this.port);
			Integer roomNumber = 0;

			while (true) {
				Socket clientSocket = serverSocket.accept();
				ServerThreadForClient thread = new ServerThreadForClient(roomNumber++, clientSocket);
				// TODO clientRecord.put(arg0, arg1);
			}
		} catch (IOException e) {
			System.out.println("Listen :" + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.ubu.lsi.server.GameServer#shutdown()
	 */
	public void shutdown() {
		this.executor.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * es.ubu.lsi.server.GameServer#broadcastRoom(es.ubu.lsi.common.GameElement)
	 */
	public void broadcastRoom(GameElement element) {
		// TODO
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see es.ubu.lsi.server.GameServer#remove(int)
	 */
	public void remove(int id) {
		this.clientRecord.remove(id);
	}

	/**
	 * Starts the main thread of the server.
	 * 
	 * @param args
	 *            Not expected to have arguments.
	 */
	public static void main(String[] args) {
		GameServerImpl gameServerImpl = new GameServerImpl();
		gameServerImpl.startup();
	}

	/**
	 * The method expects messages from the client and responds with the
	 * corresponding operation.
	 * 
	 * @author Miguel Angel Leon Bardavio
	 *
	 */
	private class ServerThreadForClient extends Thread {

		/**
		 * A identifier for the room.
		 */
		private int idRoom;

		private ObjectInputStream in;

		private ObjectOutputStream out;

		private Socket clientSocket;

		public ServerThreadForClient(int idRoom, Socket clientSocket) {

			try {
				this.idRoom = idRoom;
				this.clientSocket = clientSocket;
				this.in = new ObjectInputStream(this.clientSocket.getInputStream());
				this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
				//this.start();
			} catch (IOException e) {
				System.out.println("Connection:" + e.getMessage());
			}
		}

		/**
		 * Gets ID of the room.
		 * 
		 * @return ID of the room.
		 */
		public int getIdRoom() {
			return idRoom;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			Boolean exitCondition = false;

			try {
				while (!exitCondition) {
					GameElement gameElement = (GameElement) in.readObject();

					switch (gameElement.getElement()) {

					case LOGOUT:
						remove(gameElement.getId());
						exitCondition = true;
						break;
					case SHUTDOWN:
						shutdown();
						exitCondition = true;// TODO quizas no sea necesario
						break;
					default:
						broadcastRoom(gameElement);
						break;
					}

				}
			} /*
				 * catch ( e) { // TODO Cast e.printStackTrace(); }
				 */catch (IOException e) {
				System.out.println("Connection:" + e.getMessage());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
