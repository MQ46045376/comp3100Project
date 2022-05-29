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
                // JOBN submitTime jobID estRuntime core memory disk
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

    //Put server info and scheduling algorithm here!!!
	private static String algSchd(String[] job, BufferedReader in, DataOutputStream dout) {
		
		int serverCounter;// Number of servers on system.
		
		// 11-13 UPDATE: make case for gets avail
		// Requests available server for specific jobs
        sendMSG("GETS Avail " + job[4] + " " + job[5] + " " + job[6] + "\n", dout);
        String msgReceived = readMSG(in);
        String[] Data = parsing(msgReceived);
        sendMSG("OK\n", dout);
        
        if (Data[1].equals("0")) {// If no available servers
            sendMSG("GETS All\n", dout); //get all servers instead
            msgReceived = readMSG(in);
            msgReceived = readMSG(in);
            Data = parsing(msgReceived); //receive server info

            sendMSG("OK\n", dout);
            serverCounter = Integer.parseInt(Data[1]);
        } else {
            serverCounter = Integer.parseInt(Data[1]);
        }

        // FINDING SERVER INFO
        // MAKING SERVER ARRAY
        Server[] updatedServerList = new Server[serverCounter];

        // Loop through all servers to create server list
        for (int i = 0; i < serverCounter; i++) {
            msgReceived = readMSG(in);
            String[] updatedStringList = parsing(msgReceived);
            updatedServerList[i] = new Server(updatedStringList[0], updatedStringList[1], updatedStringList[4],
                    updatedStringList[5], updatedStringList[6], updatedStringList[7], updatedStringList[8]);
        }//now should have a list of servers 
        
        // Code stuck at '.' output from server
        // Need to catch it at end of data stream.
        sendMSG("OK\n", dout);
        msgReceived = readMSG(in);

        //DECIDING WHICH SERVER TO USE
        //TODO
        
        
        //need to catch 
        return null;	
		}	
	
	
}

