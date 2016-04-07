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

	private VoteRequestListener vRListener = null;
	
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
	
	public void startListening()
	{
		if (vRListener == null)
		{
			vRListener = new VoteRequestListener("localhost", 10901);
		}
		
		vRListener.startVRListener();
	}
}

class VoteRequestListener
{
	private Runnable myVRListener = null;
	
	public VoteRequestListener(String host, int myListeningPort)
	{
		myVRListener = vRListener(host, myListeningPort);
	}
	
	@SuppressWarnings("unused")
	private Runnable vRListener(final String host,final int myListeningPort)
	{
		Runnable vrlRunnable = new Runnable()
		{
			ObjectInputStream dInStream = null;
			ServerSocket myServerListeningSocket;
			Socket myIOSocket = null;
			VoteRequest recievedRequest;
			
			@Override
			public void run() {
							
				try {
					myServerListeningSocket = new ServerSocket(myListeningPort);
					System.out.println("Waiting for connection");
					
					while(myIOSocket == null)
					{
						myIOSocket = myServerListeningSocket.accept();
					}
					
					dInStream = new ObjectInputStream(myIOSocket.getInputStream());
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
		
		return vrlRunnable;		
	}
	
	public void startVRListener()
	{
		this.myVRListener.run();
	}
	
}