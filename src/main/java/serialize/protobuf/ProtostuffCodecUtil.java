package serialize.protobuf;


import com.google.common.io.Closer;
import io.netty.buffer.ByteBuf;
import serialize.MessageCodecUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class ProtostuffCodecUtil implements MessageCodecUtil {

    private static Closer closer = Closer.create();
    private ProtostuffSerializePool pool = ProtostuffSerializePool.getProtostuffPoolInstance();
    private boolean rpcDirect =false;

    public boolean isRpcDirect() {
        return rpcDirect;
    }

    public void setRpcDirect(boolean rpcDirect) {
        this.rpcDirect = rpcDirect;
    }

    @Override
    public void encode(ByteBuf out, Object object) throws IOException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            closer.register(byteArrayOutputStream);
            ProtostuffSerialize protostuffSerialize = pool.borrow();
            protostuffSerialize.serialize(byteArrayOutputStream, object);
            byte[] body = byteArrayOutputStream.toByteArray();
            int messageLength = body.length;
            out.writeInt(messageLength);
            out.writeBytes(body);
            pool.restore(protostuffSerialize);
        } finally {
            closer.close();
        }

    }

    @Override
    public Object decode(byte[] body) throws IOException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
            closer.register(byteArrayInputStream);
            ProtostuffSerialize protostuffSerialize = pool.borrow();
            Object result = protostuffSerialize.deserialize(byteArrayInputStream);
            pool.restore(protostuffSerialize);
            return result;
        } finally {
            closer.close();
        }
    }
}
