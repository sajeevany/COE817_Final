package voteserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
		new Thread(vRListener.getVRListener()).start();
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
			ObjectInputStream oInStream = null;
			ObjectOutputStream oOutStream = null;
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
					
					oInStream = new ObjectInputStream(new BufferedInputStream(myIOSocket.getInputStream()));
					oOutStream = new ObjectOutputStream(new BufferedOutputStream(myIOSocket.getOutputStream()));
									
				} catch (IOException e1) {
					e1.printStackTrace();
					System.err.println("Unable to bind to socket at port " + myListeningPort + 
							" and get input stream. Stopping VRListerner. Stopping application.");
					System.exit(0);
				}
				
				while (true)
				{
					try {
						recievedRequest = (VoteRequest) oInStream.readObject();
						
						//test receiver
						System.out.println(recievedRequest.toString());
						break;
						
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			}
			
			
		}; 
		
		return vrlRunnable;		
	}
	
	public Runnable getVRListener()
	{
		return this.myVRListener;
	}
	
}