import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Vector;

public class Server {

	ArrayList<String> clients;
	Vector<ClientHandler> clientHandlers = new Vector<>();
	final int port;
	int clientCount;

	public Server(int port) throws SocketException {

		clients = new ArrayList<>();
		this.port = port;
		clientCount = 0;
	}

	// Main function starts up server
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Missing Input");
			return;
		}

		int port = Integer.parseInt(args[0]);

		try {
			Server server = new Server(port);
			System.out.println("Server listening on port " + port);
			server.service();

		} catch (SocketException ex) {
			System.out.println("Socket error: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		}
	}

	private void service() throws IOException {
		DatagramSocket socket = new DatagramSocket(port);

		while (true) {
			DatagramPacket requestPacket = null;
			try {

				byte[] clientMessage = new byte[50000];
				requestPacket = new DatagramPacket(clientMessage, clientMessage.length);

				socket.receive(requestPacket);

				String message = formatMessage(clientMessage).toString();

				String splitMessage[] = message.split(" ");
				
				System.out.println(message);
				
				switch(splitMessage[0]) {
				
					case "REGISTER":
						
						registerClient(socket, splitMessage, requestPacket);
						
						break;
						
					case "DE-REGISTER":
						
						deRegisterClient(socket, splitMessage, requestPacket);
						
						break;
					
					default:
						for (int i = 0; i < clientHandlers.size(); i++) {
							if (clientHandlers.get(i).getName().equals(splitMessage[2])) {
								clientHandlers.get(i).newPacket(requestPacket);
							}
						}
						
						break;
					
					
					
				} 
				
				
				
				

				
			} catch (Exception e) {
				socket.close();
				e.printStackTrace();
			}

		}
	}
	
	private void registerClient(DatagramSocket socket, String splitMessage[], DatagramPacket packet) {
		

		if (!(clients.contains(splitMessage[2]))) {
			clients.add(splitMessage[2]);
			ClientHandler t = new ClientHandler(socket, packet, clientCount, splitMessage[2], this);

			clientCount = clientCount + 1;
			// Invoking the start() method
			t.start();

			clientHandlers.add(t);
		} else {

			// REGISTER-DENIED RQ# Reason
			String message = "REGISTER_DENIED" + " " + splitMessage[1] + " " + "NAME_IN_USE";
			System.out.println(message);

			byte[] buffer = message.getBytes();

			DatagramPacket response = new DatagramPacket(buffer, buffer.length, packet.getAddress(),
					packet.getPort());
			try {
				socket.send(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	private void deRegisterClient(DatagramSocket socket, String splitMessage[], DatagramPacket packet) {
		
		int RQ = 0;
		for (int i = 0; i < clientHandlers.size(); i++) {
			if (clientHandlers.get(i).getName().equals(splitMessage[2])) {
			
				
				RQ = clientHandlers.get(i).RQ;
				
				clientHandlers.get(i).stop();
				clientHandlers.remove(i);
				
				//probs dont need
				clientCount = clientCount -1;
				
				
				clients.remove(splitMessage[2]);
				
			}
		}
		
		String message = "DE-REGISTER" + " " +RQ +" " +splitMessage[2];
		System.out.println(message);

		byte[] buffer = message.getBytes();

		DatagramPacket response = new DatagramPacket(buffer, buffer.length, packet.getAddress(),
				packet.getPort());
		try {
			socket.send(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	//TODO: REMOVE DATA FROM THE THING
		
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

