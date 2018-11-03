package serialize;

import io.netty.channel.ChannelPipeline;

/**
 * 最后我们的NettyRPC框架要能自由地支配、定制Netty的RPC服务端、客户端，
 * 采用何种序列化来进行RPC消息对象的网络传输。因此，要再抽象一个RPC消息序列化协议选择器接口（RpcSerializeFrame）
 */
public interface RpcSerializeFrame {
    public void select(RpcSerializeProtocol protocol, ChannelPipeline pipeline);
}
