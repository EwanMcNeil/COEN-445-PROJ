import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

//ClientHandler class 
//there are one client handler in each server which corresponds to a client communication 


class ClientHandler extends Thread {

	final DatagramSocket s;
	Boolean startUp;
	DatagramPacket request;
	int RQ;
	Server server;
	String name;
	Semaphore messageFlag;
	InetAddress clientAddress;
	int clientPort;
	ArrayList<String> subjects;

	// Constructor
	public ClientHandler(DatagramSocket s, InetAddress address, int port, Semaphore newMessageFlag, String Name,
			Server server, ArrayList<String> subjects, boolean startUp, int RQ) {
		this.s = s;
		this.request = null;
		setName(Name);
		this.name = Name;
		this.RQ = RQ;
		this.startUp = startUp;
		this.server = server;
		messageFlag = newMessageFlag;
		this.subjects = subjects;
		clientAddress = address;
		clientPort = port;

	}

	
	//
	//this is the main loop that takes in requests and diverges them to the proper functions
	//
	@Override
	public void run() {
		while (true) {
			try {
				messageFlag.acquire();
				String message = formatMessage(request.getData()).toString();
				String splitMessage[] = message.split(" ");
				String name = splitMessage[2];

				if (startUp) {
					// REGISTERED RQ#
					registerClient(splitMessage);
				}

				else {
					if (name.equals(this.getName())) {
						String command = splitMessage[0].toUpperCase().replace("_", "-");

						switch (command) {
						case "UPDATE":
							updateClient(splitMessage);
							break;

						case "SUBJECTS":
						case "SUBJECTS-UPDATED":
						case "SUBJECTS-REJECTED":
							updateSubjects(clientAddress, clientPort, splitMessage);
							break;

						case "ECHO":
							echoClient(clientAddress, clientPort);
							break;
						}
					}
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void registerClient(String splitMessage[]) {
		String message = "REGISTERED " + RQ;
		String message2 = "REGISTERED " + RQ + " " + splitMessage[2] + " " + splitMessage[3] + " " + splitMessage[4];

		byte[] buffer = message.getBytes();
		byte[] buffer2 = message2.getBytes();

		if (server.isServing) {
			DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);

			DatagramPacket ServerResponse = new DatagramPacket(buffer2, buffer2.length, server.Server2, server.Port2);

			try {
				s.send(response);
				s.send(ServerResponse);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		startUp = false;
		RQ += 1;

		server.writeClientsFiles();
	}

	private void updateClient(String splitMessage[]) {
		String name = splitMessage[2];

		String message = "UPDATE-CONFIRMED " + RQ + " " + splitMessage[2] + " " + splitMessage[3] + " "
				+ splitMessage[4];

		try {
			if (splitMessage[3].contains("/")) {
				String hostName[] = splitMessage[3].split("/");
				this.clientAddress = InetAddress.getByName(hostName[1]);
			} else {
				this.clientAddress = InetAddress.getByName(splitMessage[3]);
			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.clientPort = Integer.parseInt(splitMessage[4]);

		byte[] buffer = message.getBytes();

		if (server.isServing) {
			DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);

			DatagramPacket ServerResponse = new DatagramPacket(buffer, buffer.length, server.Server2, server.Port2);

			try {
				s.send(response);
				s.send(ServerResponse);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		RQ += 1;

		server.writeClientsFiles();
	}

	private void updateSubjects(InetAddress clientAddress, int clientPort, String splitMessage[]) {
		ArrayList<String> accepted_subjects = new ArrayList<>(Arrays.asList("FOOTBALL", "HOCKEY", "GOLF"));
		String message = "";
		String subjects_sent = "";
		boolean all_in = true;
		String new_subjects = "";

		splitMessage = Arrays.copyOfRange(splitMessage, 3, splitMessage.length);

		/* We format the subjects coming from the request to send the response */
		for (String subject : splitMessage) {
			subjects_sent += subject.toUpperCase() + " ";
		}

		/* We check that all subjects from the request are accepted */
		for (String subject : splitMessage) {
			subject = subject.toUpperCase();

			if (!accepted_subjects.contains(subject)) {
				all_in = false;
				System.out.println(subject + " is not an accepted subject.");
			}
		}

		/* We format the message depending of if the subjects are accepted or not */
		if (all_in) {
			for (String subject : splitMessage) {
				subject = subject.toUpperCase();

				if (!subjects.contains(subject))
					subjects.add(subject);
			}

			message = "SUBJECTS-UPDATED " + RQ + " " + getName() + " " + subjects_sent;
		}

		else {
			message = "SUBJECTS-REJECTED " + RQ + " " + getName() + " " + subjects_sent;
		}

		byte[] buffer = message.getBytes();

		if (server.isServing) {
			DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);

			DatagramPacket ServerResponse = new DatagramPacket(buffer, buffer.length, server.Server2, server.Port2);

			try {
				s.send(response);
				s.send(ServerResponse);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		RQ += 1;

		server.writeClientsFiles();
	}

	private void echoClient(InetAddress clientAddress, int clientPort) {
		String message = "ECHO " + RQ + " " + getName();

		byte[] buffer = message.getBytes();

		if (server.isServing) {
			DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);

			DatagramPacket ServerResponse = new DatagramPacket(buffer, buffer.length, server.Server2, server.Port2);

			try {
				s.send(response);
				s.send(ServerResponse);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		RQ += 1;
	}

	public void newPacket(DatagramPacket input) {
		this.request = input;
	}

	public int getRQ() {
		return RQ;
	}

	//This method is from the tutorial and is needed to convert
	// the buffer from the input into strings
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