package server;

import com.google.common.util.concurrent.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import message.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parallel.NamedThreadFactory;
import parallel.TaskThreadPool;
import serialize.RpcSerializeProtocol;

import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *  这个类是服务器 的类， 包括启动等，最好用spring 写成 单例的 Bean， 在这里，为了简便，就省略了。
 */
public class MessageRecvExecutor {
    private static Logger logger = LoggerFactory.getLogger(MessageRecvExecutor.class);

    private String serverAddress;

    private RpcSerializeProtocol serializeProtocol = RpcSerializeProtocol.PROTOSTUFFSERIALIZE;

    private final static String DELIMITER = ":";

    private static ListeningExecutorService threadPoolExecutor;

    public MessageRecvExecutor(String serverAddress, String serializeProtocol) {
        this.serverAddress = serverAddress;
        this.serializeProtocol = Enum.valueOf(RpcSerializeProtocol.class, serializeProtocol);
        try {
            init();
        }catch (Exception e){
            logger.error("server start exception!");
        }

    }

    private void init() throws Exception{
        //netty的线程池模型设置成主从线程池模式，这样可以应对高并发请求
        //当然netty还支持单线程、多线程网络IO模型，可以根据业务需求灵活配置
        ThreadFactory threadRpcFactory = new NamedThreadFactory("Netty-DouDiZhu-ThreadFactory");

        //方法返回到Java虚拟机的可用的处理器数量
        int parallel = Runtime.getRuntime().availableProcessors() * 2;

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup(parallel, threadRpcFactory, SelectorProvider.provider());

        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MessageRecvInitializer().buildRpcSerializeProtocol(serializeProtocol))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] ipAddr =  serverAddress.split(DELIMITER);

            if(ipAddr.length == 2){
                String host = ipAddr[0];
                int port = Integer.parseInt(ipAddr[1]);

                ChannelFuture future = bootstrap.bind(host, port).sync();
                System.out.printf("[author me] Netty RPC Server start success!\nip:%s\nport:%d\nprotocol:%s\n\n",
                        host, port, serializeProtocol);
                future.channel().closeFuture().sync();
            }else {
                System.out.printf("[author me] Netty RPC Server start fail!\n");
            }
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void submit(Callable<Boolean> task){
        if(threadPoolExecutor == null){
            synchronized (MessageRecvExecutor.class){
                if(threadPoolExecutor == null){
                    threadPoolExecutor = MoreExecutors.listeningDecorator(
                            (ThreadPoolExecutor) TaskThreadPool.getExecutor(16, 1)
                    );
                }
            }
        }

        ListenableFuture<Boolean> future = threadPoolExecutor.submit(task);
        Futures.addCallback(future, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        }, threadPoolExecutor);
    }
}
