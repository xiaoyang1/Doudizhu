package server;


import com.esotericsoftware.kryo.pool.KryoPool;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import serialize.MessageCodecUtil;
import serialize.RpcSerializeFrame;
import serialize.RpcSerializeProtocol;
import serialize.kryo.KryoCodecUtil;
import serialize.kryo.KryoDecoder;
import serialize.kryo.KryoEncoder;
import serialize.kryo.KryoPoolFactory;
import serialize.protobuf.ProtostuffCodecUtil;
import serialize.protobuf.ProtostuffDecoder;
import serialize.protobuf.ProtostuffEncoder;

import java.util.Map;

public class RpcRecvSerializeFrame implements RpcSerializeFrame {


    public RpcRecvSerializeFrame() {
    }

    @Override
    public void select(RpcSerializeProtocol protocol, ChannelPipeline pipeline) {
        switch (protocol){
            case JDKSERIALIZE:{
                pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, MessageCodecUtil.MESSAGE_LENGTH, 0, MessageCodecUtil.MESSAGE_LENGTH));
                pipeline.addLast(new LengthFieldPrepender(MessageCodecUtil.MESSAGE_LENGTH));
                pipeline.addLast(new ObjectEncoder());
                pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
                pipeline.addLast(new MessageRecvHandler());
                break;
            }

            case PROTOSTUFFSERIALIZE:{
                ProtostuffCodecUtil util = new ProtostuffCodecUtil();
                pipeline.addLast(new ProtostuffEncoder(util));
                pipeline.addLast(new ProtostuffDecoder(util));
                pipeline.addLast(new MessageRecvHandler());
                break;
            }
            case KRYOSERIALIZE:{
                KryoPool pool = KryoPoolFactory.getKryoPoolInstance();
                KryoCodecUtil util = new KryoCodecUtil(pool);
                pipeline.addLast(new KryoEncoder(util));
                pipeline.addLast(new KryoDecoder(util));
                pipeline.addLast(new MessageRecvHandler());
                break;
            }

            default:{
                break;
            }
        }
    }
}
