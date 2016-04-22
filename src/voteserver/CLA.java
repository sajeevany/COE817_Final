package voteserver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.crypto.SecretKey;

import encryption.JEncrypDES;
import encryption.JEncryptRSA;

public class CLA // implements Runnable
{
	// 2. Must check to see if the client has voted already. If they haven't
	// voted yet, then give them a validation number.

	static String algorithm = "RSA";
	static RSAPrivateKey privKey;
	static PublicKey pubKey;
        static PublicKey personalPublicKey;
	static String publicKeyString = "public_key_Client";
	static String privateKeyString = "private_key_Server";
        static String publicServerString = "public_key_Server";
	static String desAlgorithm = "DES";
	static String keyString = "des_key";
	static SecretKey secretKey;
	private static CLA instance = null;
	private String clientId;
	private String password;
	private String validationNumber;
	private final String clientList = "clientList";
	private final String voteLog = "VoteLog.txt";
	private Map<String, String> userMap = new HashMap<String, String>();

	// Singleton object initialization
	private CLA(){};
	
	public static CLA getInstance() {

		if (instance == null) {
			instance = new CLA();
		}
		return instance;
	}
	
	
	public ArrayList<String> getUAV() {

		ArrayList<String> toCTF = new ArrayList();
		toCTF.add(this.clientId);
		toCTF.add(this.password);
		toCTF.add(this.validationNumber);

		return toCTF;
	}

	public byte[] getEncryptedSecretKey() {
            
        byte[] key = JEncryptRSA.encrypt(personalPublicKey, secretKey.getEncoded(), algorithm);  
		return key;
	}

	private static void getKeysFromFiles() { // Gets the RSA keys.

		// Get public key from file.
		try {
			FileInputStream keyfis = new FileInputStream(publicKeyString);
			byte[] encKey = new byte[keyfis.available()];
			keyfis.read(encKey);
			keyfis.close();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
			pubKey = keyFactory.generatePublic(pubKeySpec);
			// System.out.println(pubKey);

			// Get private key from file.
			FileInputStream keyfis2 = new FileInputStream(privateKeyString);
			byte[] encKey2 = new byte[keyfis2.available()];
			keyfis2.read(encKey2);
			keyfis2.close();
			PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey2);
			KeyFactory keyFactory2 = KeyFactory.getInstance(algorithm);
			privKey = (RSAPrivateKey) keyFactory2.generatePrivate(privKeySpec);
                        
            FileInputStream keyfis3 = new FileInputStream(publicServerString);
			byte[] encKey3 = new byte[keyfis3.available()];
			keyfis3.read(encKey3);
			keyfis3.close();
			X509EncodedKeySpec pubKeySpec2 = new X509EncodedKeySpec(encKey3);
			KeyFactory keyFactory3 = KeyFactory.getInstance(algorithm);
			personalPublicKey = keyFactory.generatePublic(pubKeySpec2);
			// System.out.println(privKey);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}


	// return E(pubKey, secretKey)||E(secretKey, validation number)
	public ArrayList<byte[]> getAuthenticatedSessionKey(byte[] encryptedString) throws IOException { 

		if (pubKey == null && privKey == null) {
			getKeysFromFiles();
		}
		if (secretKey == null) {

			try {
				secretKey = JEncrypDES.desKeyGen();

			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
		// gets Username and pw sent by client, decrypts using private key
		String fromClient = new String(JEncryptRSA.decrypt(privKey, encryptedString, algorithm));
		this.clientId = fromClient.substring(0, fromClient.indexOf("@"));
		this.password = fromClient.substring(fromClient.indexOf("@") + 1, fromClient.length());
		boolean checked = false;

		try {
			// Check if Username and pw match, stored values
			checked = checkUserInfo(clientId, password);
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		// Check if user has already voted 
		boolean haveTheyVotedYet = true;
		if (checked) {
			haveTheyVotedYet = checkIfTheyHaveVoted(this.voteLog, clientId);
		} else {
			return null;
		}

		ArrayList<byte[]> toClient = new ArrayList();
		// if they have not voted set validation number to 1 and give client secret key value (encrypted with des) 
		if (!haveTheyVotedYet) {
			byte[] encryptedDESKey = JEncryptRSA.encrypt(pubKey, secretKey.getEncoded(), algorithm);
			// Generate validation number for client
			Random rand = new Random();
			int  n = rand.nextInt(10) + 1;
			this.validationNumber = String.valueOf(n);
			//Encrypt validation number using DES/sessionkey
			byte[] validationNumber = JEncrypDES.encryptDES(this.validationNumber, secretKey);
			// give session key and validation number to client 
			toClient.add(encryptedDESKey);
			toClient.add(validationNumber);
			return toClient;
		} else {
			// If they have already voted set validation number to 0
			byte[] encryptedDESKey = JEncryptRSA.encrypt(pubKey, secretKey.getEncoded(), algorithm);
			byte[] validationNumber = JEncrypDES.encryptDES("0", secretKey);
			toClient.add(encryptedDESKey);
			toClient.add(validationNumber);
			this.validationNumber = "0";
			return toClient;
		}

	}

	// Validate user name and password
	private boolean checkUserInfo(String clientId, String password) throws Exception {

		this.userMap = getPopulatedClientMap(clientList);
		if (userMap.containsKey(clientId)) {
			String expectedPassword = userMap.get(clientId);

			if (password.equals(expectedPassword)) {
				return true;
			} else {
				return false;
			}
		}

		return false;
	}

	// returns hashmap of clientIDs and password
	private Map<String, String> getPopulatedClientMap(String clientListFileName) throws IOException {
		HashMap<String, String> userPasswordMap = new HashMap<>();

		BufferedReader clientListReader = new BufferedReader(new FileReader(clientListFileName));
		String line = null;

		while ((line = clientListReader.readLine()) != null) {
			StringTokenizer sTok = new StringTokenizer(line, ",");
			userPasswordMap.put(sTok.nextToken(), sTok.nextToken());
		}

		clientListReader.close();

		return userPasswordMap;
	}

	// validate if user has voted.
	private boolean checkIfTheyHaveVoted(String voteLogFileName, String clientID) throws IOException {

		BufferedReader clientListReader = new BufferedReader(new FileReader(voteLogFileName));
		String line = null;

		while ((line = clientListReader.readLine()) != null) {
			StringTokenizer sTok = new StringTokenizer(line, ",");

			if (sTok.nextToken().equals(clientID)) {
				return true;
			}
		}

		clientListReader.close();

		return false;
	}

}
