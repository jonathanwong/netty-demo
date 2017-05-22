package server.primary;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import model.Person;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Created by jon on 5/20/17.
 */
public class PersonEncoder extends MessageToByteEncoder<Person> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Person msg, ByteBuf out) throws Exception {
        System.out.println("PersonEncoder - encode");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (ObjectOutput output = new ObjectOutputStream(byteArrayOutputStream)) {
            output.writeObject(msg);
            output.flush();
            byte[] bytes = byteArrayOutputStream.toByteArray();

            out.writeBytes(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
