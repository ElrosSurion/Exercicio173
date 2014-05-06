/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ANTONIOFA
 */
import java.io.*;
import java.net.*;
import java.util.HashMap;
public class ServidorWeb {

    public static void main (String args[]) throws Exception {
        String requestMessageLine;
        String fileName;
        HashMap<String, String> datos = new HashMap<String, String>();
        BufferedReader br = null;
        String lin = null;
        try {
            br = new BufferedReader(new FileReader("configuracion.txt"));
            String[] cachos;
            while ((lin = br.readLine()) != null) {
                cachos = lin.split("=");             
                datos.put(cachos[0], cachos[1]);
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Non existe o ficheiro de configuracion.");
        }
        
        int myPort = Integer.parseInt(datos.get("porto"));
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
                fileName = datos.get("ruta") + cachos[1];
                String extension = "";
                int i = fileName.lastIndexOf('.');
                if (i > 0) {
                    extension = fileName.substring(i+1);
                }
                if (extension != "" && datos.get("extensions").contains(extension)) {
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
                        /* Esto no es obligatorio
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
                        File file2 = new File(datos.get("erro404"));
                        int numOfBytes = (int) file2.length();
                        FileInputStream inFile = new FileInputStream (datos.get("erro404"));
                        byte[] fileInBytes = new byte[numOfBytes];
                        inFile.read(fileInBytes);
                        outToClient.writeBytes("HTTP/1.0 404 NOT_FOUND\r\n");
                        outToClient.writeBytes("\r\n");
                        outToClient.write(fileInBytes, 0, numOfBytes);
                    }
                } else {
                    System.out.println("Extensión de arquivo incorrecta.");
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
