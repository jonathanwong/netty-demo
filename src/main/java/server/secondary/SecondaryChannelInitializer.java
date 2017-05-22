package server.secondary;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 * Created by jon on 5/20/17.
 */
public class SecondaryChannelInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new EchoServerHandler());
    }
}
