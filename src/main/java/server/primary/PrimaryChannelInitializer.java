package server.primary;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 * Created by jon on 5/20/17.
 */
public class PrimaryChannelInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) throws Exception {
//        ch.pipeline().addLast(new PersonDecoder());
//        ch.pipeline().addLast(new PersonEncoder());
        ch.pipeline().addLast(new IntDecoder());
        ch.pipeline().addLast(new IntEncoder());
        ch.pipeline().addLast(new PrimaryServerHandler());
    }
}
