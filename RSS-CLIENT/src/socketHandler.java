import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;

public class socketHandler extends Thread{
	DatagramSocket socket;
	Client client;
	Semaphore printSem;
	public socketHandler(DatagramSocket s, Client c, Semaphore Sem) {
		this.socket = s;
		this.client = c;
		this.printSem = Sem;
		
	}
	
	
	
	@Override
	public void run() {
	
		while (true) {
			DatagramPacket packet = null;
			

			byte[] clientMessage = new byte[50000];
			
			packet = new DatagramPacket(clientMessage, clientMessage.length);

			try {
				socket.receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String message = formatMessage(clientMessage).toString();
			
			
			try {
				printSem.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			System.out.print("Client has recieved:");
			System.out.println(message);
			System.out.println();

			
			
			printSem.release();

			String splitMessage[] = message.split(" ");
			
			
			String command = splitMessage[0].toUpperCase().replace("_", "-");
			
			//System.out.println(splitMessage[0] + splitMessage[1] + splitMessage[2]);
			System.out.println();
			
			switch(command) {
				case "REGISTERED":
					System.out.println("Client has recieved:");
					System.out.println(message);
					registered(packet, splitMessage);
					break;
					
				case "DE-REGISTER":
					System.out.println("Client has recieved:");
					System.out.println(message);
					deRegister(packet, splitMessage);
					break;
					
					
				case "ECHO":
					System.out.println("Client has recieved echo :");
					System.out.println(message);
					
					

					break;
					
					
				case "SUBJECTS-UPDATED":
					System.out.println("Client has recieved:");
					client.subjects.add(splitMessage[3]);
					System.out.println(message);
					subjectsUpdated(packet, splitMessage);
					break;
					
				case "REGISTER-DENIED":
					System.out.println("Client has recieved:");
					System.out.println(message);
					client.startUp = true;
				case "UPDATE-DENIED":
					System.out.println("Client has recieved:");
					System.out.println(message);
					
					break;
					
				case "SUBJECTS-REJECTED":
					System.out.println("Client has recieved:");
					System.out.println(message);
					
					break;
				case "CHANGE-SERVER":
					System.out.println("Port 1: " + client.Port1 + " " + "Port from message: " + Integer.parseInt(splitMessage[2]));
					try {
						if(splitMessage[1].contains("/")) {
							String hostName[] = splitMessage[1].split("/");
							client.currentHost = InetAddress.getByName(hostName[1]);
						}
						else {
							System.out.println("hiiii "+ splitMessage[1].getBytes());
							client.currentHost = InetAddress.getByName(splitMessage[1]);
						}
						
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					client.currentPort = Integer.parseInt(splitMessage[2]);
					break;
					
				case "MESSAGE":
					try {
						printSem.acquire();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					System.out.println("you have recieved a new message from user:");
					System.out.println(splitMessage[1]);
					System.out.println("about your shared topic of:");
					System.out.println(splitMessage[2]);
					System.out.println("message is:");
					
					
					int count = 3;
					String messageRec = " ";
					while(count < splitMessage.length) {
						messageRec = messageRec + " " + splitMessage[count];
						count = count +1;
					}
					
					System.out.println(messageRec);
					printSem.release();
				
					break;
				default:
					System.out.println("server response not reconized");
				
				
			} 
			
			
		}

		
		
	}
	
	
	
	public void registered(DatagramPacket requestPacket, String splitMessage[]) {
	
		client.currentHost = requestPacket.getAddress();
		client.currentPort = requestPacket.getPort();
	
	
		
	} 
	
	
	public void updatedApproved(DatagramPacket requestPacket,String splitMessage[]) {
		
		client.currentHost = requestPacket.getAddress();
		client.currentPort = requestPacket.getPort();
		
	}
	

	public void updatedDenied(DatagramPacket requestPacket, String splitMessage[]) {
	
		//TODO
		
	}
	
	public void subjectsUpdated(DatagramPacket requestPacket, String splitMessage[]) {
		
	
	}
	
	
	
	public void deRegister(DatagramPacket requestPacket, String splitMessage[]) {
	
			
					client.startUp = true;
					client.RQ += 1;
				
	
	}
	
	
	
	
	
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
