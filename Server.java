import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.*;   

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
			
			// for each name in the path array
			// String fileListRepr = "Ring / Filename\n---- / --------\n";
			TrustManager manager = new TrustManager();
			for (String path:paths) {
				// prints filename and directory name
				if (!path.endsWith(".sig")) {
					int circumference = 0;
					try {
						circumference = manager.getCircumference(path);
					} catch(Exception ex) { }

					filesList.add(path, Integer.toString(circumference), " ");
				}
			}

			return(filesList);
			

		} catch (Exception e) {
			// if any error occurs
			e.printStackTrace();
		}
		return (null);
	}


	private TCUploadResponseMessage uploadFile(TCUploadRequestMessage message){

		try{

			System.out.println("Receving a file");

			File dest = new File(message.filename);

			OutputStream output = new FileOutputStream(dest);
		
			output.write(message.fileData);

			return (new TCUploadResponseMessage(true, "Upload Sucess"));

		} catch(Exception e){
			return (new TCUploadResponseMessage(false, "Upload fail"));
			
		}

	}

	private void downloadFile(TCDownloadRequestMessage message){


	}

	private void createVouch(TCVouchRequestMessage message){
		// get the name from trust manager

		// create an TCUploadRequestMessage
		// call upload file with the TCUploadRequestMessage
	}

	private TCListResponseMessage listFiles(TCListRequestMessage massage){
		return (new TCListResponseMessage(getListOfFiles()));

	}


	public static void main(String[] args) {

		//while (true) {
		int serverPort = 19999;

		TCServerSocket socket = new TCServerSocketFactory(serverPort).open();

		TCSocket connection = sockect.accept();
		TCMessage message = connection.readPacket();

		if(message instanceof TCUploadRequestMessage){
			connection.sendPacket(uploadFile((TCUploadRequestMessage) message));
		}

		if(message instanceof TCDownloadRequestMessage){
			connection.sendPacket(downloadFile((TCDownloadRequestMessage) message));
		}

		if(message instanceof TCVouchRequestMessage){
			connection.sendPacket((createVouch((TCVouchRequestMessage) message));
		}

		if(message instanceof TCListRequestMessage) {
			connection.sendPacket(listFiles((TCListRequestMessage) message));
		}
	}
}
