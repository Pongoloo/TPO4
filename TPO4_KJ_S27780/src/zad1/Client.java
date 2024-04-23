/**
 * @author Karwowski Jakub S27780
 */

package zad1;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Client {

    private String host;
    private int port;
    private String id;

    Socket socket;
    PrintWriter out;
    BufferedReader in;


    public Client(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;


    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String send(String req) {
        out.println(req);
        return readResponse();
    }

    private String readResponse() {
        CharBuffer allocate = CharBuffer.allocate(1024);
        try {
            int read = in.read(allocate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //toDo potecjalnie ify obslugujace czy read==-1 i read==0
        // still works so fuck it B)
        allocate.flip();
        return allocate.toString();
    }

    public String getId() {
        return this.id;
    }


}
