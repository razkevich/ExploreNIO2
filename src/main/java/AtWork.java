import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class AtWork {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open(), ssc2 = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc2.configureBlocking(false);
        ServerSocket ss = ssc.socket(), ss2 = ssc2.socket();
        InetSocketAddress isa9999 = new InetSocketAddress(9999), isa9998 = new InetSocketAddress(9998);
        ss.bind(isa9999);
        ss2.bind(isa9998);
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        ssc2.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("listening on 9999 and 9998");
        while (true) {
            if (selector.select() == 0) {
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey selectionKey : selectionKeys) {
                if ((selectionKey.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                    SocketChannel sc = ((ServerSocketChannel) selectionKey.channel()).socket().accept().getChannel();
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);
                } else if ((selectionKey.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                    ByteBuffer bb = ByteBuffer.allocate(1000);
                    SocketChannel sc = (SocketChannel) selectionKey.channel();
                    int count = sc.read(bb);
                    if (count <= 0) {
                        continue;
                    }
                    String message = new String(bb.array(), 0, count);
                    System.out.println(message);
                    sc.write(ByteBuffer.wrap((String.valueOf(message.hashCode()) + " on port " + sc.socket().getChannel().getLocalAddress()).getBytes()));

                    sc.read(bb);
                }
            }
            selectionKeys.clear();
        }
    }
}
