package com.rarchives.ripme.torUtils;

import com.rarchives.ripme.utils.Utils;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TorController {
    private String pathToExe;
    private int controlPort;
    private int socksPort;
    private TorClient client;
    private TorProcess process;
    private char[] password;
    private char[] passwordHash;
    private String configFile;
    public static final int DEFAULT_SOCKSPORT = 9150;
    public static final int DEFAULT_CONTROLPORT = 9151;

    /**
     * Creates a TorController
     *
     * @param pathToExecutable
     *            path to the executable torUtils binary
     */
    public TorController(String pathToExecutable) {
        this(pathToExecutable, DEFAULT_SOCKSPORT, DEFAULT_CONTROLPORT);
    }

    /**
     * Creates a TorController
     *
     * @param pathToExecutable
     *            path to the executable torUtils binary
     * @param socksPort
     *            port for socks connections
     * @param controlPort
     *            port for control connections
     */
    public TorController(String pathToExecutable, int socksPort, int controlPort) {
        this.socksPort = socksPort;
        this.controlPort = controlPort;
        this.pathToExe = pathToExecutable;
        password = null;
        passwordHash = null;
        configFile = null;
    }

    /**
     * Sets the authentication password.
     *
     * @param password
     */
    public void setPassword(char[] password) {
        this.password = password;
    }

    /**
     * Starts the torUtils process and connects to the torUtils control server.
     *
     * @return
     */
    public boolean startUp() {
        process = new TorProcess(this.pathToExe);
        client = new TorClient(controlPort, password);

        if (password != null && passwordHash == null) {
            passwordHash = process.getPasswordHash(password);
            if (passwordHash == null)
                return false;
        }

        this.configFile = this.createConfigFile();
        if (configFile == null)
            return false;

        return process.start(configFile) && client.connect() && client.authenticate();
    }

    /**
     * Disconnects from the torUtils control server and terminates the torUtils process.
     *
     * @return
     */
    public boolean shutDown() {
        boolean firstError = process == null;
        boolean secondError = client == null;
        //Utils.appendLog("1" + firstError , Color.RED);
        //Utils.appendLog("11" + secondError , Color.RED);
        if (process == null || client == null)
            return false;
        boolean error = client.quit();
        //Utils.appendLog("2" + error , Color.RED);
        error = client.disconnect() && error;
        //Utils.appendLog("3" + error , Color.RED);
        error = process.terminate() && error;
        //Utils.appendLog("4" + error , Color.RED);
        error = this.removeConfigFile() && error;
        //Utils.appendLog("5" + error , Color.RED);
        return error;
    }

    /**
     * Changes the identity from torUtils client
     *
     * @return true if the identity was successful changed
     */
    public boolean changeIdentity() {
        if (process == null || !process.isAlive() || client == null || !client.isConnected())
            return false;
        return client.changeIdentity();
    }

    /**
     * Returns the socket port used to tunnel traffic
     *
     * @return
     */
    public int getSocksPort() {
        return this.socksPort;
    }

    /**
     * Returns the control port used to connect to the torUtils control server
     *
     * @return
     */
    public int getControlPort() {
        return this.controlPort;
    }

    /***
     * @see http://www.java2s.com/Code/Java/Development-Class/Getoperatingsystemtemporarydirectoryfolder.htm
     * @return system specific temp directory
     */
    private String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    /***
     * Writes a temporary torUtils config file
     *
     * @return true if the file was successful created
     * @see https://www.torproject.org/docs/torUtils-manual.html.en
     */
    private String createConfigFile() {
        File file = new File(getTempDir() + File.separator + "torconfig.tmp");
        if (file.exists()) {
            file.delete();
        }
        try {
            FileWriter writer = new FileWriter(file);
            writer.write("# temporary torUtils config file for java torUtils controler\r\n");
            writer.write("AvoidDiskWrites 1\r\n");
            writer.write("Log notice stdout\r\n");
            writer.write("SocksPort " + socksPort + " IPv6Traffic PreferIPv6 KeepAliveIsolateSOCKSAuth\r\n");
            if (passwordHash != null)
                writer.write("HashedControlPassword " + String.valueOf(passwordHash) + "\r\n");
            writer.write("ControlPort " + controlPort + "\r\n");
            writer.flush();
            writer.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Prints the logs from the torUtils control server and from the tors stdout.
     */
    public void printLog() {
        System.out.println("# PROCESS STDOUT:");
        if (process != null)
            for (String line : process.log)
                System.out.println(line);
        System.out.println("# TELNET LOG");
        if (client != null)
            for (String line : client.log)
                System.out.println(line);
    }

    /**
     * Removes the temporary torUtils config file.
     * @return
     */
    private boolean removeConfigFile(){
        File f = new File(this.configFile);
        if(f.exists()){
            f.delete();
            return true;
        }else{
            return false;
        }
    }
}