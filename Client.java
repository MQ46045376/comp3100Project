import java.io.*;
import java.net.*;

public class Client {

	private final static int SocketNumber = 50000; // change this to desired socket number
	private final static String UserName = "someUsername"; //change this to username
	
	
	public static void main(String[] args) {
		// The commented numbers are in reference to the lines in LRR sudo code
		// LRR = Largest Round Robin
		try {
			//1,2,3
			// Create socket, init in/out streams and connect to ds-server
			Socket s = new Socket("localhost", SocketNumber);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			
			//4,5
			dout.write("HELO\n".getBytes()); //send HELO
			dout.flush(); //Design choice: flushing after every write to ensure nothing messes up in output stream
			
			String msg; //msg holds the last msg sent by server
			msg = in.readLine(); //receive OK
			System.out.println("DS-Server: " + msg); 
			
			//6: send AUTH username
			dout.write(("AUTH " + UserName + "\n").getBytes());
			dout.flush();

			// 7: Receive OK
			msg = in.readLine();
			System.out.println("DS-Server: " + msg);

			// 8
			while(!msg.contains("NONE")){
				//9 send REDY
				dout.write("REDY\n".getBytes());
				dout.flush();

				//10 receive JOBN JCPL NONE
				msg = in.readLine();
				String jobType = msg; // jobType save type of job i.e. JOBN JCPL or NONE
				System.out.println("DS-Server: " + msg);

				//find largest server??
				//String[] serverSortArr = msg.split(" ");

				//11 send GETS ALL
				dout.write("GETS ALL\n".getBytes());
				dout.flush();


				//12 receive DATA nRec recSize // e.g. DATA 5 124
				msg = in.readLine();
				System.out.println("DS-Server: " + msg);

				String[] temp = msg.split(" "); // stores [DATA] [nRec] [recSize] temporarily
				int nRecs = Integer.parseInt(temp[2]); //   get    ^nRec and turn it into int

				//13 
				dout.write("OK\n".getBytes()); //send OK
				dout.flush();

				//14
				String largestServerType; //stores largest server type
				int mostCores = -1; //store the largest core size; more cores = bigger server
				int numberOfLargestServers = 0; //stores number of that server

				/*
				ASSUMPTION: all server types have unique number of cores; there won't be two different server types with same amount of cores
				*/
				for(int i = 0; i < nRecs; i++){
					//receive each record
					msg = in.readLine();
					System.out.println(msg);

					//keep track of largest server type & number of server of that type
					// split into serverType serverID state curStartTime core memory disk ...
					temp = msg.split(" ");

					if(Integer.parseInt(temp[4]) > mostCores){ //if this server is bigger than the one on record
						
						
						numberOfLargestServers = 1;

					}




				}







				

			}
			dout.write(("QUIT\n").getBytes());
			// recieves quit
			dout.close();
			s.close();
		}

		catch (Exception e) {
			System.out.println(e);
		}
	}
}
			// Create socket, init in/out streams and connect to ds-server
			Socket s = new Socket("localhost", SocketNumber);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			
			//4,5,6
			dout.write("HELO\n".getBytes()); //send HELO
			dout.flush(); //Design choice: flushing after every write to ensure nothing messes up in output stream
			
			String msg; //msg holds the last msg sent by server
			msg = in.readLine(); //receive OK
			System.out.println("DS-Server: " + msg); 
			
			//if (msg.contains("OK")) { //should contain OK anyway
				dout.write(("AUTH " + UserName + "\n").getBytes());
				dout.flush();
			//}
