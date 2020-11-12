import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.*; 

public class Server {
	Vector<ClientHandler> clientHandlers = new Vector<>();
	Vector<Semaphore> messageFlags = new Vector<>();
	final int port;
	int clientCount;
	InetAddress Server2;
	int Port2;
	static boolean isServing = false;
	DatagramSocket socket;
	
	public Server(int port, boolean isServing, InetAddress IP, int port2) throws SocketException {
		this.port = port;
		clientCount = 0;
		this.isServing = isServing;
		Server2 = IP;
		Port2 = port2;
	}

	// Main function starts up server
	//server needs 10011 1 localhost 10012
	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println("Missing Input");
			return;
		}

		
		int port = Integer.parseInt(args[0]);
		boolean is_serving = false;
		
		if(Integer.parseInt(args[1]) == 1)
			is_serving = true;
		else
			is_serving = false;
			
		String Server2_name = args[2];
		int port2 = Integer.parseInt(args[3]);

		try {
			Server server = new Server(port, is_serving, InetAddress.getByName(Server2_name), port2);
			
			System.out.println("Server listening on port " + port);
			System.out.println("Serving: " + Boolean.valueOf(isServing));
			
			//initializeClients();
			
			server.service();
			

		} catch (SocketException ex) {
			System.out.println("Socket error: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		}
	}

	private void service() throws IOException {
		socket = new DatagramSocket(port);
		
		//initializeClients();
		
		while (true) {
			DatagramPacket requestPacket = null;
			
			try {
				byte[] clientMessage = new byte[50000];
				requestPacket = new DatagramPacket(clientMessage, clientMessage.length);

				socket.receive(requestPacket);

				String message = formatMessage(clientMessage).toString();

				String splitMessage[] = message.split(" ");
				
				System.out.print("Server receives: ");
				System.out.println(message);
				
				String command = splitMessage[0].toUpperCase().replace("_", "-");
				String name = splitMessage[2];
				
				initializeClients();
				
				switch(command) {
					case "REGISTER":
						registerClient(socket, splitMessage, requestPacket);
						break;
						
					case "DE-REGISTER":
						deRegisterClient(socket, splitMessage, requestPacket);
						break;
					
					default:
						otherRequests(socket, splitMessage, requestPacket);
						break;
				} 

			} catch (Exception e) {
				//here an exception causes the program to crash if out of bounds
				socket.close();
				e.printStackTrace();
			}
		}
	}
	
	public void writeClientsFile() {
		FileWriter writer;
		String output = "";
		
		try {
			writer = new FileWriter("clients_files.txt");
			
			for(int j = 0; j < clientHandlers.size(); j++) {
				ArrayList<String> subjects_list = clientHandlers.get(j).subjects;
				String client_subjects = "";
				
				for(int k = 0; k < subjects_list.size(); k++)
					if(k != subjects_list.size() - 1)
						client_subjects += subjects_list.get(k) + ",";
					
					else
						client_subjects += subjects_list.get(k);
				
				if(subjects_list.size() != 0)
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
		System.out.println("In initializeClients");
		String file_name = "clients_files.txt";
		
		File file = new File(file_name);
		BufferedReader reader;
		
		DatagramPacket requestPacket = null;
		Semaphore messageFlag = new Semaphore(1);

		if(file.exists() &&  !file.isDirectory()) {
			reader = new BufferedReader(new FileReader(file_name));
				
			String line = reader.readLine();
			String subjects[] = null;
			
			while(line != null) {
				String splitLine[] = line.split(" ");
				
				if(line.contains(",")) {
					subjects = splitLine[2].split(",");
				}
					
				
				System.out.print("splitLine: ");
				for(String s : splitLine)
					System.out.print(s + " ");
				System.out.println("");
				
				/*if(subjects.length != 0) {
					System.out.print("subjects: ");
					for(String s : subjects)
						System.out.print(s + " ");
					System.out.println("");
				}*/
				
				/**ClientHandler client = new ClientHandler(socket, requestPacket, messageFlag, clientCount, splitLine[0], this, (ArrayList<String>) Arrays.asList(subjects));
				
				client.start();
				
				clientHandlers.add(client);*/
				
				
				
				line = reader.readLine();
			}
			
			reader.close();

		}
			
	}
	
	private void registerClient(DatagramSocket socket, String splitMessage[], DatagramPacket packet) {
		
		String name = splitMessage[2];
		ArrayList<String> subjects = new ArrayList<>();
		ArrayList<String> clients_name = new ArrayList<>();
		
		for(ClientHandler clientHandler : clientHandlers)
			clients_name.add(clientHandler.name);
		
		if (!(clients_name.contains(name))) {
			Semaphore messageFlag = new Semaphore(1);
			
			messageFlags.add(messageFlag);
			
			ClientHandler t = new ClientHandler(socket, packet, messageFlag, clientCount, name, this, subjects);

			clientCount += 1;
			
			// Invoking the start() method
			t.start();

			clientHandlers.add(t);
		} 
		
		else {
			// REGISTER-DENIED RQ# Reason
			String message = "REGISTER-DENIED" + " " + splitMessage[1] + " " + "NAME_IN_USE";
			String message2 = "REGISTER-DENIED" + " " + splitMessage[1] + " " + splitMessage[2] + " " + splitMessage[3] + " " + splitMessage[4];


			byte[] buffer = message.getBytes();
			byte[] buffer2 = message2.getBytes();

			if(isServing) {
				DatagramPacket response = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
				
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
		
		for (int i = 0; i < clientHandlers.size(); i++) {
			if (clientHandlers.get(i).getName().equals(name)) {
				RQ = clientHandlers.get(i).RQ;
				
				clientHandlers.get(i).stop();
				clientHandlers.remove(i);
				messageFlags.remove(i);
				
				clientCount -= 1;

				writeClientsFile();
			}
		}
		
		String message = "DE-REGISTER" + " " + RQ + " " + name;

		byte[] buffer = message.getBytes();

		if(isServing) {
			DatagramPacket response = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
			
			DatagramPacket ServerResponse = new DatagramPacket(buffer, buffer.length, Server2, Port2);
			
			try {
				socket.send(response);
				socket.send(ServerResponse);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	private void otherRequests(DatagramSocket socket, String splitMessage[], DatagramPacket packet) throws IOException {
		String name = splitMessage[2];
		
		ArrayList<String> clients_name = new ArrayList<>();
		
		for(ClientHandler clientHandler : clientHandlers)
			clients_name.add(clientHandler.name);
		
		//initializeClients();
		
		if(clients_name.contains(name)) {
			for (int i = 0; i < clientHandlers.size(); i++) {
				if (clientHandlers.get(i).getName().equals(name)) {
					clientHandlers.get(i).newPacket(packet);
					messageFlags.get(i).release();
				}
				
			}
		}
		
		else {
			
			if(isServing) {
				String message1 = "UPDATE-DENIED " + splitMessage[1] + " NAME_NOT_IN_USE";
			
				byte[] buffer1 = message1.getBytes();
			
				DatagramPacket response1 = new DatagramPacket(buffer1, buffer1.length, packet.getAddress(), packet.getPort());
			
				try {
					socket.send(response1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
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

