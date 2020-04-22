/**
 * @author Petrykowski Maciej S19267
 */

package S_PASSTIME_SERVER1;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Client {

    private String host;
    private int port;
    private String id;
    private SocketChannel sc;

    public Client(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    public void connect() {
        try {
            sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(new InetSocketAddress(host, port));
            while (!sc.finishConnect()) {
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String send(String req) {
        try {
            sc.write(Charset.defaultCharset().encode(req + "\n"));
            String response = readResponse();
            return response;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String readResponse() throws IOException, InterruptedException {
        ByteBuffer inBuf = ByteBuffer.allocateDirect(1024);
        StringBuilder sb = new StringBuilder();
        char a = 0, b = 0;
        readLoop:
        while (true) {
            inBuf.clear();
            int readBytes = sc.read(inBuf);

            if (readBytes == 0) {
                Thread.sleep(10);
                continue;
            } else if (readBytes == -1) {
                break;
            } else {
                inBuf.flip();
                CharBuffer cbuf = Charset.defaultCharset().decode(inBuf);
                while (cbuf.hasRemaining()) {
                    char c = cbuf.get();
                    b = a;
                    a = c;
                    sb.append(c);
                    if (b == '\t' && a == '\t') break readLoop;
                }
            }
        }
        return sb.toString();
    }

    public String getId() {
        return id;
    }
}
