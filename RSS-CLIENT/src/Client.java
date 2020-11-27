import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Client {
	int RQ;
	Boolean startUp;
	InetAddress hostName1;
	InetAddress hostName2;
	InetAddress clientIP;
	int clientPort;
	String clientName;
	int Port1;
	int Port2;
	Semaphore printSem;
	ArrayList<String> subjects;
	Semaphore echoSem;
	Semaphore publishSem;
	Semaphore upSubSem;
	Semaphore upClientSem;
	Semaphore regClientSem;
	Semaphore deRegClientSem;
	Semaphore response;
	boolean registered;
	boolean updated;
	boolean deRegistered;
	boolean subjectsBool;
	boolean clientUpdate;
	boolean socketCreation;
	boolean portError;
	DatagramSocket socket;

	public InetAddress currentHost;
	public int currentPort;
	public boolean publish;

	public int getRQ() {
		return RQ;
	}

	public void setRQ(int rQ) {
		RQ = rQ;
	}

	public Boolean getStartUp() {
		return startUp;
	}

	public void setStartUp(Boolean startUp) {
		this.startUp = startUp;
	}

	public InetAddress getHostName1() {
		return hostName1;
	}

	public void setHostName1(InetAddress hostName1) {
		this.hostName1 = hostName1;
	}

	public InetAddress getHostName2() {
		return hostName2;
	}

	public void setHostName2(InetAddress hostName2) {
		this.hostName2 = hostName2;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public int getPort1() {
		return Port1;
	}

	public void setPort1(int port1) {
		Port1 = port1;
	}

	public int getPort2() {
		return Port2;
	}

	public void setPort2(int port2) {
		Port2 = port2;
	}

	public InetAddress getCurrentHost() {
		return currentHost;
	}

	public void setCurrentHost(InetAddress currentHost) {
		this.currentHost = currentHost;
	}

	public int getCurrentPort() {
		return currentPort;
	}

	public void setCurrentPort(int currentPort) {
		this.currentPort = currentPort;
	}

	public Client(InetAddress ClientIP, int ClientPort, InetAddress IP1, int port1, InetAddress IP2, int port2) {
		RQ = 0;
		startUp = true;
		hostName1 = IP1;
		Port1 = port1;
		hostName2 = IP2;
		Port2 = port2;
		clientIP = ClientIP;
		clientPort = ClientPort;
		printSem = new Semaphore(1);
		subjects = new ArrayList<>();

		response = new Semaphore(0);

		/*
		 * echoSem = new Semaphore(0); publishSem = new Semaphore(0); upSubSem = new
		 * Semaphore(0); upClientSem = new Semaphore(0); regClientSem = new
		 * Semaphore(0); deRegClientSem = new Semaphore(0); /*
		 * 
		 * 
		 */
		registered = false;
		updated = false;
		deRegistered = false;
		subjectsBool = false;
		socketCreation = true;
		publish = true;
		portError = false;

		// InetAddress address = InetAddress.getByName(hostName);

		try {
			socket = new DatagramSocket(clientPort, clientIP);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// client needs localhost 10011 localhost 10012
	public static void main(String[] args) {
		if (args.length < 6) {
			System.out.println("Missing Input");
			return;
		}

		String clientIP = args[0];
		int clientPort = Integer.parseInt(args[1]);

		String hostname1 = args[2]; // IPaddress
		int port1 = Integer.parseInt(args[3]);

		String hostname2 = args[4];
		int port2 = Integer.parseInt(args[5]);

		try {
			Client client = new Client(InetAddress.getByName(clientIP), clientPort, InetAddress.getByName(hostname1),
					port1, InetAddress.getByName(hostname2), port2);
			System.out.println("Starting client connected to host: " + hostname1 + " on port: " + port1);
			System.out.println("Starting client connected to host: " + hostname2 + " on port: " + port2);

			client.service();
		} catch (SocketException ex) {
			System.out.println("Socket error: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		}
	}

	public void service() throws IOException {
		Scanner console = new Scanner(System.in);

		try {

			if (socketCreation) {
				socketHandler socketHandler = new socketHandler(socket, this, printSem);
				socketHandler.start();
				socketCreation = false;
			}

			while (true) {
				// Create a Scanner object to read input.

				// need to first send
				// REGISTER RQ# Name IP Address Socket#
				if (startUp) {
					printSem.acquire();
					System.out.println("Enter the command to be sent (REGISTER, UPDATE):");
					printSem.release();
					String input = console.nextLine();

					input = input.toUpperCase().replace("_", "-");

					switch (input) {
					case "REGISTER":
						registerClient(socket);
						break;

					case "UPDATE":
						updateClient(socket);
						break;

					default:
						printSem.acquire();
						System.out.println("Error: This is not a valid command!");
						printSem.release();
						this.service();
						break;
					}

					startUp = false;
				} else {
					commandInput(socket);
				}
			}

		} catch (SocketTimeoutException ex) {
			System.out.println("Timeout error: " + ex.getMessage());
			ex.printStackTrace();
		} catch (IOException ex) {
			System.out.println("Client error: " + ex.getMessage());
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	private void commandInput(DatagramSocket socket) throws IOException, InterruptedException {
		Scanner console = new Scanner(System.in);

		while (true) {
			printSem.acquire();
			System.out.println("Enter the next command to be sent or enter HELP:");

			printSem.release();
			String input = console.nextLine();

			String splitInput[] = input.split(" ");

			String command = splitInput[0].toUpperCase().replace("_", "-");

			// case statement of input and sending to server
			switch (command) {
			case "DE-REGISTER":
				deRegisterClient(socket);
				break;

			case "SUBJECTS":
				updateSubjects(socket);
				break;

			case "PUBLISH":
				publish(socket);
				break;

			case "UPDATE":
				updateClient(socket);
				break;

			case "ECHO":
				echo(socket);
				break;

			case "SERVING":
				System.out.println("Client is sending to: " + this.currentHost + this.currentPort);
				break;

			case "HELP":
				System.out.println("The current commands you can send are: ");
				System.out.println("DE-REGISTER");
				System.out.println("SUBJECTS");
				System.out.println("PUBLISH");
				System.out.println("UPDATE");
				System.out.println("ECHO");
				System.out.println("SERVING");
				break;

			default:
				System.out.println("Error: This is not a valid command!");
				this.commandInput(socket);
				break;
			}
		}
	}

	private void registerClient(DatagramSocket socket) throws InterruptedException, IOException {
		Scanner console = new Scanner(System.in);

		printSem.acquire();
		System.out.println("Enter your name to register with the server:");
		printSem.release();
		String name = console.nextLine();

		String message1 = "REGISTER " + RQ + " " + name + " " + clientIP + " " + clientPort;

		byte[] requestbuffer1 = message1.getBytes();

		String message2 = "REGISTER " + RQ + " " + name + " " + clientIP + " " + clientPort;

		byte[] requestbuffer2 = message2.getBytes();

		// Send message to server
		DatagramPacket request1 = new DatagramPacket(requestbuffer1, requestbuffer1.length, hostName1, Port1);
		try {
			socket.send(request1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DatagramPacket request2 = new DatagramPacket(requestbuffer2, requestbuffer2.length, hostName2, Port2);
		try {
			socket.send(request2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean acquired = response.tryAcquire(60, TimeUnit.SECONDS);

		if (!acquired) {
			System.out.println("Server has failed to respond please retry or reconnect.");

			startUp = true;
			service();
		}

		else {
			if (registered) {
				System.out.println("Registration is good, you may proceed.");
				clientName = name;

			}

			else {
				System.out.println("Please try to register again.");
				startUp = true;
				service();
			}
		}
	}

	private void deRegisterClient(DatagramSocket socket) throws InterruptedException {
		String sendingMessage = "DE-REGISTER " + RQ + " " + clientName;

		byte[] requestbuffer = sendingMessage.getBytes();

		// Send message to server
		DatagramPacket request = new DatagramPacket(requestbuffer, requestbuffer.length, currentHost, currentPort);
		try {
			socket.send(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean acquired = response.tryAcquire(60, TimeUnit.SECONDS);
		if (!acquired) {
			System.out.println("Server has failed to respond please retry or reconnect.");
		}
		if (!registered) {
			System.out.println("de-register successful");
			startUp = true;
			try {
				service();
			} catch (IOException e) {

			}
		}

	}

	private void updateClient(DatagramSocket socket) throws InterruptedException {
		Scanner console = new Scanner(System.in);

		printSem.acquire();
		System.out.println("Enter your name to update from the server:");
		printSem.release();

		String name = console.nextLine();

		String message1 = "UPDATE " + RQ + " " + name + " " + clientIP + " " + clientPort;

		byte[] requestbuffer1 = message1.getBytes();

		String message2 = "UPDATE " + RQ + " " + name + " " + clientIP + " " + clientPort;

		byte[] requestbuffer2 = message2.getBytes();

		// Send message to server
		DatagramPacket request1 = new DatagramPacket(requestbuffer1, requestbuffer1.length, hostName1, Port1);
		try {
			socket.send(request1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Send message to server
		DatagramPacket request2 = new DatagramPacket(requestbuffer2, requestbuffer2.length, hostName2, Port2);
		try {
			socket.send(request2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean acquired = response.tryAcquire(60, TimeUnit.SECONDS);

		if (!acquired) {
			System.out.println("Server has failed to respond please retry or reconnect.");
			startUp = true;
			try {
				service();
			} catch (IOException e) {

			}
		}

		if (portError) {
			System.out.println("portError resarting the client");
			startUp = true;
			try {
				portError = false;
				service();
			} catch (IOException e) {

			}

		}

		if (clientUpdate) {
			System.out.println("Update sucessful please continue.");
			clientName = name;

		}

		else {
			System.out.println("Update not sucessful please try again.");
			try {
				service();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void updateSubjects(DatagramSocket socket) throws InterruptedException {
		printSem.acquire();
		System.out.println("Enter the subjects you would like to subscribe too:");
		printSem.release();
		Scanner console = new Scanner(System.in);

		String message = console.nextLine();
		String splitMessage[] = message.split(" ");

		String subjects_sent = "";

		for (String subject : splitMessage) {
			subjects_sent += subject + " ";
		}

		String sendingMessage = "SUBJECTS " + RQ + " " + clientName + " " + subjects_sent;

		byte[] requestbuffer = sendingMessage.getBytes();

		// Send message to server
		DatagramPacket request = new DatagramPacket(requestbuffer, requestbuffer.length, currentHost, currentPort);
		try {
			socket.send(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean acquired = response.tryAcquire(60, TimeUnit.SECONDS);

		if (!acquired) {
			System.out.println("Server has failed to respond please retry or reconnect.");
		}

		if (portError) {
			System.out.println("portError resarting the client");
			startUp = true;
			try {
				portError = false;
				service();
			} catch (IOException e) {

			}

		}

		RQ += 1;

	}

	private void publish(DatagramSocket socket) throws InterruptedException {
		printSem.acquire();
		System.out.println("Enter the subject you would like to publish to:");
		printSem.release();
		Scanner console = new Scanner(System.in);
		String subject = console.nextLine();

		printSem.acquire();
		System.out.println("Enter the message you would like to send:");
		printSem.release();

		String message = console.nextLine();

		// have a check here for if the subject is in its thing

		String sendingMessage = "PUBLISH " + RQ + " " + clientName + " " + subject + " " + message;
		/*
		 * System.out.print("CLIENT send: "); System.out.println(sendingMessage);
		 */

		byte[] requestbuffer = sendingMessage.getBytes();

		// Send message to server
		DatagramPacket request = new DatagramPacket(requestbuffer, requestbuffer.length, currentHost, currentPort);
		try {
			socket.send(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RQ += 1;

		boolean acquired = response.tryAcquire(10, TimeUnit.SECONDS);

		if (portError) {
			System.out.println("portError resarting the client");
			startUp = true;
			try {
				portError = false;
				service();
			} catch (IOException e) {

			}

		}

		if (!publish) {
			System.out.println("You are not registered in that interest or it does not exist, please subscribe first.");
			publish = true;
		}
		if (startUp) {
			try {
				service();
			} catch (IOException e) {

			}
		}
		printSem.release();

	}

	private void echo(DatagramSocket socket) throws InterruptedException {
		printSem.acquire();
		System.out.println("Enter the message you would like to echo:");
		printSem.release();
		Scanner console = new Scanner(System.in);
		String message = console.nextLine();

		String sendingMessage = "ECHO " + RQ + " " + clientName + " " + message;
		/*
		 * System.out.print("CLIENT send: "); System.out.println(sendingMessage);
		 */

		byte[] requestbuffer = sendingMessage.getBytes();

		// Send message to server
		DatagramPacket request = new DatagramPacket(requestbuffer, requestbuffer.length, currentHost, currentPort);
		try {
			socket.send(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RQ += 1;

		boolean acquired = response.tryAcquire(60, TimeUnit.SECONDS);
		if (!acquired) {
			System.out.println("Server has failed to respond please retry or reconnect.");
		}

		if (portError) {
			System.out.println("portError resarting the client");
			startUp = true;
			try {
				portError = false;
				service();
			} catch (IOException e) {

			}

		}

	}

}
