package client;

import serialize.RpcSerializeProtocol;

public class MessageSendExecutor {
    private ClientLoader loader = new ClientLoader();

    private ClientProcessor processor;
    public MessageSendExecutor() {
    }

    public MessageSendExecutor(ClientProcessor processor) {
        this.processor = processor;
    }

    public void setClientLoader(String serverAddress, RpcSerializeProtocol protocol){

        try {
            loader.load(serverAddress, protocol, processor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        loader.unLoad(processor);
    }


}
