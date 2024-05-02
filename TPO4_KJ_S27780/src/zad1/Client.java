/**
 * @author Karwowski Jakub S27780
 */

package zad1;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Client {

    private String host;
    private int port;
    private String id;
    private SocketChannel socketChannel;
    private boolean isFinished=false;


    public Client(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;

    }

    public void connect() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.bind(null);
            socketChannel.connect(new InetSocketAddress(host,port));
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String send(String req) {
        if(isFinished){
            try {
                socketChannel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            sendRequirement(req);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if(req.equals("bye") || req.equals("bye and log transfer")){
                isFinished=true;
            }
            return readResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
    private void sendRequirement(String requirement) throws IOException {

        byteBuffer.clear();

        byteBuffer.put(requirement.getBytes(Charset.defaultCharset()));

        byteBuffer.flip();
        while(byteBuffer.hasRemaining()){
            socketChannel.write(byteBuffer);
        }


    }
    ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);
    private String readResponse() throws IOException {
        readBuffer.clear();
        int read = socketChannel.read(readBuffer);
        if(read == -1){
            socketChannel.close();
            return null;
        }
        if (read==0){
            while(read==0){
                read=socketChannel.read(readBuffer);
            }
        }
        readBuffer.flip();
        byte[] receivedBytes = new byte[readBuffer.remaining()];
        readBuffer.get(receivedBytes);
        return new String(receivedBytes,Charset.defaultCharset());
    }

    public String getId() {
        return this.id;
    }



}
