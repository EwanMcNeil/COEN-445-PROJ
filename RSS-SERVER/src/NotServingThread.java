import java.net.DatagramSocket;
import java.util.Scanner;

public class NotServingThread extends Thread {
	Server server;
	DatagramSocket socket;

	public NotServingThread(Server server, DatagramSocket socket) {
		this.server = server;
		this.socket = socket;
	}

			
			@Override
		    public void run(){
		    	String input;
				Scanner console = new Scanner(System.in);
		    	System.out.println("Enter the next command to be sent: (UPDATE-SERVER)");
		    	
				input = console.nextLine();

				String splitInput[] = input.split(" ");

				String command = splitInput[0].toUpperCase().replace("_", "-");
				
				System.out.println(splitInput[0] + " " + splitInput[1] + " " + splitInput[2] + " length: " + splitInput.length);
				
				if(command.equals("UPDATE-SERVER") && splitInput.length == 3) {
					server.updateServer(socket,splitInput);
				}
				else {
					System.out.println("Invalid command!");
				}
				if(server.isServing) {
					Thread.interrupted();
				}
		    }

}