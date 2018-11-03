package server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import serialize.RpcSerializeProtocol;

public class MessageRecvInitializer extends ChannelInitializer<SocketChannel> {

    private RpcSerializeProtocol protocol;
    private RpcRecvSerializeFrame frame = null;

    public MessageRecvInitializer() {
        frame = new RpcRecvSerializeFrame();
    }

    MessageRecvInitializer buildRpcSerializeProtocol(RpcSerializeProtocol protocol){
        this.protocol = protocol;
        return this;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        frame.select(protocol, pipeline);
    }
}
