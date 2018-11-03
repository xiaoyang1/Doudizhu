package serialize;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

public interface MessageCodecUtil {
    final static int MESSAGE_LENGTH = 4;

    void encode(final ByteBuf out, final Object object) throws IOException;

    Object decode(byte[] body) throws IOException;
}
