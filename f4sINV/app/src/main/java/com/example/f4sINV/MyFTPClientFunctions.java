package com.example.f4sINV;

import android.util.Log;

import java.io.FileInputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;


public class MyFTPClientFunctions {

    // Now, declare a public FTP client object.
    private static final String TAG = "MyFTPClientFunctions";
    public FTPClient mFTPClient = null;

    // Method to connect to FTP server:
    public boolean ftpConnect(String host, String username, String password,
                              int port) {
        try {
            Log.i(TAG, "1");
            mFTPClient = new FTPClient();
            // connecting to the host
            Log.i(TAG, "2");
            mFTPClient.connect(host, port); //Palma aquí

            // now check the reply code, if positive mean connection success
            Log.i(TAG, "3");
            if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
                // login using username & password
                Log.i(TAG, "4");
                boolean status = mFTPClient.login(username, password);

                /*
                 * Set File Transfer Mode
                 *
                 * To avoid corruption issue you must specified a correct
                 * transfer mode, such as ASCII_FILE_TYPE, BINARY_FILE_TYPE,
                 * EBCDIC_FILE_TYPE .etc. Here, I use BINARY_FILE_TYPE for
                 * transferring text, image, and compressed files.
                 */
                Log.i(TAG, "5");
                mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                Log.i(TAG, "6");
                mFTPClient.enterLocalPassiveMode();

                Log.i(TAG, "7");
                return status;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error: could not connect to host " + host);
        }

        return false;
    }

    // Method to change working directory:
    public boolean ftpChangeDirectory(String directory_path) {
        try {
            // JFA añade para que vaya a diretorio home
            // Importante!! -> PRIMERO se ha de partir de home y de ahi cambiar al dir siguiente indicado
            mFTPClient.changeWorkingDirectory("/"); //Home de f4sFTP

            mFTPClient.changeWorkingDirectory(directory_path); // Directorio destino desde home "/"
            Log.i(TAG, "Cambio de directorio a " + directory_path);
        } catch (Exception e) {
            Log.d(TAG, "Error: could not change directory to " + directory_path);
        }
        return false;
    }

    // Method to create new directory:
    public boolean ftpMakeDirectory(String new_dir_path) {
        try {
            boolean status = mFTPClient.makeDirectory(new_dir_path);
            return status;
        } catch (Exception e) {
            Log.d(TAG, "Error: could not create new directory named "
                    + new_dir_path);
        }
        return false;
    }

    // Method to upload a file to FTP server:
    public boolean ftpUpload(String srcFilePath, String desFileName) {
        boolean status = false;
        try {
            FileInputStream srcFileStream = new FileInputStream(srcFilePath);

            status = mFTPClient.storeFile(desFileName, srcFileStream);

            srcFileStream.close();

            return status;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "upload failed: " + e);
        }

        return status;
    }
}