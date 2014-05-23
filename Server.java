import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.*;   
import java.util.*;

public class Server {

	/**
	 * The server will accept the connection from the client
	 * If the server sees client lose connection, re-establish
	 */
	
	public TCFileList getListOfFiles() {
		File file = null;
		String[] paths;
			
		try {      
			// create new file object
			file = new File("./");
								 
			// array of files and directory
			paths = file.list();

			TCFileList filesList = new TCFileList();

			TrustManager manager = new TrustManager();
			for (String path:paths) {
				// prints filename and directory name
				if (! path.endsWith(".sig") && ! path.endsWith(".class") && ! path.endsWith(".p12") && ! path.startsWith(".")) {
					int circumference = 0;
					try {
						circumference = manager.getCircumference(path);
					} catch(Exception ex) { }
					File file3 = new File(path); 
					Object[] signObjects = manager.getFileSignatories(file3).toArray();
					String signs[] = Arrays.copyOf(signObjects, signObjects.length, String[].class);
					filesList.add(path, circumference, signs);
				}
			}
			return(filesList);
		} catch (Exception e) {
			// if any error occurs
			e.printStackTrace();
		}
		return (null);
	}


	private TCUploadResponseMessage uploadFile(TCUploadRequestMessage message) {

		try{

			System.out.println("Uploading a file: " + message.fileName);

			File dest = new File(message.fileName);
			if (dest.exists() && message.fileName.endsWith(".crt")) {
				throw new RuntimeException("Cannot overwrite a certificate.");
			}

			OutputStream output = new FileOutputStream(dest);
		
			output.write(message.fileData);

			return (new TCUploadResponseMessage(true, "Sucessfully uploaded " + message.fileName));

		} catch(Exception e) {
			return (new TCUploadResponseMessage(false, "Upload failed: " + e.getMessage()));
			
		}

	}

	private TCDownloadResponseMessage downloadFile(TCDownloadRequestMessage message) {

		System.out.println("Sending a file");

		TrustManager man = new TrustManager();

		if(man.checkCircumference(message.fileName, message.protection)) {

			TrustManager trust = new TrustManager();
			return new TCDownloadResponseMessage(true, "Download success!", trust.loadFileAsBytes(message.fileName));
		} else {
			return new TCDownloadResponseMessage(false, "File was not secure", new byte[0]);
		}



	}

	private TCVouchResponseMessage createVouch(TCVouchRequestMessage message) {
		
		String keyName = message.certName.split(TrustManager.CERTIFICATE_EXTENSION)[0];
		System.out.println(keyName + " is vouching for " + message.fileName);
		// get the name from trust manager
		String sigFileName = TrustManager.createSignatureName(message.fileName, keyName);
		// create an TCUploadRequestMessage
		TCUploadRequestMessage newMessage = new TCUploadRequestMessage(sigFileName, message.signatureData);
		// call upload file with the TCUploadRequestMessage
		TCUploadResponseMessage m2 = uploadFile(newMessage);
		return new TCVouchResponseMessage(m2.success, m2.message);
	}

	private TCListResponseMessage listFiles(TCListRequestMessage massage) {
		System.out.println("Listing files.");
		return (new TCListResponseMessage(getListOfFiles()));

	}


	public static void main(String[] args) {
		int serverPort = 19999;
		if (args.length > 0) {
			serverPort = Integer.parseInt(args[0]);
		}
		Server server = new Server();
		TCServerSocket socket = null;
		TCSocket connection = null;
		try {
			socket = new TCServerSocketFactory(serverPort).open();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
<<<<<<< HEAD
		
=======

		System.out.print("listening on port ");
		System.out.println(serverPort);

>>>>>>> FETCH_HEAD
		while (true){
			try {
				connection = socket.accept();
				while(true){
					TCMessage message = connection.readPacket();
					System.out.print("Message received: ");
				
					if(message instanceof TCUploadRequestMessage) {
						connection.sendPacket(server.uploadFile((TCUploadRequestMessage) message));
					}
		
					if(message instanceof TCDownloadRequestMessage) {
						connection.sendPacket(server.downloadFile((TCDownloadRequestMessage) message));
					}
		
					if(message instanceof TCVouchRequestMessage) {
						connection.sendPacket(server.createVouch((TCVouchRequestMessage) message));
					}
		
					if(message instanceof TCListRequestMessage) {
						connection.sendPacket(server.listFiles((TCListRequestMessage) message));
					}
				}
			} catch (Exception ex) {
				System.out.println("Client disconnected.");
			}
		}
	}
}
