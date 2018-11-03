package startup;

import server.MessageRecvExecutor;

public class ServerStart {
    public static void main(String[] args) {
        MessageRecvExecutor server = new MessageRecvExecutor("127.0.0.1:18888", "KRYOSERIALIZE");
    }
}
