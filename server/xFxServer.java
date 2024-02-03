	import java.net.*;
	import java.io.*;
import java.util.StringTokenizer;

	public class xFxServer {

		public static void main(String[] args) throws Exception {

			try (ServerSocket ss = new ServerSocket(80)) {

				while (true) {
					System.out.println("Server waiting...");
					Socket connectionFromClient = ss.accept();
					System.out.println(
							"Server got a connection from a client whose port is: " + connectionFromClient.getPort());
					
					try {
						InputStream in = connectionFromClient.getInputStream();
						OutputStream out = connectionFromClient.getOutputStream();

						String errorMessage = "NOT FOUND\n";
						String noFilesError = "NO FILES\n";

						BufferedReader headerReader = new BufferedReader(new InputStreamReader(in));
						BufferedWriter headerWriter = new BufferedWriter(new OutputStreamWriter(out));

						DataInputStream dataIn = new DataInputStream(in);
						DataOutputStream dataOut = new DataOutputStream(out);

						String header = headerReader.readLine();
						StringTokenizer strk = new StringTokenizer(header, " ");

						String command = strk.nextToken();

						//String fileName = strk.nextToken();
						String fileName = "";

						if (command.equals("download")) {
							try {
								fileName = strk.nextToken();

								String path = "ServerShare/" + fileName;

								FileInputStream fileIn = new FileInputStream(path);
								int fileSize = fileIn.available();

								// Get the last modified time
								File file = new File(path);
								long lastModifiedTime = file.lastModified();

								header = "OK " + fileSize + " " + lastModifiedTime + "\n";

								headerWriter.write(header, 0, header.length());
								headerWriter.flush();

								// Reading the client's header
								header = headerReader.readLine();

								if(header.equals("DOWNLOAD")){
									// Sending the bytes of the file
									byte[] bytes = new byte[fileSize];
									fileIn.read(bytes);
									//Thread.sleep(100);
									dataOut.write(bytes, 0, fileSize);
								} else if( header.equals("NO DOWNLOAD")){
									System.out.println("Skipping Download...");
								}

								fileIn.close();
							} catch (Exception ex) {
								headerWriter.write(errorMessage, 0, errorMessage.length());
								headerWriter.flush();

							} finally {
								connectionFromClient.close();
							}
						} else if (command.equals("upload")) {
							String temp = strk.nextToken();

							int size = Integer.parseInt(temp);
							
							byte[] space = new byte[size];

							dataIn.readFully(space);

							try (FileOutputStream fileOut = new FileOutputStream("ServerShare/" + fileName)) {
								fileOut.write(space, 0, size);
							}

						} else if (command.equals("list")){
							try {
								// Getting the files from the folder
								File folder = new File("ServerShare/");
								File[] files = folder.listFiles();

								// Initializing the string that will be sent to the client
								StringBuilder listOfFiles = new StringBuilder(); 

								if(files.length == 0){
									header = "NO FILES\n";
									headerWriter.write(header, 0, header.length());
									headerWriter.flush();
								}
								else{

									// Preparing the list of files: Filename size\n
									for(File file: files){
										if(file.isFile()){ // Checking if the file is not a directory
											listOfFiles.append(file.getName() + " " + file.length() + "\n");
										}
									}
									
									// Preparing and sending the header
									header = "OK " + files.length + " FILES " + listOfFiles.toString().getBytes().length + "\n";
									
									headerWriter.write(header, 0, header.length());
									headerWriter.flush();
									
									byte[] bytes = listOfFiles.toString().getBytes();
									int listSize = listOfFiles.toString().getBytes().length;
									
									// If I remove this, the client won't receive the data
									Thread.sleep(100);
									
									// Sending the list 
									dataOut.write(bytes, 0, listSize);
								}
							} catch (Exception ex) {
								headerWriter.write(noFilesError, 0, noFilesError.length());
								headerWriter.flush();
							} finally {
								connectionFromClient.close();
							}
						} else if(command.equals("resume")){
							try {
								fileName = strk.nextToken();

								// Get the file position from the client's req
								String temp = strk.nextToken();
								int filePosition = Integer.parseInt(temp);

								// Find the file
								FileInputStream fileIn = new FileInputStream("ServerShare/" + fileName);
								int fileSize = fileIn.available();

								// Get the bytes from the position where we stopped 
								byte[] filebytes = new byte[fileSize];
								BufferedInputStream fileInput = new BufferedInputStream(fileIn);

								fileInput.skip(filePosition); // Skip to where the download stopped

								// Read and print the remaining content
								int bytesRead = fileInput.read(filebytes, 0, filebytes.length); // Fill it with our desired bytes
								//System.out.println("File:" + new String(filebytes, 0, filebytes.length) +" length: " + bytesRead);
								
								// Send header to client
								header = "OK " + bytesRead + "\n";

								headerWriter.write(header, 0, header.length());
								headerWriter.flush();
								
								Thread.sleep(100);

								// Send the remaining bytes
								dataOut.write(filebytes, 0, bytesRead);

								fileIn.close();

							} catch (Exception ex) {
								headerWriter.write(errorMessage, 0, errorMessage.length());
								headerWriter.flush();
							} finally {
								connectionFromClient.close();
							}
						} 
						else {

							System.out.println("Connection got from an incompatible client");

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}