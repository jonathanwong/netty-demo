package client.secondary;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

/**
 * Created by jon on 5/21/17.
 */
public class SecondaryClientConnectHandler extends ChannelInboundHandlerAdapter {

    private String host;
    private int port;
    private Object data;
    private Channel outboundChannel;

    public SecondaryClientConnectHandler(String host, int port, Object data) {
        this.host = host;
        this.port = port;
        this.data = data;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("SecondaryClientConnectHandler - channelActive");

        final Channel inboundChannel = ctx.channel();

        // start connection attempt
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new SecondaryClientServerHandler(inboundChannel, data));

        ChannelFuture channelFuture = bootstrap.connect(host, port);
        outboundChannel = channelFuture.channel();
        channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
            if (channelFuture1.isSuccess()) {
                System.out.println("SecondaryClientConnectHandler - connected to " + host + ":" + port);
            } else {
                inboundChannel.close();
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("SecondaryClientConnectHandler - channelRead");

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        System.out.println("SecondaryClientConnectHandler - channelReadComplete");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
