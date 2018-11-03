package serialize.protobuf;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;

import message.PokeMessage;
import message.UserActionMessage;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import serialize.RpcSerialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProtostuffSerialize implements RpcSerialize {

    private static SchemaCache cachedSchema = SchemaCache.getInstance();
    private static Objenesis objenesis = new ObjenesisStd(true);
    private boolean isUserActionMessage = false;

    public boolean isUserActionMessage() {
        return isUserActionMessage;
    }

    public void getIsUserActionMessage(boolean isUserActionMessage) {
        this.isUserActionMessage = isUserActionMessage;
    }

    public static <T> Schema<T> getSchema(Class<T> cls){
        return (Schema<T>) cachedSchema.get(cls);
    }

    @Override
    public void serialize(OutputStream output, Object object) throws IOException {
        Class cls = (Class) object.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try{
            Schema schema = getSchema(cls);
            ProtostuffIOUtil.writeTo(output, object, schema, buffer);
        } catch (Exception e){
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public Object deserialize(InputStream input) throws IOException {
        try {
            Class cls = isUserActionMessage() ? UserActionMessage.class : PokeMessage.class;
            Object message = (Object) objenesis.newInstance(cls);
            Schema<Object> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(input, message, schema);
            return message;
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
