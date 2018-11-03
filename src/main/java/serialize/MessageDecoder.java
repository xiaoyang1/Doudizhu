package serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageDecoder extends ByteToMessageDecoder {

    private final static int MESSAGE_LENGTH = MessageCodecUtil.MESSAGE_LENGTH;

    private MessageCodecUtil util = null;

    public MessageDecoder(MessageCodecUtil util){
        this.util = util;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //出现粘包导致消息头长度不对，直接返回
        if(in.readableBytes() < MessageDecoder.MESSAGE_LENGTH){
            return;
        }
        // System.out.println(in.readableBytes());
        // 先记录下当前的index
        in.markReaderIndex();
        int messageLength = in.readInt();

        // System.out.println("message length : " + messageLength);
        if(messageLength < 0){
            ctx.close();
        }

        if(in.readableBytes() < messageLength){
            //读到的消息长度和报文头的已知长度不匹配。那就重置一下ByteBuf读索引的位置
            // 因为netty是存在粘包的，所以不满足长度的话说明在下一个包，就重置读的索引
            // System.out.println("是不是这里出问题了？");
            // System.out.println(in.readableBytes() + " : " + messageLength);
            in.resetReaderIndex();
            return;
        } else {
            byte[] messageBody = new byte[messageLength];
            in.readBytes(messageBody);

            // 开始解码
            try{
                Object obj = util.decode(messageBody);
                out.add(obj);
            } catch (IOException e){
                Logger.getLogger(MessageDecoder.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
}
