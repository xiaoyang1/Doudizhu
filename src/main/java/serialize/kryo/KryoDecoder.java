package serialize.kryo;


import serialize.MessageCodecUtil;
import serialize.MessageDecoder;

public class KryoDecoder extends MessageDecoder {
    public KryoDecoder(MessageCodecUtil util) {
        super(util);
    }
}
