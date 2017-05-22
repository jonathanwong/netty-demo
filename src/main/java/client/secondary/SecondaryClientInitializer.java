package client.secondary;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by jon on 5/21/17.
 */
public class SecondaryClientInitializer extends ChannelInitializer<SocketChannel> {

    private Object data;

    public SecondaryClientInitializer(Object data) {
        this.data = data;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        System.out.println("SecondaryClientInitializer - initChannel");
        ch.pipeline().addLast(new SecondaryClientConnectHandler("localhost", 3020, data));

        System.out.println("fireChannelActive");
        ch.pipeline().context("secondaryClientInitializer").fireChannelActive();
    }
}
