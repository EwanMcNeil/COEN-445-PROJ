import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*; 
//ClientHandler class 
class ClientHandler extends Thread {

	final DatagramSocket s;
	Boolean startUp;
	DatagramPacket request;
	final int count;
	int RQ;
	Server server;
	Semaphore messageFlag;
	InetAddress clientAddress;
	int clientPort;
	
	// Constructor
	public ClientHandler(DatagramSocket s, DatagramPacket request, Semaphore newMessageFlag,  int count, String Name, Server server) {
		this.s = s;
		this.request = request;
		this.count = count;
		setName(Name);
		this.RQ = 0;
		startUp = true;
		this.server = server;
		messageFlag = newMessageFlag;
	}

	@Override
	public void run() {
		//System.out.println("Started new Client Thread");

		// server can get the info about the client
		clientAddress = request.getAddress();
	    clientPort = request.getPort();

		while (true) {
			try {

				messageFlag.acquire();
				String message = formatMessage(request.getData()).toString();
				String splitMessage[] = message.split(" ");
				String name = splitMessage[2];
				
				//System.out.println("FLAG ACQUIRED");
				
				if (startUp) {
					// REGISTERED RQ#
					registerClient();
				} 
				
				else {
					if (name.equals(this.getName())) {
						
						String command = splitMessage[0].toUpperCase().replace("_", "-");
						
							switch(command) {
								case "UPDATE":
									updateClient();
									break;
								
								case "SUBJECTS":
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
	
	private void registerClient() {
		String message = "REGISTERED " + RQ;
		
		System.out.print("Server sends: ");
		System.out.println(message);
		
		byte[] buffer = message.getBytes();
	
		DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
		
		DatagramPacket ServerResponse = new DatagramPacket(buffer, buffer.length, server.Server2, server.Port2);
		
		try {
			s.send(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		startUp = false;
		RQ += 1;
		
		//s.send(ServerResponse);
	}
	
	private void updateClient() {
		String message  = "UPDATE-CONFIRMED " + RQ + " " + getName();
		System.out.print("Server sends: ");
		System.out.println(message);
		this.clientAddress = this.request.getAddress();
		this.clientPort = this.request.getPort();

		byte[] buffer = message.getBytes();
		
		if(server.isServing) {
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
	
	private void updateSubjects(InetAddress clientAddress, int clientPort, String splitMessage[]) {
		ArrayList<String> accepted_subjects = new ArrayList<>(Arrays.asList("FOOTBALL", "HOCKEY", "GOLF"));
		String message = "";
		String subjects_sent = "";
		boolean all_in = true;
		
		splitMessage = Arrays.copyOfRange(splitMessage, 3, splitMessage.length);
		
		for(String subject : splitMessage) {
			subjects_sent += subject.toUpperCase() + " ";
		}
		
		for(String subject : splitMessage) {
			subject = subject.toUpperCase();
			
			if(!accepted_subjects.contains(subject)) {
				all_in = false;
				break;
			}
		}
		
		if(all_in)
			message = "SUBJECTS-UPDATED " + RQ + " " + getName() + " " + subjects_sent;
		else
			message  = "SUBJECTS-REJECTED " + RQ + " " + getName() + " " + subjects_sent;
		
		System.out.print("Server sends: ");
		System.out.println(message);
		
		this.clientAddress = this.request.getAddress();
		this.clientPort = this.request.getPort();

		byte[] buffer = message.getBytes();
		
		if(server.isServing) {
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
	
	private void echoClient(InetAddress clientAddress, int clientPort) {
		String message  = "ECHO " + RQ + " " + getName();
		
		System.out.print("Server sends: ");
		System.out.println(message);
		
		byte[] buffer = message.getBytes();
		
		if(server.isServing) {
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

	// A method to convert the byte array data into a string representation.
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