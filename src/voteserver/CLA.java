package voteserver;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class CLA {

	private static CLA myInstance = null;
	private CLA(){}
	
	
	public static CLA getInstance()
	{
		if (myInstance == null)
		{
			myInstance = new CLA();
		}
		
		return myInstance;
	}
	
	private void startListening()
	{
		
	}
}

class voteRequestListener
{
	@SuppressWarnings("unused")
	private Runnable vRListener(final String host,final int myListeningPort)
	{
		
		Runnable vrlRunnable = new Runnable()
		{
			ObjectInputStream dInStream = null;
			Socket myListeningSocket;
			VoteRequest recievedRequest;
			
			@Override
			public void run() {
							
				try {
					myListeningSocket = new Socket(host,myListeningPort);
					dInStream = new ObjectInputStream(myListeningSocket.getInputStream());
				} catch (IOException e1) {
					e1.printStackTrace();
					System.err.println("Unable to bind to socket at port " + myListeningPort + 
							" and get input stream. Stopping VRListerner. Stopping application.");
					System.exit(0);
				}
				
				while (true)
				{
					try {
						recievedRequest = (VoteRequest) dInStream.readObject();
						
						//test receiver
						System.out.println(recievedRequest.toString());
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			}
			
			
		}; 
		
		return null;		
	}
}