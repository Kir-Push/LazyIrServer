package com.push.lazyir.modules.share;



import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.managers.TcpConnectionManager;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.BackgroundService;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static com.push.lazyir.MainClass.executorService;


/**
 * Created by buhalo on 05.03.17.
 */
public class ShareModule extends Module {

    public static final String SHARE_T = "ShareModule";
    @Deprecated
    public static final String SHARE_TYPE = "share_type";
    public static final String SETUP_SERVER_AND_SEND_ME_PORT = "setup server and send me port";
    public static final String CONNECT_TO_ME_AND_RECEIVE_FILES = "connect to me and receive files"; // first arg port,second number of files - others files
    public static final String GET_PATH = "get path";  // actually it means list files on path;
    public static final String SEND_PATH = "post path";
    public static final String GET_FILE = "get file";
    public static final String POST_FILE = "post file";
    public static final String GET_DIRECTORY = "get directory";
    public static final String POST_DIRECORY = "post directory";
    public static final String PORT = "port";

    public static final String CONNECT_TO_ME_AND_RECEIVE_FILES_REVERSE = "CONNECT_TO_ME_AND_RECEIVE_FILES_REVERSE";

    public static final String RECCONECT = "recconect";


    public static final String FIRST_ARG_NUMBER_OF_FILES ="nf {?,?}";// first ? wil be you curr file, second overral files ( which you need to receive


    private Socket fileSocket;
    private DataInputStream in;
    private String serverPath;
    private String clientPath;
    private boolean waitingForServerAnswer = false;

    private boolean connectedToserver = false;
    private int port = 9000;
    private String userName;
    private String pass;
    private Future futuresftp;
    private SftpServerProcess sftpServerProcess;

    private boolean waitResponse;

    private List<FileWrap> responseList;
    private String rootPathFromServer;

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(GET_PATH))
        {
            FileWraps fileWraps = np.getObject(NetworkPackage.N_OBJECT,FileWraps.class);
            String temp;
            if( fileWraps.getFiles().get(0).getPath().equals("root"))
            {
                temp = FileSystemView.getFileSystemView().getRoots()[0].getAbsolutePath();
            }
            else
            {
                temp = fileWraps.getFiles().get(0).getPath();
            }
            File folder = new File(temp);
            File[] listOfFiles = folder.listFiles();
            NetworkPackage sendNp = new NetworkPackage(SHARE_T, SEND_PATH);
            sendNp.setObject(NetworkPackage.N_OBJECT,new FileWraps(getFileWrapFromArray(listOfFiles)));
            String fromTypeAndData = sendNp.getMessage();
            BackgroundService.getTcp().sendCommandToServer(np.getId(),fromTypeAndData);

        }
        else
        if(np.getData().equals(SETUP_SERVER_AND_SEND_ME_PORT))
        {
            startDownloaderTask(np);
        }
        else
        if(np.getData().equals(CONNECT_TO_ME_AND_RECEIVE_FILES))
        {
            connectToSftpServer(np);
        }
        else
        if(np.getData().equals(CONNECT_TO_ME_AND_RECEIVE_FILES_REVERSE))
        {
            ParseDownload(np);
        }
        else
        if(np.getData().equalsIgnoreCase(RECCONECT))
        {
             userName = np.getValue("userName");
             pass = np.getValue("pass");
             if(userName != null && pass != null)
             recconectToSftp(np.getId());
        }

    }

    private void connectToSftpServer(NetworkPackage np) {
        String value = np.getValue(PORT);
     //   if(connectedToserver)
            stopSftpServer();
        port = Integer.parseInt(value);
        userName = np.getValue("userName");
        pass = np.getValue("pass");
        sftpServerProcess = new SftpServerProcess(port, Device.getConnectedDevices().get(np.getId()).getIp(),userName,pass);
        futuresftp = executorService.submit(sftpServerProcess);

    }

    public void recconectToSftp(String id) //todo in android
    {
       stopSftpServer();
       if(userName == null || pass == null)
       {
           NetworkPackage np = new NetworkPackage(SHARE_T,RECCONECT);
           BackgroundService.getTcp().sendCommandToServer(id,np.getMessage());
       }
       else
       {
           sftpServerProcess = new SftpServerProcess(port, Device.getConnectedDevices().get(id).getIp(),userName,pass);
           futuresftp = executorService.submit(sftpServerProcess);
       }
    }

    public void stopSftpServer() {
        if(sftpServerProcess != null) {
            sftpServerProcess.stopProcess();
            futuresftp.cancel(true);
        }

    }

    public static void sendSetupServerCommand(String dvID)
    {
        NetworkPackage np = new NetworkPackage(SHARE_T,SETUP_SERVER_AND_SEND_ME_PORT);
        String os  = System.getProperty("os.name").toLowerCase();
        if(os.indexOf("win") >= 0)
        {
            os = "win";
        }
        else if(os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0 )
        {
            os = "nix";
        }
        else
        {
            os = "hz";
        }
        np.setValue("os",os);
        BackgroundService.getTcp().sendCommandToServer(dvID,np.getMessage());
    }

    private List<FileWrap> getFileWrapFromArray(File[] files)
    {
        List<FileWrap> fileWraps = new ArrayList<>();
        if(files == null)
            return fileWraps;
        for(int i = 0;i<files.length;i++)
        {
            File fl = files[i];
            fileWraps.add(new FileWrap(false,fl.isFile(),fl.getName()));
        }
        return fileWraps;
    }

    private void startDownloaderTask(NetworkPackage np) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (ServerSocket serverSocket  = new ServerSocket(5668)) {
                    fileSocket = serverSocket.accept();
                    FileWraps fileWraps = np.getObject(NetworkPackage.N_OBJECT,FileWraps.class);
                    List<FileWrap> files = fileWraps.getFiles();
                    String currPath = files.get(0).getPath();
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(fileSocket.getOutputStream()));
                    for(int i = 1;i<files.size();i++)
                    {
                        String s = files.get(i).getPath();
                       // System.out.println(currPath +"/" +  s);
                        File file = new File(currPath,s);
                        try {
                            FileInputStream in = new FileInputStream(file);
                            byte[] bytes = new byte[16*1024];
                            int count;
                            out.writeUTF(s);
                            out.writeLong(file.length());
                            while ((count = in.read(bytes)) > 0) {
                                out.write(bytes, 0, count);
                                out.flush();
                            }
                            in.close();
                         //   System.out.println("endFile!!!$$$!!!".getBytes());

                        } catch (IOException e) {
                            Loggout.e("ShareModule",e.toString());
                        }
                    }
                    if(out != null)
                    out.close();

                }catch (IOException e)
                {
                    Loggout.e("SHAREMODULE",e.toString());
                }
            }
        }).start();
        try {
            NetworkPackage napa = new NetworkPackage(SHARE_T, CONNECT_TO_ME_AND_RECEIVE_FILES);
            napa.setValue(PORT,"5668");
            List<FileWrap> files = np.getObject(NetworkPackage.N_OBJECT, FileWraps.class).getFiles();
            napa.setObject(NetworkPackage.N_OBJECT,new FileWraps(files.subList(1,files.size())));
            String fromTypeAndData = napa.getMessage();
            BackgroundService.getTcp().sendCommandToServer(np.getId(),fromTypeAndData);
        } catch (Exception error) {
           Loggout.e("SHAREMANAGER",error.toString());
        }
    }




