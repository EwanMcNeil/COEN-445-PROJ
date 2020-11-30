import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
	Vector<ClientHandler> clientHandlers = new Vector<>();
	Vector<Semaphore> messageFlags = new Vector<>();
	InetAddress Address;
	int port;
	int clientCount;
	InetAddress Server2;
	int Port2;
	static boolean isServing = false;
	DatagramSocket socket;
	static boolean isStarting = true;

	static boolean goodToChange = false;
	static boolean noSignOfA = true;

	public Server(String name, int port, boolean isServing, InetAddress IP, int port2) throws SocketException {
		try {
			Address = InetAddress.getByName(name);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.port = port;
		this.isServing = isServing;
		Server2 = IP;
		Port2 = port2;
	}

	// Main function starts up server
	public static void main(String[] args) {
		if (args.length < 5) {
			System.out.println("Missing Input");
			return;
		}
		boolean is_serving = false;

		if (Integer.parseInt(args[0]) == 1)
			is_serving = true;
		else
			is_serving = false;

		String serverIP = args[1];

		int port = Integer.parseInt(args[2]);

		String Server2_name = args[3];
		int port2 = Integer.parseInt(args[4]);

		try {
			Server server = new Server(serverIP, port, is_serving, InetAddress.getByName(Server2_name), port2);

			System.out.println("Server listening on port " + port);
			System.out.println("Serving: " + Boolean.valueOf(isServing));

			server.service();

		} catch (SocketException ex) {
			System.out.println("Socket error: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		}
	}

	public void servingTimer(DatagramSocket socket) {

		long howLong = 1000 * 120; // 1min
		// long howLong = 10000; //10 sec
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				System.out.println("Time to stop serving");
				requestChanging(socket);

			}
		}, howLong);

	}

	public void timeToServeThreadRequest(DatagramSocket socket) {

		long howLong = 1000 * 5; // 5sec
		// long howLong = 10000; //10 sec
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (goodToChange) {
					System.out.println("Changing Server");
					changeServer(socket);
				} else {
					System.out.println("Changing resquest not received, will continue serving.");
					servingTimer(socket);
				}

			}
		}, howLong);

	}

