package ds_sim_Client;

import java.io.*;
import java.net.*;

import ds_sim_Client.Client.Server;

public class Client {

	private final static int SocketNumber = 50000; // change this to desired socket number
	private final static String UserName = "someUsername"; // change this to username
	
    // Server Class to save the info of the servers
    public static class Server implements Comparable<Server> {
        String type = "";
        int id, core, mem, disk, waitingJob, runningJob;
        
        public Server(String type, String id, String core, String mem, String disk, String waitingJob,
                String runningJob) {
            this.type = type;
            this.id = Integer.parseInt(id);
            this.core = Integer.parseInt(core);
            this.mem = Integer.parseInt(mem);
            this.disk = Integer.parseInt(disk);
            this.waitingJob = Integer.parseInt(waitingJob);
            this.runningJob = Integer.parseInt(runningJob);
        }

        public String getType() {
            return type;
        }
        public void setType(String newType) {
            this.type = newType;
        }

        public String getID() {
            return Integer.toString(id);
        }
        public void setID(String newID) {
            this.id = Integer.parseInt(newID);
        }

        public String getCore() {
            return Integer.toString(core);
        }      
        public void setCore(String newCore) {
            this.core = Integer.parseInt(newCore);
        }

        public String getMem() {
            return Integer.toString(mem);
        }       
        public void setMem(String newMem) {
            this.mem = Integer.parseInt(newMem);
        }

        public String getDisk() {
            return Integer.toString(disk);
        }        
        public void setDisk(String newDisk) {
            this.disk = Integer.parseInt(newDisk);
        }

        public String getWaitingJobs() {
            return Integer.toString(waitingJob);
        }       
        public void setWaitingJobs(String newWaiting) {
            this.waitingJob = Integer.parseInt(newWaiting);
        }

        public String getRunningJobs() {
            return Integer.toString(runningJob);
        }
        public void setRunningJobs(String newRunning) {
            this.waitingJob = Integer.parseInt(newRunning);
        }

        @Override // sort by core then type ascening order.
        public int compareTo(Client.Server cs) {
            if (this.core - cs.core == 0) {
                return cs.type.compareTo(this.type);
            }
            return this.core - cs.core;
        }
    }
	
    //Function for parsing
	public static String[] parsing(String raw) {
        String delim = "[ ]+"; // set the space as the splitting element for parsing messages.
        String[] Split = raw.split(delim);
        return Split;
    }
	
	// Function to read messages
    public static String readMSG(BufferedReader in) throws IOException {
        String message = in.readLine();
        System.out.println("Server: " + message);
        return message;
    }
    
    // Fucntion to send messages
    public static void sendMSG(String msg, DataOutputStream out) {
        try {
            out.write(("Client: " + msg).getBytes());
            out.flush();
        } catch (IOException e) {
            System.out.println(e);
        }

    }
    
	
	// Inital Handshake
    public static void initHandshake(BufferedReader in, DataOutputStream out) {
        try {
            String received = ""; // holds received message from server

            sendMSG("HELO\n", out); // initiate handshake by sending HELO

            received = readMSG(in);
            if (received.equals("OK")) {
                sendMSG("AUTH Jonathan\n", out);
            } else {
                System.out.println("ERROR: OK was not received");
            }

            received = readMSG(in);
            if (received.equals("OK")) {
                sendMSG("REDY\n", out);
            } else {
                System.out.println("ERROR: OK was not received");
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

	public static void main(String[] args) {
		// The commented numbers are in reference to the lines in LRR sudo code
		// LRR = Largest Round Robin
		try {
			// 1,2,3
			// Create socket, init in/out streams and connect to ds-server
			Socket s = new Socket("localhost", SocketNumber);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());

	            // Handshake with server
	            initHandshake(in, dout);
	            String msg = readMSG(in); //stores msg from server
	            
			// 8
            while (!msg.equals("NONE")) {
                // Get job id and job type for switch statement and scheduling
                String[] job = parsing(msg); //returns parsed job info
                
                // 10 receive JOBN JCPL NONE
                switch (job[0]) {
                    // Schedule job
                //if there's a job to do
				// SCHD: jobID serverType serverID
				// JOBN: submitTime jobID estRuntime core memory disk
                    case "JOBN":
                        sendMSG("SCHD " + job[2] + " " + algSchd(job, in, dout) + "\n", dout);
                        break;
                    // If job is being completed send REDY
                    case "JCPL":
                        sendMSG("REDY\n", dout);
                        break;
                    // Ask for next job
                    case "OK":
                        sendMSG("REDY\n", dout);
                        break;
                }
                msg = readMSG(in);
            }

            sendMSG("QUIT\n", dout);

            dout.close();
            s.close();

        } catch (Exception e) {
            System.out.println(e);
        }
	}

    //Put scheduling algorithm and finding largest server here!!!
	private static String algSchd(String[] job, BufferedReader in, DataOutputStream dout) {
		while (!msg.contains("NONE")) {

			// 9 send REDY
			dout.write("REDY\n".getBytes());
			System.out.println("Me: REDY");
			dout.flush();

			
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

			// 11 send GETS ALL UPDATE: make case for gets avail
			// Requests available server for specific jobs
	        sendMSG("GETS Avail " + jobType[4] + " " + jobType[5] + " " + jobType[6] + "\n", dout);
	        String msgReceived = readMSG(in);
	        String[] Data = parsing(msgReceived);
	        sendMSG("OK\n", dout);
	        int totalServer;// Number of servers on system.

	        // If no available servers get all servers instead
	        if (Data[1].equals("0")) {
	            sendMSG("GETS All\n", dout);
	            msgReceived = readMSG(in);
	            msgReceived = readMSG(in);
	            Data = parsing(msgReceived);
	            
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

    } catch (Exception e) {
        System.out.println(e);
    }
		
		
	}
	
	
	
	
	
}
