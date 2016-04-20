package voteserver;

import encryption.JEncrypDES;
import encryption.JEncryptRSA;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

public class CLA //implements Runnable
{
    //2. Must check to see if the client has voted already. If they haven't voted yet, then give them a validation number.
    
	//TODO make singleton
	
        static String algorithm = "RSA";
   static RSAPrivateKey privKey;
    static PublicKey pubKey;
    static String publicKeyString = "public_key_Client";
   static String privateKeyString = "private_key_Server";
   static String desAlgorithm = "DES";
   static String keyString = "des_key";
    static SecretKey secretKey;
    
    private static void getKeysFromFiles(){ //Gets the RSA keys.
                    
            //Get public key from file.
        try {
            FileInputStream keyfis = new FileInputStream(publicKeyString);
            byte[] encKey = new byte[keyfis.available()];  
            keyfis.read(encKey);
            keyfis.close();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            pubKey = keyFactory.generatePublic(pubKeySpec);
            //System.out.println(pubKey);
            
            //Get private key from file.
            FileInputStream keyfis2 = new FileInputStream(privateKeyString);
            byte[] encKey2 = new byte[keyfis2.available()];  
            keyfis2.read(encKey2);
            keyfis2.close();
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey2);
            KeyFactory keyFactory2 = KeyFactory.getInstance(algorithm);
             privKey = (RSAPrivateKey)keyFactory2.generatePrivate(privKeySpec);
            //System.out.println(privKey);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    
    public static byte[] getRequestToVote() {
        
        return new byte[0];
    }
    
    public static byte[] test2(byte[] encrypted) {
        
        if (secretKey == null) {
            getDesKeysFromFiles();
            //System.out.println("Keys were null");
        }
        
        String fromClient = new String(JEncrypDES.decryptDES(encrypted, secretKey));
        System.out.println(fromClient);
        
        byte[] toClient = JEncrypDES.encryptDES("I gotcha bud.", secretKey);
        //System.out.println("Test 2");
        return toClient;
    }
    
    	//TODO 
    	//return E(pubKey, secretKey)||E(secretKey, validation number)
        public static ArrayList<byte[]> getAuthenticatedSessionKey(byte[] encryptedString) { //Generates a new DES key and sends that to the client.
            
            if (pubKey == null && privKey == null && secretKey == null) {
            	getKeysFromFiles();
            try {
                secretKey = JEncrypDES.desKeyGen();
            } catch (Exception e) {
                
            }
            //System.out.println("Keys were null");
        }
            
            byte[] toClient = JEncryptRSA.encrypt(pubKey, secretKey.getEncoded(), algorithm);
            //System.out.println("Start");
            return null;
        }

    private static void getDesKeysFromFiles() { //Here just in case.
        try {
        FileInputStream keyfis = new FileInputStream(keyString);
            byte[] encKey = new byte[keyfis.available()];  
            keyfis.read(encKey);
            keyfis.close();
            SecretKeySpec keySpec = new SecretKeySpec(encKey, desAlgorithm);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(desAlgorithm);
            secretKey = keyFactory.generateSecret(keySpec);
            //System.out.println(secretKey);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}

