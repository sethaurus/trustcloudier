/*
*This is the Client backend.
* functions called by the front end.
* Calls arguments from TrustManager
* Comunicates with the Server. 
*
* By
* Emily Martin 21153667
* Henry
* Steven
*
*/



import java.io.*;                           // Basics for I/O operations
import javax.net.ssl.SSLSocket;             // Allow for SSL support
import javax.net.ssl.SSLSocketFactory;
import java.util.ArrayList;
import java.util.Arrays;

import java.lang.Byte;
import java.lang.Object;

public class ClientNew{

	private static String host = null;
	private static int serverPort;
	private static boolean connected = false;

	private static TCSocketFactory socketfactory;
	private static TCSocket socket;
	private static String serverConnect = null;

	public ClientNew() {
		System.setProperty("javax.net.ssl.trustStore","sslKey");
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
	}

	private static void fatalError(String message) {
		System.out.println(message);
		System.exit(-1);
	}

	private static boolean findAndSetServerDetails(String hostPort) {
		try {
			String parts[] = hostPort.split(":");

			// Create and initialize socket and port
			host = parts[0];

			// Check if port is a digit
			try {
				serverPort = Integer.parseInt(parts[1]);
			} catch (NumberFormatException e) {
				fatalError("Error: Server Port is NOT a number");
			}
		} catch (Exception ex) {
			fatalError("ERROR: Could not find a server/port.");

		}
		return true;
	}
	/*
	* Opens a connection to the server
	*
	*/
	public static void openConnection(String hostPort) {
		if (socket == null) {
			if (findAndSetServerDetails(hostPort)) {
				try {
					// Sets SSL as default
					socketfactory = new TCSocketFactory(host, serverPort);
	
					// Connects to server (may need try statement)
					socket = socketfactory.open();
	
					}
				catch(Exception e) {
					throw new RuntimeException(e);
					// fatalError("Sorry, the server at " + hostPort + " could not be reached.");
				}
			}
		}
	}
	/*
	* Do nothing. 
	*
	*/
	public static void closeConnection() {
		//... NOOP
	}

	public static void sendFile(String fileName) {

		try {
			byte[] fileBytes = TrustManager.loadFileAsBytes(fileName);
			TCUploadRequestMessage message = new TCUploadRequestMessage(fileName, fileBytes);
			socket.sendPacket(message);
			TCResponseMessage response = (TCResponseMessage) socket.readPacket();
			System.out.println(response.message);
		}
		catch(Exception e) {
			fatalError("Couldn't find the file " + fileName);
		}      
	}

	public static void fetchFile(String fileName, int c) { 
		try {
			TCDownloadRequestMessage message = new TCDownloadRequestMessage(fileName, c);
			socket.sendPacket(message);
			TCDownloadResponseMessage response = (TCDownloadResponseMessage) socket.readPacket();
			if (response.success) {
				File dest = new File(fileName);
				OutputStream output = new FileOutputStream(dest);
				int bytesRead;
				output.write(response.payload);
			} else {
				System.out.println(response.message);
			} 
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void listFiles() {
		try {
			TCListRequestMessage message = new TCListRequestMessage();
			socket.sendPacket(message);
			TCListResponseMessage response = (TCListResponseMessage) socket.readPacket();
			
			TCFileList fileList = response.files;
			System.out.println(fileList.toString());
	    } catch(Exception ex) {
	    	fatalError("Could not list the files.");
	    }
	}

	public static void vouchForFile(String fileName, String keyName) {
		byte[] sigBytes = null;
		String certFileName = keyName + ".crt";
		try {
			// sendFile(certFileName);
			TrustManager manager = new TrustManager();
			File file = new File(fileName);
			String sigFileName = manager.createSignature(file, keyName).getName();
			sigBytes = TrustManager.loadFileAsBytes(sigFileName);
		} catch(Exception ex) {
			fatalError("Error: couldn't load keys for identity " + keyName);
		}
		try {
			TCVouchRequestMessage message = new TCVouchRequestMessage(fileName, certFileName, sigBytes);
			System.out.println("Sending vouch-request.");
			socket.sendPacket(message);
			TCResponseMessage response = (TCResponseMessage) socket.readPacket();
			System.out.println(response.message);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void main(String args[]) {
		openConnection("localhost:19999");
		System.out.println("Connected");
		fetchFile("kittens.jpg", 1);
		closeConnection();
		System.out.println("Disconected");
	}

}