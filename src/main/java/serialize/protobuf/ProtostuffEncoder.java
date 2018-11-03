package serialize.protobuf;


import serialize.MessageCodecUtil;
import serialize.MessageEncoder;

public class ProtostuffEncoder extends MessageEncoder {
    public ProtostuffEncoder(MessageCodecUtil util) {
        super(util);
    }
}
