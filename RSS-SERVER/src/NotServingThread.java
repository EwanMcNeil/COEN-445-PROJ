import java.net.DatagramSocket;
import java.util.Scanner;

public class NotServingThread extends Thread {
		
	Server server;
	DatagramSocket socket;
	
		public NotServingThread(Server server,DatagramSocket socket) {
			
			this.server = server;
			this.socket = socket;
			System.out.print("notServingThreadCreated");
		}
		
			
			@Override
		    public void run(){
		    	String input;
				Scanner console = new Scanner(System.in);
		    	System.out.print("Enter the next command to be sent: (UPDATE-SERVER)");
		    	
				input = console.nextLine();

				String splitInput[] = input.split(" ");

				String command = splitInput[0].toUpperCase().replace("_", "-");
				if(command == "UPDATE-SERVER" && splitInput.length == 3) {
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