import java.io.*;
import java.net.*;
import java.util.*;

public class WebServer
{
    public static void main (String args[]) throws Exception {
        String requestMessageLine;
        String fileName;

        int myPort = 6789;
        ServerSocket listenSocket = new ServerSocket (myPort);

        while(true) {
            System.out.println ("Escoitando o porto " + myPort);
            Socket connectionSocket = listenSocket.accept();
            BufferedReader inFromClient = new BufferedReader (new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream (connectionSocket.getOutputStream());
            
            // tratamos a primeira liña da petición
            requestMessageLine = inFromClient.readLine();
            System.out.println (requestMessageLine);

            String[] cachos = requestMessageLine.split("\\s");

            if (cachos[0].equals("GET")) {
                fileName = cachos[1];

                if (fileName.startsWith("/") == true)
                    fileName = fileName.substring(1);

                // ler o contido do ficheiro solicitado
                File file = new File(fileName);
                if (file.exists()) {
                    // converter o ficheiro nun array de bytes
                    int numOfBytes = (int) file.length();
                    FileInputStream inFile = new FileInputStream (fileName);
                    byte[] fileInBytes = new byte[numOfBytes];
                    inFile.read(fileInBytes);

                    // enviar a contestación
                    outToClient.writeBytes ("HTTP/1.0 200 Document Follows\r\n");
                    /*
                    if (fileName.endsWith(".jpg"))
                        outToClient.writeBytes ("Content-Type: image/jpeg\r\n");
                    if (fileName.endsWith(".gif"))
                        outToClient.writeBytes ("Content-Type: image/gif\r\n");
                    */
                    //outToClient.writeBytes ("Content-Length: " + numOfBytes + "\r\n");
                    outToClient.writeBytes ("\r\n");
                    outToClient.write(fileInBytes, 0, numOfBytes);
                } else {
                    System.out.println("Not Found");
                    outToClient.writeBytes("HTTP/1.0 404 NOT_FOUND\r\n");
                    outToClient.writeBytes("\r\n");
                    outToClient.writeBytes("<html><body>Sent&iacute;molo. Non se atopou o ficheiro</body></html>\r\n");
                }
                // ler, sen tratar, o resto de liñas da petición
                requestMessageLine = inFromClient.readLine();
                while (requestMessageLine.length() >= 5) {
                    System.out.println (requestMessageLine);
                    requestMessageLine = inFromClient.readLine();
                }
                System.out.println (requestMessageLine);

                connectionSocket.close();
            } else {
                System.out.println ("Petición incorrecta");
            }
        }
    }
}