//
//    public synchronized File downloadFile(String path,String fileName)
//    {
//        if(fileSocket == null || !fileSocket.isConnected())
//        {
//            return null;
//        }
//        File file = new File(path,fileName);
//        FileOutputStream out = null;
//        try {
//            in = fileSocket.getInputStream();
//            out = new FileOutputStream(file);
//            byte[] bytes = new byte[16*1024];
//            int count;
//            while ((count = in.read(bytes)) > 0) {
//                out.write(bytes, 0, count);
//            }
//
//
//        } catch (IOException e) {
//           Loggout.e("ShareModule",e.toString());
//        }
//        finally {
//            if(out != null)
//                try {
//                    out.close();
//                } catch (IOException e) {
//                    Loggout.e("ShareModule",e.toString());
//                }
//        }
//        return file;
//    }
//
//    public List<File> downloadFiles(String path,List<FileWrap> fileWraps,String externalPath)
//    {
//        List<File> downloadedFiles = new ArrayList<>();
//
//        for(FileWrap fileWrap : fileWraps)
//        {
//            if(fileWrap.isFile()) {
//                downloadedFiles.add(downloadFile(path, fileWrap.getPath()));
//            }
//        }
//        return downloadedFiles;
//    }

    public synchronized File downloadFile(String path,String fileName)
    {
        if(fileSocket == null || !fileSocket.isConnected())
        {
            return null;
        }
        File file = new File(path,fileName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            byte[] bytes = new byte[16*1024];
            int count;
            System.out.println("try read utf");
            String name = in.readUTF();
            System.out.println("Name is " + name);
            long fileSize = in.readLong();
            while (fileSize > 0 && (count = in.read(bytes, 0, (int)Math.min(bytes.length, fileSize))) != -1) {

                System.out.println(fileSize);
                if(new String(bytes,0,count).equals("endFile!!!$$$!!!"))
                {
                    System.out.println("file break    count "  + count);
                    System.out.println(bytes);
                    break;
                }

                out.write(bytes, 0, count);
                out.flush();
                fileSize -= count;
            }
            System.out.println("I'm in download file jaja   " + path + "   " + fileName);
            out.close();
        } catch (IOException e) {
            Loggout.e("ShareModule",e.toString());
        }
        return file;
    }

    private List<File> fileList;

    public List<File> ParseDownload(final NetworkPackage np) // if np.data == CONNECT_TO_ME_AND_RECEIVE_FILES
    {
        final List<FileWrap> args = np.getObject(NetworkPackage.N_OBJECT,FileWraps.class).getFiles();
        serverPath = np.getValue("Where");
        clientPath = np.getValue("Whom");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int port = Integer.parseInt(np.getValue(PORT));
                String secondArgFirst = args.get(0).getPath();
//                List<FileWrap> fileWraps = new ArrayList<>();
//                for(int i = 0;i<args.size();i++)
//                {
//                    String arg = args.get(i).getPath();
//                        if(args.get(i).isFile()){
//                            fileWraps.add(new FileWrap(true,args.get(i).isFile(),args.get(i).getPath()));
//                    }
//                }

                String id = np.getId();
                Device device = Device.getConnectedDevices().get(id);
                try {
                    fileSocket = new Socket(device.getIp(),port);
                    in = new DataInputStream(new BufferedInputStream(fileSocket.getInputStream()));
                    fileList= downloadFiles(serverPath, args, null);
                    in.close();
                    fileSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return fileList;
    }

    public List<File> downloadFiles(String path,List<FileWrap> fileWraps,String externalPath)
    {
        List<File> downloadedFiles = new ArrayList<>();


        for(FileWrap fileWrap : fileWraps)
        {
            //    if(fileWrap.isFile()) {//
            System.out.println("tut");
            downloadedFiles.add(downloadFile(path, fileWrap.getPath()));
            //     }
        }
        return downloadedFiles;
    }



}
