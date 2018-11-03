package serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;

import message.ACKMessage;
import message.NAKMessage;
import message.PokeMessage;
import message.UserActionMessage;
import org.objenesis.strategy.StdInstantiatorStrategy;

public class KryoPoolFactory {
    private static KryoPoolFactory poolFactory = null;

    public KryoFactory factory = new KryoFactory() {
        @Override
        public Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setReferences(false);
            // 把已知的结构注册到Kryo注册器里面，提高序列化/反序列化效率
            kryo.register(PokeMessage.class);
            kryo.register(UserActionMessage.class);
            kryo.register(ACKMessage.class);
            kryo.register(NAKMessage.class);
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }
    };

    private KryoPool pool = new KryoPool.Builder(factory).build();

    private KryoPoolFactory() {
    }

    public static KryoPool getKryoPoolInstance(){
        if(poolFactory == null){
            synchronized (KryoPoolFactory.class) {
                if (poolFactory == null) {
                    poolFactory = new KryoPoolFactory();
                }
            }
        }
        return poolFactory.getPool();
    }

    private KryoPool getPool(){
        return pool;
    }
}
