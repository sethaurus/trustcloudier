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
			/*
			Server serverInstance = new Server();
			System.setProperty("javax.net.ssl.keyStore","sslKey");
			System.setProperty("javax.net.ssl.keyStorePassword", "123456");
			*/
				/*
				serverInstance.sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();      
				serverInstance.sslserversocket = (SSLServerSocket) serverInstance.sslserversocketfactory.createServerSocket(serverPort);
				
				//System.out.println("Server Initialized. SSL set as default and on port " + serverPort);

				serverInstance.sslsocket = (SSLSocket) serverInstance.sslserversocket.accept();

		String str = null;
			while ((str = bufferedreader.readLine()) != null) {

				if(str.equals("-a-")){
					System.out.println("Receving a file");
					String fileName = bufferedreader.readLine();

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
				}

				if(str.equals("-f-")){
					System.out.println("sending a file");
						
					int c = Integer.parseInt(bufferedreader.readLine());
					System.out.println(c);
					String fileName = bufferedreader.readLine();
					boolean isTrusted = new TrustManager().checkCircumference(fileName, c);

					if(isTrusted){
						bufferedwriter.write("T\n");
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
						serverInstance.sslserversocket.close();
						serverInstance.sslsocket.close();
					}
						else{
							bufferedwriter.write("F\n");

						}
					}

					if(str.equals("-l-")){
						bufferedwriter.write(getListOfFiles());
						bufferedwriter.flush();

						//serverInstance.sslserversocket.close();
						//serverInstance.sslsocket.close();
					}


					else{
						System.out.println(str);
						System.out.println();
					}
				}
				**/
	}
}
