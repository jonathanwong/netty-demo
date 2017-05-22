package server.primary;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by jon on 5/20/17.
 */
public class PersonDecoder extends ByteToMessageDecoder {

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        System.out.println("PersonDecoder - decode");
        if (in.readableBytes() < 95) {
            return;
        }

        System.out.println("readBytes");
        out.add(in.readBytes(95));
    }
}
