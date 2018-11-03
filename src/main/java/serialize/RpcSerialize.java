package serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 针对不同编解码序列化的框架（这里主要是指Kryo、Hessian），再抽象、
 * 萃取出一个RPC消息序列化/反序列化接口（RpcSerialize）、RPC消息编解码接口（MessageCodecUtil）
 */
public interface RpcSerialize {

    void serialize(OutputStream output, Object object) throws IOException;

    Object deserialize(InputStream input) throws IOException;
}
