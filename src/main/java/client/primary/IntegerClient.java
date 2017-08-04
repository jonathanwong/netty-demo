package client.primary;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by jon on 5/20/17.
 */
public class IntegerClient {

    static final String HOST = "localhost";
    static final int PORT = 3010;

    private String host;
    private int port;

    public IntegerClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        new IntegerClient(HOST, PORT).start();
    }

    public void start() throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IntegerClientHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
