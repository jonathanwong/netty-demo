package client.primary;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import model.Person;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Created by jon on 5/20/17.
 */
public class PersonClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
//        Person person = new Person("jon", "wong");
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        try (ObjectOutput output = new ObjectOutputStream(byteArrayOutputStream)) {
//            output.writeObject(person);
//            output.flush();
//            byte[] bytes = byteArrayOutputStream.toByteArray();
//
//            System.out.println("Sending person object from client");
//            final ChannelFuture channelFuture = ctx.writeAndFlush(Unpooled.wrappedBuffer(bytes));
//            channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
//                if (channelFuture1.isSuccess()) {
//                    assert channelFuture == channelFuture1;
////                  ctx.close();
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        ByteBuf someInt = ctx.alloc().buffer(4);
        someInt.writeInt(19);
        ctx.writeAndFlush(someInt);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("PersonClientHandler - channelRead");
        Integer msgAsInt = ((ByteBuf) msg).getInt(0);
        System.out.println("PersonClientHandler - Message: " + (int) msgAsInt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
