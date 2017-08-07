package com.push.lazyir.managers;

import com.push.lazyir.devices.NetworkPackage;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by buhalo on 19.07.17.
 */
public class ConnectionThreadTest {


    private Socket socket;
    private  TcpConnectionManager instanceForTest;
    private TcpConnectionManager.ConnectionThread connectionThread;
    private ByteArrayOutputStream byteArrayOutputStream;
    private ByteArrayInputStream byteArrayInputStream;


    public static void assertDoesNotThrow(FailingRunnable action){
        try{
            action.run();
        }
        catch(Exception ex){
            throw new Error("expected action not to throw, but it did!", ex);
        }
    }
    @FunctionalInterface interface FailingRunnable { void run() throws Exception; };
    @Before
    public void setUp() throws Exception {
        socket = mock(Socket.class);
       byteArrayOutputStream = new ByteArrayOutputStream();
       byteArrayInputStream = new ByteArrayInputStream("{\"id\":\"buhalo-MS-78172\",\"name\":\"buhalo2\",\"type\":\"tcpIntroduce\",\"data\":\"407820240\"}".getBytes());
        when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
        when(socket.getInputStream()).thenReturn(byteArrayInputStream);
        when(socket.getInetAddress()).thenReturn(InetAddress.getByName("localhost"));
        instanceForTest = new TcpConnectionManager();
        connectionThread = instanceForTest.new ConnectionThread(socket);
    }

    public void setUpInOut() throws NoSuchFieldException, IllegalAccessException {
        Field in = connectionThread.getClass().getDeclaredField("in");
        Field out = connectionThread.getClass().getDeclaredField("out");
        Field serveron = instanceForTest.getClass().getDeclaredField("ServerOn");
        in.setAccessible(true);
        out.setAccessible(true);
        serveron.setAccessible(true);
        in.set(connectionThread,new BufferedReader(
                new InputStreamReader(byteArrayInputStream)));
        out.set(connectionThread,new PrintWriter(
                new OutputStreamWriter(byteArrayOutputStream)));
        serveron.set(instanceForTest,true);
    }

    @Test
    public void run() throws Exception {
        setUpInOut();
        connectionThread.run();
        assertDoesNotThrow(connectionThread::run);
    }

    @Test
    public void sendIntroduce() throws Exception
    {
        setUpInOut();
        connectionThread.sendIntroduce();
        Assert.assertEquals("{\"id\":\"buhalo-MS-7817\",\"name\":\"buhalo\",\"type\":\"tcpIntroduce\",\"data\":\"407820240\"}\n",byteArrayOutputStream.toString());
    }

    @Test
    public void determineWhatTodo() throws Exception {
        setUpInOut();
        connectionThread.determineWhatTodo(new NetworkPackage("{\"id\":\"buhalo-MS-78172\",\"name\":\"buhalo2\",\"type\":\"tcpIntroduce\",\"data\":\"407820240\"}"));
        System.out.println( byteArrayOutputStream.toString());
    }

    @Test
    public void newConnectedDevice() throws Exception {
    }

    @Test
    public void pingCheck() throws Exception {
    }

    @Test
    public void ping() throws Exception {
    }

    @Test
    public void commandFromClient() throws Exception {
    }

    @Test
    public void testSimplePayload() throws IOException {
        byte[] emptyPayload = new byte[1001];

        // Using Mockito
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
        final String payload = "whatever you wanted to send";


        Assert.assertTrue("Message sent successfully", sendTo("localhost", 5667,payload));
        System.out.println(byteArrayOutputStream.toString());
        Assert.assertEquals(payload, byteArrayOutputStream.toString());
    }

    public boolean sendTo(String hostname, int port,String payload) {
        boolean sent = false;

        try {

            OutputStream out = socket.getOutputStream();
            out.write(payload.getBytes());
            socket.close();
            sent = true;
        } catch (IOException e) {

        }

        return sent;
    }


}