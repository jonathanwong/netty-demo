package server.primary;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by jon on 5/20/17.
 */
public class IntEncoder extends MessageToByteEncoder<Integer> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Integer msg, ByteBuf out) throws Exception {
        System.out.println("IntEncoder - encode");
        ByteBuf encoded = ctx.alloc().buffer(4);
        encoded.writeInt(msg);
        out.writeBytes(encoded);
    }
}
