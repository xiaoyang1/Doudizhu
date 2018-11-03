package client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import message.Message;

public class MessageSendHandler extends ChannelInboundHandlerAdapter {

    private ClientProcessor processor;

    public MessageSendHandler(ClientProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        // ClientMessageTask task = new ClientMessageTask(message);
        processor.process(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
