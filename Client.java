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
				/*
				ASSUMPTION: Client will only receive JOBN JCPL or NONE; job type will always be 4 char long
				*/
				String jobType = msg.substring(0, 3); // jobType save type of job i.e. JOBN JCPL or NONE
				System.out.println("DS-Server: " + msg);

				//find largest server??
				//String[] serverSortArr = msg.split(" ");

				//11 send GETS ALL
				dout.write("GETS All\n".getBytes());
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

				
				for(int i = 0; i < nRecs; i++){
					//receive each record
					msg = in.readLine();
					System.out.println(msg);

					//keep track of largest server type & number of server of that type

					// split into:       serverType serverID state curStartTime core memory disk ...
					temp = msg.split(" ");

					if(Integer.parseInt(temp[4]) > mostCores){ //if this server is bigger than the one on record
						
						/*
						ASSUMPTION: all server types have unique number of cores; server is either a bigger server or the largest server on record.
						*/

						largestServerType = temp[0];//update the largest server type
						mostCores = Integer.parseInt(temp[4]); //update the cores to beat to become biggest server
						numberOfLargestServers = 1; //update number of largest servers (counting the current one)


					} else numberOfLargestServers += 1 ; //server is the one on record. Number of largest server += 1
				}

				//18
				dout.write("OK\n".getBytes()); //send OK
				dout.flush();

				//19 
				msg = in.readLine(); //receive msg
				System.out.println(msg);

				//20
				if(jobType.contains("JOBN")){ //if msg at step 10 is JOBN
					dout.write("SCHD\n".getBytes()); //send SCHD
					dout.flush();
				} else{
					dout.write("REDY\n".getBytes()); //send REDY for next instructions
				    dout.flush();
					msg = in.readLine();
				}
			}

			//24-25
			dout.write(("QUIT\n").getBytes()); //send QUIT
			dout.flush();
			
			msg = in.readLine(); // recieves QUIT
			System.out.println("DS-Server: " + msg);
			

			//26 close socket
			dout.close();
			s.close();
		}

		catch (Exception e) {
			System.out.println(e);
		}
	}
}
