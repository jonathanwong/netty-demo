package client.secondary;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by jon on 5/21/17.
 */
public class SecondaryClientServerHandler extends ChannelInboundHandlerAdapter {

    private final Channel inboundChannel;
    private Object data;

    public SecondaryClientServerHandler(Channel inboundChannel, Object data) {
        this.inboundChannel = inboundChannel;
        this.data = data;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("SecondaryClientServerHandler - channelActive");
        System.out.println("send some data here");
        ByteBuf someInt = ctx.alloc().buffer(4);
        int newValue = (int) data * 2;
        someInt.writeInt(newValue);
        ctx.writeAndFlush(someInt);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("SecondaryClientServerHandler - channelRead");

        ByteBuf b = ((ByteBuf) msg).readBytes(4);
        int result = b.getInt(0);

        System.out.println("Received: " + result);
    }
}
