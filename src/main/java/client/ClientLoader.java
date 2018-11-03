package client;

import entity.User;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import serialize.RpcSerializeProtocol;

import java.net.InetSocketAddress;

public class ClientLoader {
    private volatile static ClientLoader clientLoader;
    private final static String DELIMITER = ":";
    //默认采用Java原生序列化协议方式传输RPC消息
    private RpcSerializeProtocol serializeProtocol = RpcSerializeProtocol.PROTOSTUFFSERIALIZE;

    //方法返回到Java虚拟机的可用的处理器数量
    private final static int parallel = Runtime.getRuntime().availableProcessors() * 2;
    //netty nio线程池
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(parallel);

    public ClientLoader() {
    }


    public void load(String serverAddress, RpcSerializeProtocol serializeProtocol, ClientProcessor processor) throws Exception{
        String[] ipAddr = serverAddress.split(ClientLoader.DELIMITER);
        if(ipAddr.length == 2){
            String host = ipAddr[0];
            int port = Integer.parseInt(ipAddr[1]);
            final InetSocketAddress remoteAddr = new InetSocketAddress(host, port);

            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new MessageSendChannelInitializer(processor).buildRpcSerializeProtocol(serializeProtocol));

            ChannelFuture future = bootstrap.connect(remoteAddr).sync();
            processor.setChannel(future.channel());
        }
    }

    public void unLoad(ClientProcessor processor) {
        processor.setChannel(null);
        eventLoopGroup.shutdownGracefully();
    }
}
