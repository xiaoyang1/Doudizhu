package client;

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

public class RpcSendSerializeFrame implements RpcSerializeFrame {

    private ClientProcessor processor;

    public RpcSendSerializeFrame(ClientProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void select(RpcSerializeProtocol protocol, ChannelPipeline pipeline) {
        switch (protocol){
            case JDKSERIALIZE: {
                pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, MessageCodecUtil.MESSAGE_LENGTH, 0, MessageCodecUtil.MESSAGE_LENGTH));
                pipeline.addLast(new LengthFieldPrepender(MessageCodecUtil.MESSAGE_LENGTH));
                pipeline.addLast(new ObjectEncoder());
                pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
                pipeline.addLast(new MessageSendHandler(processor));
                break;
            }
            /**
             *  由于 PROTOSTUFFSERIALIZE ，在反序列时需要知道类名， 所以如果采用这个，还需要修改encode 和decode， 而外写一个字段，记录对应的类，否则序列化出问题，
             *  导致无法解析，程序卡死， 为了避免这个问题，所以采用了 KRYOSERIALIZE， 或者 Hessian。
             */
            case PROTOSTUFFSERIALIZE: {
                ProtostuffCodecUtil util = new ProtostuffCodecUtil();
                pipeline.addLast(new ProtostuffEncoder(util));
                pipeline.addLast(new ProtostuffDecoder(util));
                pipeline.addLast(new MessageSendHandler(processor));
                break;
            }
            case KRYOSERIALIZE: {
                KryoPool pool = KryoPoolFactory.getKryoPoolInstance();
                KryoCodecUtil util = new KryoCodecUtil(pool);
                pipeline.addLast(new KryoEncoder(util));
                pipeline.addLast(new KryoDecoder(util));
                pipeline.addLast(new MessageSendHandler(processor));
                break;
            }
            default: {
                break;
            }
        }
    }
}
