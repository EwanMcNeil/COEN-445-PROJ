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
	InetAddress hostName;
	String clientName;
	int Port;

	public Client(InetAddress IP, int port) {

		RQ = 0;
		startUp = true;
		hostName = IP;
		Port = port;
	}

	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("Missing Input");
			return;

		}

		String hostname = args[0]; // IPaddress
		int port = Integer.parseInt(args[1]);

		try {
			Client client = new Client(InetAddress.getByName(hostname), port);
			System.out.println("Starting Client connected to host: " + hostname + "on port: " + port);
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
		String message = "REGISTER " + RQ + " " + name + " " + hostName + " " + Port;
		System.out.println(message);

		byte[] requestbuffer = message.getBytes();

		// Send message to server
		DatagramPacket request = new DatagramPacket(requestbuffer, requestbuffer.length, hostName, Port);
		socket.send(request);

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

			// case statment of input and sending to server
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
		DatagramPacket request = new DatagramPacket(requestbuffer, requestbuffer.length, hostName, Port);
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
