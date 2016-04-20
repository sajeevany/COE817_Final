package client;

import encryption.JEncrypDES;
import encryption.JEncryptRSA;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

import voteserver.CLA;
import voteserver.VoteRequest;


public class Client// implements Runnable
{
	static String algorithm = "RSA";
	static RSAPrivateKey privKey;
	static PublicKey pubKey;
	static String publicKeyString = "public_key_Server";
	static String privateKeyString = "private_key_Client";
	static String desAlgorithm = "DES";
	static String keyString = "des_key";
	static SecretKey secretKey;
    
    public static void main(String[] args) throws IOException {
        //1. Send a request to vote to the CLA.
        //3. Get the validation number from the CLA.
        //4. Cast vote to CTF
        //6. Read response from CTF. Close program.
        //getDesKeysFromFiles();
        getKeysFromFiles();
        
        //handshake
        String myUserInfo = "Client1@Password";
        byte[] encryptedString = JEncryptRSA.encrypt(pubKey, myUserInfo.getBytes(), algorithm);
        
        //TODO get secret key and the validation number
        ArrayList<byte[]> fromCLA1 = CLA.getAuthenticatedSessionKey(encryptedString);
        
        //TODO decrypt secret key and vNum
	        //byte[] desKey = JEncryptRSA.decrypt(privKey, fromCLA1, algorithm);
	        //secretKey = new SecretKeySpec(desKey, 0, desKey.length, desAlgorithm);
        
        //comms
        //TODO if vNUm = 0 fail fast
        
        //TODO if vNum = ++ able to vote
        
        //TODO create voterequest as byte array
        
        //TODO encrypt VR with shared key
        
        //TODO send encrypted VR to CTF
        
        //TODO Listen for response + close
        
        /*
        byte[] toCLA1 = JEncrypDES.encryptDES("Gumi is love. Gumi is life.", secretKey);
        byte[] fromCLA2 = CLA.test2(toCLA1);
        String fromCLAString = new String(JEncrypDES.decryptDES(fromCLA2, secretKey));
        System.out.println(fromCLAString);*/
    }
	
   private  static void getKeysFromFiles(){
                    
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
