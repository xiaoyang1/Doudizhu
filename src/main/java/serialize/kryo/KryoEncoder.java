package serialize.kryo;


import serialize.MessageCodecUtil;
import serialize.MessageEncoder;

public class KryoEncoder extends MessageEncoder {

    public KryoEncoder(MessageCodecUtil util) {
        super(util);
    }
}
