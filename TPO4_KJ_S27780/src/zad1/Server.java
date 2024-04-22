/**
 *
 *  @author Karwowski Jakub S27780
 *
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.sql.SQLOutput;
import java.util.Iterator;
import java.util.Set;

public class Server extends Thread{
    private String host;
    private int port;
    private volatile boolean isActive;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public Server(String host, int port){
        this.host=host;
        this.port=port;
    }

    // uruchamia serwer w oddzielnym wątku
    public void startServer() {
        isActive=true;
        try {
            this.serverSocketChannel=ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(host, port));
            this.selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            this.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            serviceConnections(serverSocketChannel,selector);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    private void serviceConnections(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {

        while(isActive){
            selector.select();

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            String response=null;
            while(iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();


                if(key.isAcceptable()){
                    SocketChannel socketChannel = serverSocketChannel.accept();

                    socketChannel.configureBlocking(false);

                    socketChannel.register(selector,SelectionKey.OP_READ);
                    continue;
                }
                if(key.isReadable()){
                    System.out.println("weszlismy do readable");
                    SocketChannel socketChannel =(SocketChannel) key.channel();
                     response = handleRequest(socketChannel);
                    socketChannel.register(selector,SelectionKey.OP_WRITE);
                }
                if(key.isWritable()){
                    SocketChannel socketChannel =(SocketChannel) key.channel();
                    ByteBuffer wrap = ByteBuffer.wrap(response.getBytes(Charset.defaultCharset()));
                    socketChannel.write(wrap);

                }

            }
        }
    }
    private String handleRequest(SocketChannel socketChannel) throws IOException {
         ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
         String request;
         if(!socketChannel.isOpen()){
             return null;
         }
         byteBuffer.clear();

        int read = socketChannel.read(byteBuffer);
        System.out.println("READ:"+read);
        if(read==-1){
            socketChannel.close();
        }
        if(read > 0){
            byteBuffer.flip();
            CharBuffer charBuffer = Charset.defaultCharset().decode(byteBuffer);

            request = charBuffer.toString();
            return handleSpecificCommands(request);
        }

    return null;
    }
    private String handleSpecificCommands(String command){
        if(command.equals("bye")){
            return "logged out";
        } else if(command.equals("bye and log transfer")){
            //toDo wyslanie logu klienta tutaj
        } else{
            String[] words = command.split(" ");
            if(words[0].equals("login")){
                return "logged in";
            } else{
                return Time.passed(words[0], words[1]);
            }
        }
        return "wrong input";
    }
    // zatrzymuje działanie serwera i wątku w którym działa
    public void stopServer(){
        isActive=false;
    }
    // zwraca ogólny log serwera
     String getServerLog(){
        return null;
     }
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }


}
