import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Client {

	int RQ;
	Boolean startUp;
	InetAddress hostName1;
	InetAddress hostName2;
	String clientName;
	int Port1;
	int Port2;
	
	InetAddress currentHost;
	int currentPort;
	
	public Client(InetAddress IP1, int port1, InetAddress IP2, int port2) {
		RQ = 0;
		startUp = true;
		hostName1 = IP1;
		Port1 = port1;
		hostName2 = IP2;
		Port2 = port2;
	}
	
	//client needs localhost 10011 localhost 10012
	public static void main(String[] args) {

		if (args.length < 4) {
			System.out.println("Missing Input");
			return;

		}

		String hostname1 = args[0]; // IPaddress
		int port1 = Integer.parseInt(args[1]);
		
		String hostname2 = args[2];
		int port2 = Integer.parseInt(args[3]);

		try {
			Client client = new Client(InetAddress.getByName(hostname1), port1, InetAddress.getByName(hostname2), port2);
			System.out.println("Starting Client connected to host: " + hostname1 + " on port: " + port1);
			System.out.println("Starting Client connected to host: " + hostname2 + " on port: " + port2);
			
			client.service();

		} catch (SocketException ex) {
			System.out.println("Socket error: " + ex.getMessage());
		} catch (IOException ex) {
			System.out.println("I/O error: " + ex.getMessage());
		}

	}

	private void service() throws IOException {
		Scanner console = new Scanner(System.in);
		
		try {
			// InetAddress address = InetAddress.getByName(hostName);
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(10000); // set time out to 10s

			while (true) {
				// Create a Scanner object to read input.

				// need to first send
				// REGISTER RQ# Name IP Address Socket#
				if (startUp) {
					
					System.out.print("Enter the command to be sent (REGISTER, UPDATE): ");
					String input = console.nextLine();
					
					input = input.toUpperCase().replace("_", "-");
					
					switch(input) {
						case "REGISTER":
							registerCall(socket);
							break;
						
						case "UPDATE":
							updateClient(socket);
							break;
							
						default:
							System.out.println("Error: This is not a valid command!");
							this.service();
							break;
					}
					
					startUp = false;
				} else {
					commandInput(socket);
				}

				//Thread.sleep(10000);
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
		String input;

		while (true) {
			System.out.print("Enter the next command to be sent: ");

			input = console.nextLine();

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
			
			case "ECHO":
				echo(socket);
				break;
				
			default:
				System.out.println("Error: This is not a valid command!");
				this.commandInput(socket);
				break;
			}
		}

	}
	
	private void registerCall(DatagramSocket socket) {
		Scanner console = new Scanner(System.in);
		
		System.out.print("Enter your name to register with the server: ");

		String name = console.nextLine();

		clientName = name;
		
		String message1 = "REGISTER " + RQ + " " + name + " " + hostName1 + " " + Port1;
		System.out.print("CLIENT sends: ");
		System.out.println(message1);

		byte[] requestbuffer1 = message1.getBytes();
		
		String message2 = "REGISTER " + RQ + " " + name + " " + hostName2 + " " + Port2;
		System.out.print("CLIENT sends: ");
		System.out.println(message2);

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

		// wait for response
		byte[] buffer = new byte[512];
		DatagramPacket response = new DatagramPacket(buffer, buffer.length);
		try {
			socket.receive(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // blocking call

		currentHost = response.getAddress();
		currentPort = response.getPort();
		
		// format and print response
		String serverMessage = new String(buffer, 0, response.getLength());
		System.out.print("CLIENT receives: ");
		System.out.println(serverMessage);

		String splitMessage[] = serverMessage.split(" ");
		RQ += 1;
		
		String command = splitMessage[0].toUpperCase().replace("_", "-");

		if (command.equals("REGISTER-DENIED")) {
			registerCall(socket);
		}

	}

	private void deRegisterClient(DatagramSocket socket) {

		String sendingMessage = "DE-REGISTER " + RQ + " " + clientName;
		System.out.print("CLIENT sends: ");
		System.out.println(sendingMessage);

		byte[] requestbuffer = sendingMessage.getBytes();

		// Send message to server
		DatagramPacket request = new DatagramPacket(requestbuffer, requestbuffer.length, currentHost, currentPort);
		try {
			socket.send(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// wait for response for dereg
		byte[] buffer = new byte[512];
		DatagramPacket response = new DatagramPacket(buffer, buffer.length);
		try {
			socket.receive(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // blocking call

		// format and print response
		String serverMessage = new String(buffer, 0, response.getLength());

		String splitMessage[] = serverMessage.split(" ");
		
		String command = splitMessage[0].toUpperCase().replace("_", "-");
		String name = splitMessage[2];
		
		// if De-register is successful
		if (command.equals("DE-REGISTER")) {
			System.out.print("CLIENT receives: ");
			System.out.println("DE-REGISTER " + name + " successful");
			startUp = true;
			RQ += 1;
		}
	}
	
	private void updateClient(DatagramSocket socket) {
		Scanner console = new Scanner(System.in);
		
		System.out.print("Enter your name to update from the server: ");

		String name = console.nextLine();

		clientName = name;
		
		String message1 = "UPDATE " + RQ + " " + name + " " + hostName1 + " " + Port1;
		System.out.print("CLIENT sends: ");
		System.out.println(message1);

		byte[] requestbuffer1 = message1.getBytes();
		
		String message2 = "UPDATE " + RQ + " " + name + " " + hostName2 + " " + Port2;
		System.out.print("CLIENT sends: ");
		System.out.println(message2);

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

		// wait for response
		byte[] buffer = new byte[512];
		DatagramPacket response = new DatagramPacket(buffer, buffer.length);
		try {
			socket.receive(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // blocking call

		currentHost = response.getAddress();
		currentPort = response.getPort();
		
		// format and print response
		String serverMessage = new String(buffer, 0, response.getLength());
		System.out.print("CLIENT receives: ");
		System.out.println(serverMessage);

		String splitMessage[] = serverMessage.split(" ");
		RQ += 1;
		
		String command = splitMessage[0].toUpperCase().replace("_", "-");
		
		if (command.equals("UPDATE-DENIED")) {
			updateClient(socket);
		}
	}
	
	private void updateSubjects(DatagramSocket socket) {
		System.out.print("Enter the subjects you would like to subscribe too: ");
		Scanner console = new Scanner(System.in);
		
		String message = console.nextLine();
		String splitMessage[] = message.split(" ");
		
		String subjects_sent = "";
		
		for(String subject : splitMessage) {
			subjects_sent += subject + " ";
		}
		

		String sendingMessage = "SUBJECTS " + RQ + " " + clientName + " " + subjects_sent;
		System.out.print("CLIENT sends: ");
		System.out.println(sendingMessage);

		byte[] requestbuffer = sendingMessage.getBytes();

		// Send message to server
		DatagramPacket request = new DatagramPacket(requestbuffer, requestbuffer.length, currentHost, currentPort);
		try {
			socket.send(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Wait for response
		byte[] buffer = new byte[512];
		DatagramPacket response = new DatagramPacket(buffer, buffer.length);
		try {
			socket.receive(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // blocking call

		// format and print response
		String serverMessage = new String(buffer, 0, response.getLength());
		
		System.out.print("CLIENT receives: ");
		System.out.println(serverMessage);

		RQ += 1;
		
		String splitMessage2[] = serverMessage.split(" ");
		
		String command = splitMessage2[0].toUpperCase().replace("_", "-");
		
		if (command.equals("SUBJECTS-REJECTED")) {
			updateSubjects(socket);
		}
	}

	
	private void echo(DatagramSocket socket) {
		
		System.out.print("Enter the message you would like to echo: ");
		Scanner console = new Scanner(System.in);
		String message = console.nextLine();
	
		String sendingMessage = "ECHO " + RQ + " " + clientName + " " + message ;
		System.out.print("CLIENT send: ");
		System.out.println(sendingMessage);

		byte[] requestbuffer = sendingMessage.getBytes();

		// Send message to server
		DatagramPacket request = new DatagramPacket(requestbuffer, requestbuffer.length, currentHost, currentPort);
		try {
			socket.send(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// wait for response for dereg
		byte[] buffer = new byte[512];
		DatagramPacket response = new DatagramPacket(buffer, buffer.length);
		try {
			socket.receive(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // blocking call

		// format and print response
		String serverMessage = new String(buffer, 0, response.getLength());

		String splitMessage[] = serverMessage.split(" ");
		
		System.out.print("CLIENT receives: ");
		System.out.println(serverMessage);	
	}
}