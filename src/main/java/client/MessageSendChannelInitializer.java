package client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import serialize.RpcSerializeProtocol;

public class MessageSendChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RpcSerializeProtocol protocol;
    private RpcSendSerializeFrame frame;
    private ClientProcessor processor;

    public MessageSendChannelInitializer(ClientProcessor processor) {
        this.processor = processor;
        frame = new RpcSendSerializeFrame(this.processor);
    }

    MessageSendChannelInitializer buildRpcSerializeProtocol(RpcSerializeProtocol protocol){
        this.protocol = protocol;
        return this;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        frame.select(protocol, pipeline);
    }
}