//Timer starting when a server stops serving
	public void notServingTimer(DatagramSocket socket) {

		long howLong = 1000 * 90; // 90 seconds
		System.out.println("Not serving timer on");
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (noSignOfA && !isServing) {
					System.out.println("No sign of serving server, sending request.");
					requestSign(socket);
				}

			}
		}, howLong);

	}

	public void signTimerRequest(DatagramSocket socket) {

		long howLong = 1000 * 5; // 5 seconds
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (noSignOfA) {
					System.out.println("Taking service, no sign of other server.");
					specialOperationTakeService(socket);
				} else {
					System.out.println("Sign received, false alarm.");
					notServingTimer(socket);
					noSignOfA = true;
				}

			}
		}, howLong);

	}

	///
	/// Service is the main loop of the program
	///

	private void service() throws IOException {
		socket = new DatagramSocket(port);
		if (isStarting) {
			initializeClients();
		}

		// Start timer for 5 minutes
		if (isServing) {
			servingTimer(socket);
		} else if (isServing == false) {
			NotServingThread thread = new NotServingThread(this, socket);
			notServingTimer(socket);
			thread.start();
		}

		while (true) {
			DatagramPacket requestPacket = null;

			try {
				byte[] clientMessage = new byte[50000];
				requestPacket = new DatagramPacket(clientMessage, clientMessage.length);

				socket.receive(requestPacket);

				String message = formatMessage(clientMessage).toString();

				String splitMessage[] = message.split(" ");

				System.out.print("Server has received: ");
				System.out.println(message);

				String command = splitMessage[0].toUpperCase().replace("_", "-");

				switch (command) {
				case "REGISTER":
				case "REGISTERED":
					registerClient(socket, splitMessage, requestPacket);
					break;

				case "DE-REGISTER":
					deRegisterClient(socket, splitMessage, requestPacket);
					break;

				case "START-SERVING":
					isServing = true;
					System.out.println("Serving: " + Boolean.valueOf(isServing));
					servingTimer(socket);
					break;

				case "UPDATE-SERVER":
					try {
						if (splitMessage[1].contains("/")) {
							String hostName[] = splitMessage[1].split("/");
							Server2 = InetAddress.getByName(hostName[1]);
						} else {

							Server2 = InetAddress.getByName(splitMessage[1]);
						}

					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Port2 = Integer.parseInt(splitMessage[2]);
					break;

				case "PUBLISH":
					publish(socket, splitMessage, requestPacket);
					break;

				case "READY-TO-CHANGE":
					readyToChange(socket);
					break;
				case "CHANGE-REQUEST-RECEIVED":
					goodToChange = true;
					break;
				case "STILL-THERE":
					yesStillThere(socket);
					break;
				case "YES":
					noSignOfA = false;
					break;

				default:

					otherRequests(socket, splitMessage, requestPacket);
					break;

				}

			} catch (Exception e) {
				// here an exception causes the program to crash if out of bounds
				socket.close();
				e.printStackTrace();
			}
		}
	}

	///
	/// database functions
	///

	public void writeClientsFiles() {
		FileWriter writer;
		String output = "";

		try {
			writer = new FileWriter("clients_files_" + String.valueOf(port) + ".txt");

			for (int j = 0; j < clientHandlers.size(); j++) {
				ArrayList<String> subjects_list = clientHandlers.get(j).subjects;
				String client_subjects = "";

				for (int k = 0; k < subjects_list.size(); k++)
					if (k != subjects_list.size() - 1 || subjects_list.size() == 1)
						client_subjects += subjects_list.get(k) + ",";

					else
						client_subjects += subjects_list.get(k);

				if (subjects_list.size() != 0)
					output += clientHandlers.get(j).name + " " + clientHandlers.get(j).RQ + " " + client_subjects + " "
							+ clientHandlers.get(j).clientAddress + " " + clientHandlers.get(j).clientPort + "\n";

				else
					output += clientHandlers.get(j).name + " " + clientHandlers.get(j).RQ + " "
							+ clientHandlers.get(j).clientAddress + " " + clientHandlers.get(j).clientPort + "\n";
			}

			writer.write(output);

			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void initializeClients() throws IOException {
		String file_name = "clients_files_" + String.valueOf(port) + ".txt";

		File file = new File(file_name);
		BufferedReader reader;

		if (file.exists() && !file.isDirectory()) {
			isStarting = false;

			reader = new BufferedReader(new FileReader(file_name));

			String line = reader.readLine();
			String subjects[] = null;

			while (line != null) {
				Semaphore messageFlag = new Semaphore(0);
				messageFlags.add(messageFlag);

				String splitLine[] = line.split(" ");

				int localRQ = Integer.parseInt(splitLine[1]);
				if (line.contains(",")) {
					subjects = splitLine[2].split(",");
				}

				ClientHandler client;
				ArrayList<String> sub = new ArrayList<>();

				if (splitLine.length > 4) {
					client = new ClientHandler(socket, InetAddress.getByName(splitLine[3].replace("/", "")),
							Integer.parseInt(splitLine[4]), messageFlag, splitLine[0], this,
							new ArrayList<>(Arrays.asList(subjects)), false, localRQ);
				}

				else {
					client = new ClientHandler(socket, InetAddress.getByName(splitLine[2].replace("/", "")),
							Integer.parseInt(splitLine[3]), messageFlag, splitLine[0], this, sub, false, localRQ);
				}

				client.start();

				clientHandlers.add(client);

				line = reader.readLine();
			}

			reader.close();
		}

		else {
			System.out.println("File does not exist");
			return;
		}
	}

	///
	/// Responding to requests functions
	///
	private void registerClient(DatagramSocket socket, String splitMessage[], DatagramPacket packet) {
		String name = splitMessage[2];
		ArrayList<String> subjects = new ArrayList<>();
		ArrayList<String> clients_name = new ArrayList<>();

		for (ClientHandler clientHandler : clientHandlers)
			clients_name.add(clientHandler.name);

		if (!(clients_name.contains(name))) {
			Semaphore messageFlag = new Semaphore(0);

			messageFlags.add(messageFlag);

			ClientHandler t = new ClientHandler(socket, packet.getAddress(), packet.getPort(), messageFlag, name, this,
					subjects, true, 0);

			// Invoking the start() method
			t.start();

			t.newPacket(packet);
			messageFlag.release();

			clientHandlers.add(t);
		}

		else {
			// REGISTER-DENIED RQ# Reason
			String message = "REGISTER-DENIED" + " " + splitMessage[1] + " " + "NAME_IN_USE";
			String message2 = "REGISTER-DENIED" + " " + splitMessage[1] + " " + splitMessage[2] + " " + splitMessage[3]
					+ " " + splitMessage[4];

			byte[] buffer = message.getBytes();
			byte[] buffer2 = message2.getBytes();

			if (isServing) {
				DatagramPacket response = new DatagramPacket(buffer, buffer.length, packet.getAddress(),
						packet.getPort());

				DatagramPacket ServerResponse = new DatagramPacket(buffer2, buffer2.length, Server2, Port2);

				try {
					socket.send(response);
					socket.send(ServerResponse);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void deRegisterClient(DatagramSocket socket, String splitMessage[], DatagramPacket packet) {
		int RQ = 0;
		String name = splitMessage[2];
		int incomingPort = packet.getPort();

		for (int i = 0; i < clientHandlers.size(); i++) {
			if (clientHandlers.get(i).getName().equals(name)) {
				if (clientHandlers.get(i).clientPort == incomingPort || this.Port2 == incomingPort) {
					RQ = clientHandlers.get(i).RQ;
					clientHandlers.get(i).stop();
					clientHandlers.remove(i);
					messageFlags.remove(i);

					String message = "DE-REGISTER" + " " + RQ + " " + name;

					byte[] buffer = message.getBytes();

					if (isServing) {
						DatagramPacket response = new DatagramPacket(buffer, buffer.length, packet.getAddress(),
								packet.getPort());

						DatagramPacket ServerResponse = new DatagramPacket(buffer, buffer.length, Server2, Port2);

						try {
							socket.send(response);
							socket.send(ServerResponse);
						} catch (IOException e) {

						}
					}

					writeClientsFiles();
				} else {

					if (isServing) {

						String message1 = " ";

						message1 = "PORT_ERROR " + splitMessage[1] + " PORT_ERROR";

						byte[] buffer1 = message1.getBytes();

						DatagramPacket response1 = new DatagramPacket(buffer1, buffer1.length, packet.getAddress(),
								packet.getPort());

						try {
							socket.send(response1);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
			}
		}

	}

	private void publish(DatagramSocket socket, String splitMessage[], DatagramPacket packet) {

		// first check name of user and subject and make sure subject is in list for the
		// user

		// if so send message to all other clients that have the same subject

		Boolean subjectCheck = false;
		Boolean portCheck = false;
		String subject = splitMessage[3].toUpperCase();
		String name = splitMessage[2];

		// this is checking for the sending server is in the register for subject
		for (int i = 0; i < clientHandlers.size(); i++) {
			if (clientHandlers.get(i).name.equals(name)) {

				if (clientHandlers.get(i).subjects.contains(subject)) {
					subjectCheck = true;
				}
				if (clientHandlers.get(i).clientPort == packet.getPort()) {
					portCheck = true;
				}
			}
		}

		int count = 4;
		String messageRec = " ";
		while (count < splitMessage.length) {
			messageRec = messageRec + " " + splitMessage[count];
			count = count + 1;
		}

		String message = "MESSAGE" + " " + name + " " + subject + " " + messageRec;
		byte[] buffer = message.getBytes();

		// goes through the rest of them
		if (subjectCheck && portCheck) {
			for (int i = 0; i < clientHandlers.size(); i++) {
				if (!(clientHandlers.get(i).name.equals(name))) {

					System.out.print(clientHandlers.get(i).name);
					if (clientHandlers.get(i).subjects.contains(subject)) {

						if (isServing) {
							DatagramPacket response = new DatagramPacket(buffer, buffer.length,
									clientHandlers.get(i).clientAddress, clientHandlers.get(i).clientPort);

							try {
								socket.send(response);

							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}

		}

		else {
			if (isServing) {

				String message1 = " ";

				if (!portCheck) {
					message1 = "PUBLISH-DENIED " + splitMessage[1] + " PORT_ERROR";

				} else {
					message1 = "PUBLISH-DENIED " + splitMessage[1] + " You are not registered in this topic!";
				}

				byte[] buffer1 = message1.getBytes();

				DatagramPacket response1 = new DatagramPacket(buffer1, buffer1.length, packet.getAddress(),
						packet.getPort());

				try {
					socket.send(response1);
				} catch (IOException e) {

				}
			}
		}
	}

	//
	// Other requests passes client requests to client handlers
	//
	private void otherRequests(DatagramSocket socket, String splitMessage[], DatagramPacket packet) throws IOException {
		String name = splitMessage[2];

		ArrayList<String> clients_name = new ArrayList<>();
		int incomingPort = packet.getPort();

		for (ClientHandler clientHandler : clientHandlers)
			clients_name.add(clientHandler.name);

		if (clients_name.contains(name)) {

			for (int i = 0; i < clientHandlers.size(); i++) {
				if (clientHandlers.get(i).getName().equals(name)) {

					if (splitMessage[0].equals("UPDATE")) {

						clientHandlers.get(i).newPacket(packet);
						messageFlags.get(i).release();
					} else {
						if (clientHandlers.get(i).clientPort == incomingPort || this.Port2 == incomingPort) {

							clientHandlers.get(i).newPacket(packet);
							messageFlags.get(i).release();
						} else {

							if (isServing) {
								String message1 = "PORT-ERROR " + splitMessage[1] + " PORT DOES NOT MATCH UP";

								byte[] buffer1 = message1.getBytes();

								DatagramPacket response1 = new DatagramPacket(buffer1, buffer1.length,
										packet.getAddress(), packet.getPort());

								try {
									socket.send(response1);
								} catch (IOException e) {

								}
							}
						}
					}
				}
			}
		}

		else {

			if (isServing) {

				String message1 = "UPDATE-DENIED " + splitMessage[1] + " NAME_NOT_IN_USE";

				byte[] buffer1 = message1.getBytes();

				DatagramPacket response1 = new DatagramPacket(buffer1, buffer1.length, packet.getAddress(),
						packet.getPort());

				try {
					socket.send(response1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void changeServer(DatagramSocket socket) {
		String message = "CHANGE-SERVER" + " " + Server2 + " " + Port2;
		String message2 = "START-SERVING";

		byte[] buffer = message.getBytes();
		byte[] buffer2 = message2.getBytes();

		for (int i = 0; i < clientHandlers.size(); i++) {

			DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientHandlers.get(i).clientAddress,
					clientHandlers.get(i).clientPort);

			try {
				socket.send(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		DatagramPacket ServerResponse = new DatagramPacket(buffer2, buffer2.length, Server2, Port2);

		try {
			socket.send(ServerResponse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Server.isServing = false;
		System.out.println("Serving: " + Boolean.valueOf(isServing));
		NotServingThread thread = new NotServingThread(this, socket);
		thread.start();
		notServingTimer(socket);
	}

	private void specialOperationTakeService(DatagramSocket socket) {

		String message = "CHANGE-SERVER" + " " + Address + " " + port;
		String message2 = "STOP-SERVING";

		byte[] buffer = message.getBytes();
		byte[] buffer2 = message2.getBytes();

		for (int i = 0; i < clientHandlers.size(); i++) {

			DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientHandlers.get(i).clientAddress,
					clientHandlers.get(i).clientPort);

			try {
				socket.send(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		DatagramPacket ServerResponse = new DatagramPacket(buffer2, buffer2.length, Server2, Port2);

		try {
			socket.send(ServerResponse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Server.isServing = true;
		System.out.println("Serving: " + Boolean.valueOf(isServing));
		servingTimer(socket);

	}

	public void updateServer(DatagramSocket socket, String[] input) {

		System.out.println("New IP address: " + input[1] + " New port: " + input[2]);

		try {
			if (input[1].contains("/")) {
				String hostName[] = input[1].split("/");
				Address = InetAddress.getByName(hostName[1]);
			} else {
				Address = InetAddress.getByName(input[1]);
			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		port = Integer.parseInt(input[2]);

		writeClientsFiles();

		String message2 = "UPDATE-SERVER" + " " + input[1] + " " + input[2];

		byte[] buffer2 = message2.getBytes();

		DatagramPacket ServerResponse = new DatagramPacket(buffer2, buffer2.length, Server2, Port2);

		try {
			socket.send(ServerResponse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.service();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void requestChanging(DatagramSocket socket) {

		String message2 = "READY-TO-CHANGE";

		byte[] buffer2 = message2.getBytes();

		DatagramPacket ServerResponse = new DatagramPacket(buffer2, buffer2.length, Server2, Port2);

		try {
			socket.send(ServerResponse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Request to change sent to: IP: " + Server2 + " Port: " + Port2);
		goodToChange = false;
		timeToServeThreadRequest(socket);
	}

	public void yesStillThere(DatagramSocket socket) {

		String message2 = "YES";

		byte[] buffer2 = message2.getBytes();

		DatagramPacket ServerResponse = new DatagramPacket(buffer2, buffer2.length, Server2, Port2);

		try {
			socket.send(ServerResponse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void requestSign(DatagramSocket socket) {

		String message2 = "STILL-THERE";

		byte[] buffer2 = message2.getBytes();

		DatagramPacket ServerResponse = new DatagramPacket(buffer2, buffer2.length, Server2, Port2);

		try {
			socket.send(ServerResponse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Request for a sign sent");
		noSignOfA = true;
		// noSignOfA = false;
		signTimerRequest(socket);
	}

	public void readyToChange(DatagramSocket socket) {

		String message2 = "CHANGE-REQUEST-RECEIVED";

		byte[] buffer2 = message2.getBytes();

		DatagramPacket ServerResponse = new DatagramPacket(buffer2, buffer2.length, Server2, Port2);

		try {
			socket.send(ServerResponse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Function is from the tutorial needed to turn the buffer into readable data
	private static StringBuilder formatMessage(byte[] a) {
		if (a == null)
			return null;

		StringBuilder ret = new StringBuilder();
		int i = 0;
		while (a[i] != 0) {
			ret.append((char) a[i]);
			i++;
		}
		return ret;
	}

}
