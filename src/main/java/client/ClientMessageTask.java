package client;

import message.Message;

import java.util.concurrent.Callable;

public class ClientMessageTask implements Callable<Boolean> {

    private Message message;

    public ClientMessageTask(Message message) {
        this.message = message;
    }

    @Override
    public Boolean call() throws Exception {
        return null;
    }
}
