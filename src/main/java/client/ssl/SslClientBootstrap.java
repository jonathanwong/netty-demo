package client.ssl;

import client.primary.IntegerClient;
import client.primary.IntegerClientHandler;
import crypto.CryptoUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * Created by jon on 8/4/17.
 */
public class SslClientBootstrap {

    static final String HOST = "localhost";
    static final int PORT = 3010;

    private String host;
    private int port;

    public SslClientBootstrap(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        new SslClientBootstrap(HOST, PORT).start();
    }

    public void start() throws Exception {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(CryptoUtils.TRUST_STORE_NAME + ".jks"), CryptoUtils.TRUST_STORE_PASSWORD);
        trustManagerFactory.init(trustStore);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(CryptoUtils.CLIENT_NAME + ".jks"), CryptoUtils.CLIENT_PASSWORD);
        keyManagerFactory.init(keyStore, CryptoUtils.CLIENT_PASSWORD);

        final SslContext sslContext = SslContextBuilder.forClient().trustManager(trustManagerFactory).keyManager(keyManagerFactory).build();

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
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
