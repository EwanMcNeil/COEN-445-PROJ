import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

	int RQ;
	Boolean startUp;
	InetAddress hostName1;
	InetAddress hostName2;
	String clientName;
	int Port1;
	int Port2;
	
	public Client(InetAddress IP1, int port1, InetAddress IP2, int port2) {
		RQ = 0;
		startUp = true;
		hostName1 = IP1;
		Port1 = port1;
		hostName2 = IP2;
		Port2 = port2;
	}

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

		try {

			// InetAddress address = InetAddress.getByName(hostName);
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(10000); // set time out to 10s

			while (true) {

				// Create a Scanner object to read input.

				// need to first send
				// REGISTER RQ# Name IP Address Socket#
				if (startUp) {
					registerCall(socket);
					startUp = false;
				} else {
					commandInput(socket);
				}

				Thread.sleep(10000);
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

	private void registerCall(DatagramSocket socket) throws IOException {
		Scanner console = new Scanner(System.in);

		System.out.print("Enter your name to register with the server: ");

		String name = console.nextLine();

		clientName = name;
		
		String message1 = "REGISTER " + RQ + " " + name + " " + hostName1 + " " + Port1;
		System.out.println(message1);

		byte[] requestbuffer1 = message1.getBytes();
		
		String message2 = "REGISTER " + RQ + " " + name + " " + hostName2 + " " + Port2;
		System.out.println(message2);

		byte[] requestbuffer2 = message2.getBytes();

		// Send message to server
		DatagramPacket request1 = new DatagramPacket(requestbuffer1, requestbuffer1.length, hostName1, Port1);
		socket.send(request1);
		
		DatagramPacket request2 = new DatagramPacket(requestbuffer2, requestbuffer2.length, hostName2, Port2);
		socket.send(request2);

		// wait for response
		byte[] buffer = new byte[512];
		DatagramPacket response = new DatagramPacket(buffer, buffer.length);
		socket.receive(response); // blocking call

		// format and print response
		String serverMessage = new String(buffer, 0, response.getLength());
		System.out.println(serverMessage);

		String splitMessage[] = serverMessage.split(" ");
		RQ += 1;

		if (splitMessage[0].equals("REGISTER_DENIED")) {
			registerCall(socket);
		}

	}

	private void commandInput(DatagramSocket socket) throws IOException, InterruptedException {
		Scanner console = new Scanner(System.in);
		String input;

		while (true) {
			// message = "ECHO";

			System.out.print("Enter the next command to be sent: ");

			input = console.nextLine();

			String splitInput[] = input.split(" ");

			// case statement of input and sending to server
			switch (splitInput[0]) {

			case "DE-REGISTER":
				deRegisterClient(socket);
				break;
			}

		}

	}

	private void deRegisterClient(DatagramSocket socket) {

		String sendingMessage = "DE-REGISTER " + RQ + " " + clientName;
		System.out.println(sendingMessage);

		byte[] requestbuffer = sendingMessage.getBytes();

		// Send message to server
		DatagramPacket request = new DatagramPacket(requestbuffer, requestbuffer.length, hostName1, Port1);
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
		// if Deregister is sucessful

		if (splitMessage[0].equals("DE-REGISTER")) {
			System.out.print("DeRegister of name: " + splitMessage[2] + "Successful");
			startUp = true;
			RQ += 1;
		}

	}
}
