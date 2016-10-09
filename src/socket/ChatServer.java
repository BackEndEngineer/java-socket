package socket;

import com.yongboy.socketio.MainServer;
import socket.ChatHandler;

/**
 * Desc:
 * Mail: hehaiyang@terminus.io
 * Date: 2016/10/9
 */
public class ChatServer {
    public ChatServer() {
    }

    public static void main(String[] args) {
        MainServer chatServer = new MainServer(new ChatHandler(), 9000);
        chatServer.start();
    }
}
