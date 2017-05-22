package server.secondary;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.primary.PrimaryChannelInitializer;

import java.net.InetSocketAddress;

/**
 * Created by jon on 5/20/17.
 */
public class SecondaryServerBootstrap {

    static final String HOST = "localhost";
    static final int PORT = 3020;

    private String host;
    private int port;

    public SecondaryServerBootstrap(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        new SecondaryServerBootstrap(HOST, PORT).start();
    }

    public void start() throws Exception {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();

        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("SecondaryServerBootstrap - initChannel");
                            ch.pipeline().addLast(new SecondaryChannelInitializer());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(host, port)).sync();
            channelFuture.channel().closeFuture().sync();
//            channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
//                if (channelFuture1.isSuccess()) {
//                    System.out.println("secondary server bound");
//                } else {
//                    System.out.println("error secondary server");
//                    channelFuture1.cause().printStackTrace();
//                }
//            });
        } finally {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        }
    }
}
