package server.ssl;

import crypto.CryptoUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import server.secondary.EchoServerHandler;

import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

/**
 * Created by jon on 8/4/17.
 */
public class SslServerBootstrap {
    static final String HOST = "localhost";
    static final int PORT = 3010;

    private String host;
    private int port;

    public SslServerBootstrap(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        new SslServerBootstrap(HOST, PORT).start();
    }

    public void start() throws Exception {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(CryptoUtils.TRUST_STORE_NAME + ".jks"), CryptoUtils.TRUST_STORE_PASSWORD);
        trustManagerFactory.init(trustStore);

        // 1) If you created the new keys and certs
        SslContext sslContext = SslContextBuilder.forServer(new FileInputStream(new File(CryptoUtils.ROOT_CERT)), new FileInputStream(new File(CryptoUtils.ROOT_KEY)), null)
                .clientAuth(ClientAuth.REQUIRE)
                .trustManager(trustManagerFactory)
                .build();

        // 2) If you used existing keys and certs
//        SslContext sslContext = SslContextBuilder.forServer(new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "mycert.pem")), new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "mykey.pem")), null)
//                .clientAuth(ClientAuth.REQUIRE)
//                .trustManager(trustManagerFactory)
//                .build();

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
                            ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(host, port)).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        }
    }
}
