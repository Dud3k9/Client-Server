/**
 * @author Petrykowski Maciej S19267
 */

package S_PASSTIME_SERVER1;


import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Server {

    private ServerSocketChannel ssc = null;
    private Selector selector = null;
    private boolean serverIsRunning = true;
    HashMap hostsLogs = new HashMap<String, StringBuilder>();
    HashMap isLoggedIn = new HashMap<String, Boolean>();
    StringBuilder serverLogs = new StringBuilder();

    public Server(String host, int port) {
        try {
            ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(host, port));
            ssc.configureBlocking(false);
            selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        new Thread(() -> {
            while (serverIsRunning) {
                try {
                    selector.select();
                    Set keys = selector.selectedKeys();

                    Iterator iter = keys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = (SelectionKey) iter.next();
                        iter.remove();

                        if (key.isAcceptable()) {
                            SocketChannel sc = ssc.accept();
                            sc.configureBlocking(false);
                            sc.register(selector, SelectionKey.OP_READ);
                            hostsLogs.put(sc.getRemoteAddress().toString(), new StringBuilder());
                            isLoggedIn.put(sc.getRemoteAddress().toString(), false);
                            continue;
                        }

                        if (key.isReadable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            srviceRequest(sc);
                            continue;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }

        }).start();
    }

    private void srviceRequest(SocketChannel sc) {
        if (!sc.isOpen()) return;
        try {
            String reqString = readReqest(sc);

            String[] req = reqString.split(" ");
            if (req[0].equals("bye") && req.length == 1)
                bye(sc, req);
            else if (req[0].equals("bye") && req[2].equals("log") && req.length == 4)
                byeAndLog(sc, req);
            else if (req[0].equals("login") && req.length == 2)
                login(sc, req);
            else if (req[0].matches("\\d{4}-\\d{2}-\\d{2}.*") &&
                    req[1].matches("\\d{4}-\\d{2}-\\d{2}.*"))
                dates(sc, req);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dates(SocketChannel sc, String[] req) throws IOException {
        String adress = sc.getRemoteAddress().toString();
        Boolean isLogged = (Boolean) isLoggedIn.get(adress);

        if (isLogged) {
            String[] name = ((StringBuilder) hostsLogs.get(adress)).toString().split(" ");
            serverLogsAppend(name[1] + " request at " + LocalTime.now().toString() + ": \"" + req[0] + " " + req[1] + "\"");
            hostLogsAppend(adress, "Request: " + req[0] + " " + req[1]);
            String response = Time.passed(req[0], req[1]);
            hostLogsAppend(adress, "Resault:\n" + response);
            sendResponse(sc, response);
        }
    }

    private void login(SocketChannel sc, String[] req) throws IOException {
        String adress = sc.getRemoteAddress().toString();
        serverLogsAppend(req[1] + " logged in at " + LocalTime.now().toString());
        if (((StringBuilder) hostsLogs.get(adress)).toString().length() < 1)
            hostLogsAppend(adress, "=== " + req[1] + " log start ===");
        hostLogsAppend(adress, "logged in");
        isLoggedIn.put(adress, true);
        sendResponse(sc, "logged in");
    }

    private void byeAndLog(SocketChannel sc, String[] req) throws IOException {
        String adress = sc.getRemoteAddress().toString();
        Boolean isLogged = (Boolean) isLoggedIn.get(adress);

        if (isLogged) {
            isLoggedIn.put(adress, false);
            String[] name = ((StringBuilder) hostsLogs.get(adress)).toString().split(" ");
            serverLogsAppend(name[1] + " logged out at " + LocalTime.now().toString());
            hostLogsAppend(adress, "logged out");
            sendResponse(sc, getHostLog(adress));
            sc.close();
        }
    }

    private void bye(SocketChannel sc, String[] req) throws IOException {
        String adress = sc.getRemoteAddress().toString();
        Boolean isLogged = (Boolean) isLoggedIn.get(adress);

        if (isLogged) {
            isLoggedIn.put(adress, false);
            String[] name = ((StringBuilder) hostsLogs.get(adress)).toString().split(" ");
            serverLogsAppend(name[1] + " logged out at " + LocalTime.now().toString());
            hostLogsAppend(adress, "logged out");
            sendResponse(sc, "logged out");
            sc.close();
        }
    }

    private String readReqest(SocketChannel sc) throws IOException {
        ByteBuffer bbuf = ByteBuffer.allocate(1024);
        StringBuffer reqString = new StringBuffer();

        readLoop:
        while (true) {
            int n = sc.read(bbuf);
            if (n > 0) {
                bbuf.flip();
                CharBuffer cbuf = Charset.defaultCharset().decode(bbuf);
                while (cbuf.hasRemaining()) {
                    char c = cbuf.get();
                    if (c == '\r' || c == '\n') break readLoop;
                    reqString.append(c);
                }
            }
        }
        return reqString.toString();
    }

    private void sendResponse(SocketChannel sc, String response) throws IOException {
        sc.write(Charset.defaultCharset().encode(response + "\t\t"));
    }

    public void stopServer() {
        setServerIsRunning(false);
        try {
//            selector.close();
            ssc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getServerLog() {
        return serverLogs.toString();
    }

    String getHostLog(String adress) {
        String logs = hostsLogs.get(adress).toString();
        String[] name = logs.split(" ");
        return logs + "=== " + name[1] + " log end ===";
    }

    void serverLogsAppend(String log) {
        serverLogs.append(log + "\n");
    }

    void hostLogsAppend(String adress, String log) {
        StringBuilder hostLogs = (StringBuilder) hostsLogs.get(adress);
        hostLogs.append(log + "\n");
    }

    public void setServerIsRunning(boolean serverIsRunning) {
        this.serverIsRunning = serverIsRunning;
    }
}
