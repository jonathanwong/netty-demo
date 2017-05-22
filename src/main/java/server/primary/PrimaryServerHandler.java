package server.primary;

import client.secondary.SecondaryClientInitializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by jon on 5/20/17.
 */
public class PrimaryServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("PrimaryServerHandler - channelRead");
//        ctx.write(msg);   // normally send it to the encoder

        if (msg instanceof Integer) {
            if ((int) msg == 19) {
                connectSecondaryServer(ctx, msg);
//                ctx.fireChannelRead(msg);
            }
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        System.out.println("PrimaryServerHandler - channelReadComplete");
//        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void connectSecondaryServer(ChannelHandlerContext ctx, Object data) {
        System.out.println("PrimaryServerHandler - connectSecondaryServer");
        ctx.pipeline().addLast("secondaryClientInitializer", new SecondaryClientInitializer(data));
    }
}
