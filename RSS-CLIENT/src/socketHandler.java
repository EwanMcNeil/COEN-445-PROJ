import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

//
//Sockethandler is needed because it needs to actively listen to the servers messages in case of a 
//publish from another client
//

public class socketHandler extends Thread {
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

			printSem.release();

			String splitMessage[] = message.split(" ");

			String command = splitMessage[0].toUpperCase().replace("_", "-");

			System.out.print("Client has received: ");
			System.out.println(message);

			switch (command) {
			case "REGISTER":
			case "REGISTERED":
				registered(packet, splitMessage);
				client.registered = true;
				// regClientSem.release();
				client.response.release();
				break;

			case "DE-REGISTER":
				client.registered = false;
				client.response.release();
				break;

			case "ECHO":
				// echoSem.release();
				client.response.release();
				break;

			case "SUBJECTS":
			case "SUBJECTS-UPDATED":
				client.subjects.add(splitMessage[3]);
				client.subjectsBool = true;
				// upSubSem.release();
				client.response.release();
				break;

			case "REGISTER-DENIED":
				client.registered = false;
				// regClientSem.release();
				client.response.release();
				break;

			case "UPDATE":
			case "UPDATE-CONFIRMED":
				updatedApproved(packet, splitMessage);
				client.clientUpdate = true;
				// upClientSem.release();
				client.response.release();
				break;

			case "UPDATE-DENIED":
				client.clientUpdate = false;
				// upClientSem.release();
				client.response.release();
				break;

			case "SUBJECTS-REJECTED":
				client.subjectsBool = false;
				// upSubSem.release();
				client.response.release();
				break;
			case "PORT-ERROR":
				client.startUp = true;
				client.portError = true;
				client.response.release();
				break;
			case "PUBLISH-DENIED":

				if (splitMessage[2].equals("PORT_ERROR")) {
					client.startUp = true;
					client.portError = true;
				} else {
					client.publish = false;
				}

				// publishSem.release();
				client.response.release();
				break;

			case "CHANGE-SERVER":
				try {
					if (splitMessage[1].contains("/")) {
						String hostName[] = splitMessage[1].split("/");
						client.currentHost = InetAddress.getByName(hostName[1]);
					} else {
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
				System.out.println("you have received a new message from user:");
				System.out.println(splitMessage[1]);
				System.out.println("about your shared topic of:");
				System.out.println(splitMessage[2]);
				System.out.println("message is:");

				int count = 3;
				String messageRec = " ";
				while (count < splitMessage.length) {
					messageRec = messageRec + " " + splitMessage[count];
					count = count + 1;
				}

				System.out.println(messageRec);
				printSem.release();

				break;

			case "THERE":
				try {
					client.service();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			default:
				System.out.println("Server response not recognized.");
				break;
			}
		}
	}

	public void registered(DatagramPacket requestPacket, String splitMessage[]) {
		client.currentHost = requestPacket.getAddress();
		client.currentPort = requestPacket.getPort();
	}

	public void updatedApproved(DatagramPacket requestPacket, String splitMessage[]) {

		client.RQ = Integer.parseInt(splitMessage[1]);
		client.currentHost = requestPacket.getAddress();
		client.currentPort = requestPacket.getPort();
	}

	/*
	 * public void updatedDenied(DatagramPacket requestPacket, String
	 * splitMessage[]) {
	 * 
	 * // TODO
	 * 
	 * }
	 * 
	 * public void subjectsUpdated(DatagramPacket requestPacket, String
	 * splitMessage[]) {
	 * 
	 * }
	 *
	 *
	 *
	 * public void deRegister(DatagramPacket requestPacket, String splitMessage[]) {
	 * 
	 * client.registered = false; //client.deRegClientSem.release();
	 * 
	 * client.response.release(); }
	 */

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
