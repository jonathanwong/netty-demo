package server.primary;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by jon on 5/20/17.
 */
public class IntDecoder extends ByteToMessageDecoder {

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        System.out.println("IntDecoder - decode");
        if (in.readableBytes() < 4) {
            System.out.println("IntDecoder - not enough bytes");
            return;
        }
        System.out.println("IntDecoder - got enough bytes");

        ByteBuf b = in.readBytes(4);
        int result = b.getInt(0);

        out.add(new Integer(result));
    }
}
