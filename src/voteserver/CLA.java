package voteserver;

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
	
}
