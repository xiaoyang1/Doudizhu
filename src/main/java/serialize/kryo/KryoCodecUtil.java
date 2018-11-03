package serialize.kryo;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.io.Closer;
import io.netty.buffer.ByteBuf;
import serialize.MessageCodecUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KryoCodecUtil implements MessageCodecUtil {

    private KryoPool pool;

    private static Closer closer = Closer.create();

    public KryoCodecUtil(KryoPool pool) {
        this.pool = pool;
    }

    @Override
    public void encode(ByteBuf out, Object object) throws IOException {
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            closer.register(byteArrayOutputStream);
            KryoSerialize kryoSerialize = new KryoSerialize(pool);
            kryoSerialize.serialize(byteArrayOutputStream, object);
            byte[] body = byteArrayOutputStream.toByteArray();
            int messageLength = body.length;
            // System.out.println("encode message  length :" + messageLength);
            out.writeInt(messageLength);
            out.writeBytes(body);
        } finally {
            closer.close();
        }
    }

    @Override
    public Object decode(byte[] body) throws IOException {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(body);
            closer.register(in);
            KryoSerialize kryoSerialize = new KryoSerialize(pool);
            Object result = kryoSerialize.deserialize(in);
            return result;
        }finally {
            closer.close();
        }
    }
}
