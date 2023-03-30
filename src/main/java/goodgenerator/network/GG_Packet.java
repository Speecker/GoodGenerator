package goodgenerator.network;

import gregtech.api.net.GT_Packet_New;

public abstract class GG_Packet extends GT_Packet_New {

    public GG_Packet(boolean aIsReference) {
        super(aIsReference);
    }
    
}
