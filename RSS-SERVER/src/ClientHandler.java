import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
	public ClientHandler(DatagramSocket s, DatagramPacket request,Semaphore newMessageFlag,  int count, String Name, Server server) {
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
		System.out.println("Started new Client Thread");

		// server can get the info about the client
		clientAddress = request.getAddress();
	    clientPort = request.getPort();

		while (true) {
			try {

				messageFlag.acquire();
				String message = formatMessage(request.getData()).toString();
				String splitMessage[] = message.split(" ");
				System.out.println("FLAG AQUIRED");
				if (startUp) {
					// REGISTERED RQ#
					if(server.isServing) {
					message = "REGISTERED " + RQ;
					byte[] buffer = message.getBytes();
					DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
					s.send(response);

					}
					startUp = false;
					RQ += 1;
				} 
				else {
					if (splitMessage[2].equals(this.getName())) {
						
//						
							switch(splitMessage[0]) {
								case "UPDATE":
									updateClient();
									break;
								case "ECHO":
									echoClient(clientAddress, clientPort);
									break;
							}
						}
					}
				
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void updateClient() {
		String message  = "UPDATE-CONFIRMED " + RQ + " " + getName();
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