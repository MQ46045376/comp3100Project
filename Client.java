import java.io.*;
import java.net.*;

public class Client {

	private final static int SocketNumber = 50000; // change this to desired socket number
	private final static String UserName = "someUsername"; // change this to username

	public static void main(String[] args) {
		// The commented numbers are in reference to the lines in LRR sudo code
		// LRR = Largest Round Robin
		try {
			// 1,2,3
			// Create socket, init in/out streams and connect to ds-server
			Socket s = new Socket("localhost", SocketNumber);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());

			// 4,5
			dout.write("HELO\n".getBytes()); // send HELOS
			System.out.println("Me: HELO");
			dout.flush(); // Design choice: flushing after every write to ensure nothing messes up in
							// output stream

			String msg; // msg holds the last msg sent by server
			msg = in.readLine(); // receive OK
			System.out.println("DS-Server: " + msg);

			// 6: send AUTH username
			dout.write(("AUTH " + UserName + "\n").getBytes());
			System.out.println("Me: " + "AUTH " + UserName + "\n");
			dout.flush();

			// 7: Receive OK
			msg = in.readLine();
			System.out.println("DS-Server: " + msg);

			int jobCounter = 0; //Count no. of jobs done (for LRR implementation)

			// 8
			while (!msg.contains("NONE")) {

				// 9 send REDY
				dout.write("REDY\n".getBytes());
				System.out.println("Me: REDY");
				dout.flush();

				// 10 receive JOBN JCPL NONE
				msg = in.readLine();
				if (msg.contains("NONE")) { // just in case
					break;
				}
				/*
				 * ASSUMPTION: Client will only receive JOBN JCPL or NONE; job type will always
				 * be 4 char long
				 */

				//       JOBN submitTime jobID estRuntime core memory disk
				// index: 0       1        2        3      4     5     6
				String[] jobType = msg.split(" "); // jobType save type of job i.e. JOBN JCPL or NONE
				System.out.println("DS-Server: " + msg);

				// String[] serverSortArr = msg.split(" ");

				// 11 send GETS ALL
				dout.write("GETS All\n".getBytes());
				System.out.println("Me: GETS All");
				dout.flush();

				// 12 receive DATA nRec recSize // e.g. DATA 5 124
				msg = in.readLine();
				System.out.println("NOte: Should receive DATA now");
				System.out.println("DS-Server: " + msg);

				String[] temp = msg.split(" "); // stores [DATA] [nRec] [recSize] temporarily
				int nRecs = Integer.parseInt(temp[1]); // get ^nRec and turn it into int

				// 13
				dout.write("OK\n".getBytes()); // send OK
				dout.flush();

				// 14 FINDING LARGEST SERVER
				String largestServerType = ""; // stores largest server type
				int mostCores = -1; // store the largest core size; more cores = bigger server
				int numberOfLargestServers = 0; // stores number of that server
				String serverID = ""; //stores serverIDs of the largest servers separated by spaces
				//i.e. serverID1 serverID2 ...

				for (int i = 0; i < nRecs; i++) {
					// receive each record
					msg = in.readLine();
					System.out.println(i + " " + msg);

					// keep track of largest server type & number of server of that type

					// split into: serverType serverID state curStartTime core memory disk ...
					// index           0           1     2        3         4    5     6 ...
					temp = msg.split(" ");

					if (Integer.parseInt(temp[4]) > mostCores) { // if this server is bigger than the one on record

						/*
						 * ASSUMPTION: all server types have unique number of cores; server is either a
						 * bigger server or the largest server on record.
						 */

						largestServerType = temp[0];// update the largest server type
						mostCores = Integer.parseInt(temp[4]); // update the cores to beat to become biggest server
						numberOfLargestServers = 1; // update number of largest servers (counting the current one)
						serverID = ""; //clear serverID list
						serverID += temp[1]; //add the ID of current server on serverID list

					} else if (Integer.parseInt(temp[4]) == mostCores) { // server is the one on record. Number of															
						numberOfLargestServers += 1; // largest server += 1

						//add ID if serverID here is unique
						if(!serverID.contains(temp[1])){
							serverID = serverID + " " + temp[1];
						}
					} // server is smaller than the one on record: nothing happens. look at next
						// server
				}
				System.out.println("serverID: " + serverID); //DEBUG
				//note: looks like 9/10 iterations have only 1 largest server. 
				//if that's the case, no wonder LRR is not apparent

				// 18
				dout.write("OK\n".getBytes()); // send OK
				dout.flush();

				// 19
				msg = in.readLine(); // receive msg
				System.out.println(msg);

				String[] listID = serverID.split(" "); //contains serverIDs of largest type
				//i.e. [serverID1][serverID2][serverID3]...

				// 20 Job scheduling
				// NEED TO IMPLEMENT LRR
				if (jobType[0].contains("JOBN")) { // if msg at step 10 is JOBN

					//Calculate serverID to use for LRR
					int serverToUse = jobCounter% listID.length; //gives idx to use
					System.out.println("Server to use = " + serverToUse);
					System.out.println("Jobcounter: " + jobCounter);

					// SCHD: jobID serverType serverID
					// JOBN: submitTime jobID estRuntime core memory disk
					dout.write(("SCHD" + " " + jobType[2] + " " + largestServerType + " " + listID[serverToUse] + "\n").getBytes()); 
					// send SCHD jobID serverID

					System.out.println("Me: " + "SCHD" + " " + jobType[2] + " " + largestServerType + " " + listID[serverToUse]);
					dout.flush();

					// Receive "OK" for SCHD
					msg = in.readLine();
					System.out.println("DS-Server: " + msg);

					jobCounter++ ;// update job counter
				}
			}
			// 24-25
			dout.write(("QUIT\n").getBytes()); // send QUIT
			System.out.println("Me: QUIT");
			dout.flush();

			msg = in.readLine(); // recieves QUIT
			System.out.println("DS-Server: " + msg);

			// 26 close socket
			dout.close();
			s.close();

			System.out.println("Sockets closed.\n Precess terminated.");
		}

		catch (Exception e) {
			System.out.println(e);
		}
	}
}
