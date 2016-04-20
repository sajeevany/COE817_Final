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
import java.util.StringTokenizer;

import javax.crypto.SecretKey;

import encryption.JEncrypDES;
import encryption.JEncryptRSA;

public class CLA // implements Runnable
{
	// 2. Must check to see if the client has voted already. If they haven't
	// voted yet, then give them a validation number.

	// TODO make singleton

	static String algorithm = "RSA";
	static RSAPrivateKey privKey;
	static PublicKey pubKey;
	static String publicKeyString = "public_key_Client";
	static String privateKeyString = "private_key_Server";
	static String desAlgorithm = "DES";
	static String keyString = "des_key";
	static SecretKey secretKey;
	private static CLA instance = null;
	private String clientId;
	private String password;
	private String validationNumber;
	private final String clientList = "keyFile";
	private final String voteLog = "VoteLog.txt";
	private Map<String, String> userMap = new HashMap<String, String>();

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

	public SecretKey getEncryptedSecretKey() {
		return secretKey;
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
			// System.out.println(privKey);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	// TODO
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

		String fromClient = new String(JEncryptRSA.decrypt(privKey, encryptedString, algorithm));
		this.clientId = fromClient.substring(0, fromClient.indexOf("@"));
		this.password = fromClient.substring(fromClient.indexOf("@") + 1, fromClient.length());
		boolean checked = false;

		try {
			checked = checkUserInfo(clientId, password);
		} catch (Exception e) {
			System.out.println(e.toString());
		}

		boolean haveTheyVotedYet = true;
		if (checked) {
			haveTheyVotedYet = checkIfTheyHaveVoted(this.voteLog, clientId);
		} else {
			return null;
		}

		ArrayList<byte[]> toClient = new ArrayList();
		if (!haveTheyVotedYet) {
			byte[] encryptedDESKey = JEncryptRSA.encrypt(pubKey, secretKey.getEncoded(), algorithm);
			byte[] validationNumber = JEncrypDES.encryptDES("1", secretKey);
			toClient.add(encryptedDESKey);
			toClient.add(validationNumber);
			return toClient;
		} else {
			byte[] encryptedDESKey = JEncryptRSA.encrypt(pubKey, secretKey.getEncoded(), algorithm);
			byte[] validationNumber = JEncrypDES.encryptDES("0", secretKey);
			toClient.add(encryptedDESKey);
			toClient.add(validationNumber);
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
