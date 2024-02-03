import java.net.*;
import java.io.*;
import java.util.StringTokenizer;

public class xFxClient {
	public static void main(String[] args) throws Exception {
		String command = args[0];
		
		String fileName = "";
		if(args.length == 2)
			fileName = args[1];

		try (Socket connectionToServer = new Socket("localhost", 80)) {

			// I/O operations

			InputStream in = connectionToServer.getInputStream();
			OutputStream out = connectionToServer.getOutputStream();

			BufferedReader headerReader = new BufferedReader(new InputStreamReader(in));
			BufferedWriter headerWriter = new BufferedWriter(new OutputStreamWriter(out));
			DataInputStream dataIn = new DataInputStream(in);
			DataOutputStream dataOut = new DataOutputStream(out);


			if (command.equals("d")) {
				String header = "download " + fileName + "\n";
				headerWriter.write(header, 0, header.length());
				headerWriter.flush();

				header = headerReader.readLine();

				if (header.equals("NOT FOUND")) {
					System.out.println("We're extremely sorry, the file you specified is not available!");
				} else {
					StringTokenizer strk = new StringTokenizer(header, " ");

					String status = strk.nextToken();
					
					if (status.equals("OK")) {

						String temp = strk.nextToken();
						int size = Integer.parseInt(temp);

						temp = strk.nextToken();
						long serverLastModified =  Long.parseLong(temp);

						// Check if the file exists in the client side with same last modified time
						File clientFile = new File("ClientShare/" + fileName);
						long clientLastModified = clientFile.lastModified();

						if (clientFile.exists() && clientLastModified == serverLastModified) {
								// Informing the end user
								System.out.println("File already exists! Skipping download.");

								// Informing the server
								header = "NO DOWNLOAD\n";
								headerWriter.write(header, 0, header.length());
								headerWriter.flush();
							} else {
								System.out.println("Downloading...");
								
								// Informing the server
								header = "DOWNLOAD\n";
								headerWriter.write(header, 0, header.length());
								headerWriter.flush();
								
								// Receiving the bytes of the file and downloading it
								byte[] space = new byte[size];
								int bytesRead = dataIn.read(space);

								// Simulate a network issue
								/*int bytesRead = dataIn.read(space,0,8000);
								connectionToServer.close();*/

								try (FileOutputStream fileOut = new FileOutputStream(clientFile)) {
									// Downloading the file
									// Or only a part of the file if there was a network issue
									fileOut.write(space, 0, bytesRead);

									// Setting the modified date to match that of the server. (By default we always get it to be equal to the time of the downlaod)
									File downloadedFile = new File("ClientShare/" + fileName);
									downloadedFile.setLastModified(serverLastModified);
								}
							}
					} else {
						System.out.println("You're not connected to the right Server!");
					}

				}

			} else if (command.equals("u")) {
				try{
					FileInputStream fileIn = new FileInputStream("ClientShare/" + fileName);
					int fileSize = fileIn.available();

					String header = "upload " + fileName + " " + fileSize + "\n";
					headerWriter.write(header, 0, header.length());
					headerWriter.flush();

					byte[] bytes = new byte[fileSize];
					fileIn.read(bytes);

					fileIn.close();

					dataOut.write(bytes, 0, fileSize);
				} catch(Exception ex){
					System.out.println("The specified file doesn't exist.");
				}

			} else if(command.equals("l")){
				// Sending the header to the server
				String header = "list\n";
				headerWriter.write(header,0,header.length());
				headerWriter.flush();

				// Receiving the server res's header
				header = headerReader.readLine();

				if(header.equals("NO FILES")){
					System.out.println("We're sorry. No files are available :/");
				} else{
					StringTokenizer strk = new StringTokenizer(header," ");
					String status = strk.nextToken();

					if(status.equals("OK")){

						// Get the number of files
						String temp = strk.nextToken();

						int numberOfFiles = Integer.parseInt(temp);

						temp = strk.nextToken(); // Skip "FILES"

						// Get the size of the files' list
						temp = strk.nextToken();
						int listSize = Integer.parseInt(temp);

						// Get the bytes
						byte[] bytes = new byte[listSize];
						dataIn.readFully(bytes);
				
						// Convert to string and display to client
						String filesList = new String(bytes);
						
						System.out.println("There are " + numberOfFiles + " files available on the server!!\n");
						System.out.format("%-40s %-10s%n", "File Name", "File Size (in bytes)");
						String[] lines = filesList.split("\n");	
						for (String line : lines) {
							strk = new StringTokenizer(line, " ");
							String name = strk.nextToken();
							String size = strk.nextToken();
							System.out.format("%-40s %-10s%n", name, size);
						}
					}
				}
			}
			else if(command.equals("r")){
				// Get the current size of the file, which represents where the file stopped downloading
				FileInputStream fileIn = new FileInputStream("ClientShare/" + fileName);
				int fileSize = fileIn.available();

				// Sending the req's header
				String header = "resume " + fileName + " " + fileSize +"\n";
				headerWriter.write(header, 0, header.length());
				headerWriter.flush();
				fileIn.close();

				// Receiving the server res's header
				header = headerReader.readLine();

				if (header.equals("NOT FOUND")) {
					System.out.println("We're extremely sorry, the file you specified is not available!");
				} else {
					StringTokenizer strk = new StringTokenizer(header, " ");

					String status = strk.nextToken();

					if (status.equals("OK")) {

						String temp = strk.nextToken();

						int size = Integer.parseInt(temp);

						if(size == -1){
							System.out.println("This file is already fully downloaded!");
							return;
						}

						byte[] space = new byte[size];

						dataIn.readFully(space);

						// Append the received bytes to the existing file
						try (FileOutputStream fileOut = new FileOutputStream("ClientShare/" + fileName, true)) {
							fileOut.write(space, 0, size);
						}

					} else {
						System.out.println("You're not connected to the right Server!");
					}

				}

			}
		}
        catch(Exception ex){
            System.out.println("Something went wrong.\n" + ex);
        }
	}
}