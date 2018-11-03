package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageRecvHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(MessageRecvHandler.class);
    public MessageRecvHandler() {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        MessageRecvInitializeTask recvTask = new MessageRecvInitializeTask(message, ctx);
        MessageRecvExecutor.submit(recvTask);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
