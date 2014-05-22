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

	private static SSLSocketFactory sslsocketfactory;
	private static SSLSocket sslsocket;
	private static String serverConnect = null;

	private static OutputStream outputstream;
	private static OutputStreamWriter outputstreamwriter;
	private static BufferedWriter bufferedwriter;

	private static InputStream inputstream;
	private static InputStreamReader inputstreamreader;
	private static BufferedReader bufferedreader;

	public ClientNew(){
		System.setProperty("javax.net.ssl.trustStore","sslKey");
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
	}

	private static boolean findAndSetServerDetails(String hostPort) {

		String parts[] = hostPort.split(":");

		// Create and initialize socket and port
		host = parts[0];

		// Check if port is a digit
		try {
			serverPort = Integer.parseInt(parts[1]);
		} catch (NumberFormatException e){
			System.out.println("Error: Server Port is NOT a number");
			return false;
		}

		return true;
	}
	/*
	* Opens a conection to the server
	*
	*/
	public static void openConection(String hostPort){
		if(findAndSetServerDetails(hostPort)){

			try {

				// Sets SSL as default
				sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

				// Connects to server (may need try statement)
				sslsocket = (SSLSocket) sslsocketfactory.createSocket(host, serverPort);

				outputstream = sslsocket.getOutputStream();
				outputstreamwriter = new OutputStreamWriter(outputstream);
				bufferedwriter = new BufferedWriter(outputstreamwriter);


				inputstream = sslsocket.getInputStream();
				inputstreamreader = new InputStreamReader(inputstream);
				bufferedreader = new BufferedReader(inputstreamreader);

				// complete handshake
				bufferedwriter.write("Hello\n");
				bufferedwriter.flush();
				}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	/*
	* Closes the connection if the conection is open. 
	*
	*/
	public static void closeConection(){
		if (sslsocket.isConnected()){
			try{
				sslsocket.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	  public static void sendFile(String fileName){
		try {
			// tell the server we're about to send a file.
			bufferedwriter.write("-a-\n");
			bufferedwriter.flush();

			// send the name of the file
			bufferedwriter.write(fileName+"\n\n");
			bufferedwriter.flush();


			File source = new File(fileName);


			InputStream input = null;
			OutputStream output = null;

			input = new FileInputStream(source);
			output = outputstream;
			byte[] buf = new byte[1024];
			int bytesRead;
				
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
			System.out.println("Transfer complete");
		}
		catch (IOException e){
			e.printStackTrace();
		}       
	}

	public static void fetchFile(String fileName, int c){

		try{
			bufferedwriter.write("-f-\n");
			bufferedwriter.flush();

				// send the name of the file
			bufferedwriter.write(c + "\n");
			bufferedwriter.flush();

			bufferedwriter.write(fileName+"\n\n");
			bufferedwriter.flush();



			String trusted = bufferedreader.readLine();
			if (trusted.equals("T")){

				File dest = new File(fileName);


				InputStream input = null;
				OutputStream output = null;

				input = inputstream;
				output = new FileOutputStream(dest);
				byte[] buf = new byte[1024];
				int bytesRead;
						
				while ((bytesRead = input.read(buf)) > 0) {
					output.write(buf, 0, bytesRead);
				}
				System.out.println("Transfer complete");
			}
			else{
				System.out.println("The file: " + fileName + " Was not Trusted");
			}
		}
		catch (IOException e){
			System.out.println("failed");
		}  
	}

	public static void listFiles(){
		try {
			bufferedwriter.write("-l-\n");
			bufferedwriter.flush();

			String str = null;
	        while ((str = bufferedreader.readLine()) != null) {

	        	if(str.equals("===EOF===")){
	        		break;
				}

	        // process client commands

	        System.out.println(str);
	            // Outputs list of files.        
	        }
	    }
	    catch (IOException e) {
	    	// don't care

	    }


	}


	public static String vouchForFile(String fileName, String keyName) {
		TrustManager manager = new TrustManager();
		File file = new File(fileName);
		return manager.createSignature(file, keyName).getName();
	}

	public static void main(String args[]){
		System.setProperty("javax.net.ssl.trustStore","sslKey");
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");

		openConection("localhost:19999");
		System.out.println("Connected");
		fetchFile("kittens.jpg", 1);
		closeConection();
		System.out.println("Disconected");
	}

}