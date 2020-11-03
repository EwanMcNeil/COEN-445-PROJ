import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

//ClientHandler class 
class ClientHandler extends Thread {

	final DatagramSocket s;
	Boolean startUp;
	DatagramPacket request;
	final int count;
	int RQ;

	// Constructor
	public ClientHandler(DatagramSocket s, DatagramPacket request, int count, String Name, Server server) {
		this.s = s;
		this.request = request;
		this.count = count;
		setName(Name);
		this.RQ = 0;
		startUp = true;

	}

	@Override
	public void run() {
		String received;
		String toreturn;
		System.out.println("Started new Client Thread");

		// server can get the info about the client
		InetAddress clientAddress = request.getAddress();
		int clientPort = request.getPort();

		while (true) {
			try {

				String message = formatMessage(request.getData()).toString();
				String splitMessage[] = message.split(" ");

				if (startUp) {
					// REGISTERED RQ#
					message = "REGISTERED " + RQ;
					byte[] buffer = message.getBytes();
					DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
					s.send(response);

					startUp = false;
					RQ += 1;
				} else {
					if (splitMessage[2].equals(this.getName())) {
						int check = Integer.parseInt(splitMessage[1]);

						if (RQ == check) {

							// switch(splitMessage[0]) {

							message = "client: " + getName() + " on thread: " + count + " has sent: " + splitMessage[5];
							System.out.println(message);

							String responseMSG = "Response to: " + splitMessage[5];

							byte[] buffer = responseMSG.getBytes();

							DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress,
									clientPort);
							s.send(response);

							RQ += 1;
							// }
						}
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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