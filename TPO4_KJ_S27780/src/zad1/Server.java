package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Server extends Thread {

    private Map<SocketChannel, String> userMaps;
    private Map<String, StringBuilder> logs;
    private StringBuilder serverLog;
    private String host;
    private int port;

    private ServerSocketChannel channel;
    private Selector selector;
    private volatile boolean isRunning = true;

    Server(String host, int port) {
        userMaps = new HashMap<>();
        logs = new HashMap<>();
        serverLog = new StringBuilder();
        this.host = host;
        this.port = port;
    }

    public void startServer() throws IOException {
        try {
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(host, port));

            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_ACCEPT);

            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopServer() {
        isRunning = false;
    }

    @Override
    public void run() {
        try {
            handleConnections();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                selector.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void handleConnections() throws IOException {

        while (isRunning) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
                    SocketChannel accept = channel.accept();
                    accept.configureBlocking(false);
                    accept.register(selector, SelectionKey.OP_READ);
                    continue;
                }

                if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    socketChannel.configureBlocking(false);
                    handleRequset(socketChannel);

                }
            }
        }
    }

    public void handleRequset(SocketChannel socketChannel) throws IOException {
        if (!socketChannel.isOpen()) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        int bytesRead = socketChannel.read(buffer);
        if (bytesRead == -1) {
            socketChannel.close();
        }
        if (bytesRead > 0) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String request = new String(bytes);
            writeResponse(socketChannel, request);

        }

    }

    public void writeResponse(SocketChannel socketChannel, String request) throws IOException {
        String response;
        if (request.charAt(0) == 'l') { // login
            String[] words = request.split(" ");
            putNewUserInDB(strip(words[1]), socketChannel);
            response = login(strip(words[1]));
        } else {
            response = getResponse(request, getIdByChannel(socketChannel));
        }
        ByteBuffer encode = Charset.defaultCharset().encode(response);
        socketChannel.write(encode);
    }

    private String getResponse(String request, String id) {
        updateGeneralLog(request, id);
        updatePersonalLog(request, id);

        if (request.equals("bye")) {
            return "logged out";
        } else {
            String[] words = request.split(" ");
            if (words[0].equals("login")) {
                return "logged in";
            } else if (words[0].equals("bye")) {
                return getLogs().get(id).toString();
            } else {
                return Time.passed(strip(words[0]), strip(words[1]));
            }
        }
    }

    private void updatePersonalLog(String request, String id) {
        if (request.charAt(0) == 'l') {
            logs.put(id, new StringBuilder());
            logs.get(id).append("=== ").append(id).append(" log start ===\nlogged in\n");
        } else if (request.charAt(0) == 'b') {
            logs.get(id).append("logged out\n=== ").append(id).append(" log end ===\n");
        } else {
            logs.get(id).append("Request: ").append(request).append("Result:\n");
            String[] dates = request.split(" ");
            logs.get(id).append(Time.passed(strip(dates[0]), strip(dates[1]))).append('\n');
        }
    }

    private void updateGeneralLog(String request, String id) {
        LocalTime now = LocalTime.now();
        String hourMinuteSecond = now.toString().substring(0, 9);
        String nanoSeconds = (now.getNano() + "").substring(0, 3);
        String time = hourMinuteSecond + nanoSeconds;
        if (request.charAt(0) == 'l') {
            serverLog.append(id).append(" logged in at ").append(time).append('\n');
        } else if (request.charAt(0) == 'b') {
            serverLog.append(id).append(" logged out at ").append(time).append('\n');

        } else {
            String s = request.replaceAll("\r\n", "");
            serverLog.append(id).append(" request at ").
                    append(time).append(": \"").append(s).append("\"").append("\n");
        }
    }

    private String login(String id) {
        updatePersonalLog("login", id);
        updateGeneralLog("login", id);
        return "logged in";
    }

    private void putNewUserInDB(String id, SocketChannel socketChannel) {
        userMaps.put(socketChannel, id);
        logs.put(id, new StringBuilder());
    }

    private String getIdByChannel(SocketChannel socketChannel) {
        return userMaps.get(socketChannel);
    }

    private String strip(String string) {
        return string.replaceAll("\\s", "");
    }

    public Map<SocketChannel, String> getUserMaps() {
        return userMaps;
    }

    public void setUserMaps(Map<SocketChannel, String> userMaps) {
        this.userMaps = userMaps;
    }

    public Map<String, StringBuilder> getLogs() {
        return logs;
    }

    public void setLogs(Map<String, StringBuilder> logs) {
        this.logs = logs;
    }

    public StringBuilder getServerLog() {
        return serverLog;
    }

    public void setServerLog(StringBuilder serverLog) {
        this.serverLog = serverLog;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws IOException {
        Server localhost = new Server("localhost", 7777);
        localhost.startServer();
    }

}
