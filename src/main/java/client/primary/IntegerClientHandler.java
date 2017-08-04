package client.primary;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by jon on 5/20/17.
 */
public class IntegerClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ByteBuf someInt = ctx.alloc().buffer(4);
        someInt.writeInt(19);
        ctx.writeAndFlush(someInt);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("IntegerClientHandler - channelRead");
        Integer msgAsInt = ((ByteBuf) msg).getInt(0);
        System.out.println("IntegerClientHandler - Message: " + (int) msgAsInt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
