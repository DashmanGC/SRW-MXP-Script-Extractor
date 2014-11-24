/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package srw.mxp.script.extractor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jonatan
 */
public class Main {
    static String bin_file = "MAP_ADD.BIN";
    static long start_of_files = 0x02bce000;
    static long end_of_files = 0x03315000;
    static RandomAccessFile f;
    static int tex_counter = 0;
    static String file_list = "";


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        if (args.length == 1){
            if (args[0].equals("-i")){ // Insert files
                try{
                    insertFiles();
                }catch (IOException ex) {
                    System.err.println("ERROR: Couldn't read file.");   // END
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                if (args[0].equals("-e")){  // Extract files
                    try{
                        extractFiles();
                    }catch (IOException ex) {
                        System.err.println("ERROR: Couldn't read file.");   // END
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                else{
                    System.out.println("ERROR: Wrong parameter: " + args[0]);
                    System.out.println("EXTRACT:\n java -jar script_extract -e");
                    System.out.println("INSERT:\n java -jar script_extract -i");
                }
            }
        }
        else{
            System.out.println("ERROR: Wrong number of parameters: " + args.length);
            System.out.println("EXTRACT:\n java -jar script_extract -e");
            System.out.println("INSERT:\n java -jar script_extract -i");
        }
    }


    // Takes a 4-byte hex little endian and returns its int value
    public static int byteSeqToInt(byte[] byteSequence){
        if (byteSequence.length != 4)
            return -1;

        int value = 0;
        value += byteSequence[0] & 0xff;
        value += (byteSequence[1] & 0xff) << 8;
        value += (byteSequence[2] & 0xff) << 16;
        value += (byteSequence[3] & 0xff) << 24;
        return value;
    }


    // Extract SRWL files from MAP_ADD.BIN
    public static void extractFiles() throws IOException{
        RandomAccessFile file = new RandomAccessFile(bin_file, "r");

        boolean error = false;

        int size = 0;

        long offset = start_of_files;
        int counter = 0;

        byte[] aux = new byte[4];
        byte[] data;
        
        // Extract the part before the SRWL blocks as a file
        data = new byte[(int) start_of_files];
        
        file.read(data);
        
        writeOther(data, "MAP_ADD_Start.BIN");
        
        file_list += "MAP_ADD_Start.BIN\n";

        
        // Search for the SRWL blocks
        while (offset < end_of_files && !error){
            file.seek(offset);

            file.read(aux);

            if (aux[0] != 'S' || aux[1] != 'R' || aux[2] != 'W' || aux[3] != 'L'){
                System.err.println("ERROR: Looking for SRWL file in the wrong place.\nMAP_ADD.BIN might have been altered before.");
                System.err.println("Offset: " + offset);
                error = true;
            }

            if (!error){
                size = 0;
                
                boolean stop = false;
                
                // Find the end of the SRWL file
                while (!stop){
                    size += 2048;
                    
                    if (offset + size >= end_of_files)
                        stop = true;
                    else{
                        System.out.println("Offset: " + (offset + size));
                        file.seek(offset + size);

                        file.read(aux);

                        if (aux[0] == 'S' && aux[1] == 'R' && aux[2] == 'W' && aux[3] == 'L'){
                            stop = true;
                            System.out.println("FOUND END!");
                        }
                    }
                }
                
                // Grab the SRWL file
                file.seek(offset);
                
                data = new byte[size];
                
                file.read(data);
                
                offset += size;
                
                counter++;
                
                // Write the SRWL file to disk
                String filename = "";
                
                if (counter < 1000)
                    filename += "0";
                if (counter < 100)
                    filename += "0";
                if (counter < 10)
                    filename += "0";

                filename += counter + ".SRWL";
                
                writeOther(data, filename);
                
                file_list += filename + "\n";
            }
        }
        
        // Extract the part after the SRWL blocks as a file
        data = new byte[(int) (file.length() - end_of_files)];
        
        file.seek(end_of_files);
        
        file.read(data);
        
        writeOther(data, "MAP_ADD_End.BIN");
        
        file_list += "MAP_ADD_End.BIN";


        // Close the file and write the file list
        file.close();
        
        writeFileList(bin_file + "_extract/files.list");
    }

    
    // Writes an array of bytes as a file
    public static void writeOther(byte[] data, String filename){
        String path = bin_file + "_extract";
        File folder = new File(path);
        if (!folder.exists()){
            boolean success = folder.mkdir();
            if (!success){
                System.err.println("ERROR: Couldn't create folder.");
                return;
            }
        }

        // Create the file inside said folder
        String file_path = filename;
        path += "/" + file_path;
        try {
            RandomAccessFile other = new RandomAccessFile(path, "rw");

            other.write(data);

            other.close();

            System.out.println(file_path + " saved successfully.");
            tex_counter++;
        } catch (IOException ex) {
            System.err.println("ERROR: Couldn't write " + file_path);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public static void writeFileList(String path) throws IOException{
        PrintWriter pw = new PrintWriter(path);

        pw.print(file_list);

        pw.close();
    }
    
    
    // Join all pieces back into a new MAP_ADD.BIN file
    public static void insertFiles() throws IOException{
        // 1) Get the list of files and offsets inside files.list
        //System.out.println("Reading from " +  bin_file + "_files.list");
        BufferedReader br = new BufferedReader(new FileReader("files.list"));
        String line;
        
        ByteArrayOutputStream os = new ByteArrayOutputStream( );
        RandomAccessFile f;
        byte[] data;

        while ((line = br.readLine()) != null) {
            f = new RandomAccessFile(line, "r");
            
            data = new byte[(int) f.length()];
            
            f.read(data);
            
            f.close();
            
            os.write(data);
        }
        br.close();
        
        // 2) For each file in the list, write it back into the file at the specified offset
        f = new RandomAccessFile(bin_file, "rw");
        
        f.write(os.toByteArray());

        f.close();
    }

}
