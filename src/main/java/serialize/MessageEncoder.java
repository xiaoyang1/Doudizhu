package serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Object> {

    private MessageCodecUtil util = null;

    public MessageEncoder(final MessageCodecUtil util){
        this.util = util;
    }


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, final Object msg, final ByteBuf byteBuf) throws Exception {
        util.encode(byteBuf, msg);
    }
}
