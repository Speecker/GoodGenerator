package goodgenerator.network;

import java.util.EnumMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import goodgenerator.main.GoodGenerator;
import gregtech.api.enums.GT_Values;
import gregtech.api.net.GT_Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageCodec;

@ChannelHandler.Sharable
@SuppressWarnings("deprecation")
public class GG_Network extends MessageToMessageCodec<FMLProxyPacket, GG_Packet> implements IGG_NetworkHandler {

    private final EnumMap<Side, FMLEmbeddedChannel> mChannel;
    private final GG_Packet[] mSubChannels;

    public GG_Network() {
        this(GoodGenerator.MOD_ID, new YottaFluidTankPacket() // 0
        );
    }

    public GG_Network(String channelName, GG_Packet... packetTypes) {
        this.mChannel = NetworkRegistry.INSTANCE.newChannel(channelName, this, new HandlerShared());
        this.mSubChannels = new GG_Packet[packetTypes.length];
        for (GG_Packet packetType : packetTypes) {
            final int pId = packetType.getPacketID();
            if (this.mSubChannels[pId] == null) this.mSubChannels[pId] = packetType;
            else throw new IllegalArgumentException("Duplicate Packet ID! " + pId);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext aContext, GG_Packet aPacket, List<Object> aOutput) throws Exception {
        final ByteBuf tBuf = Unpooled.buffer().writeByte(aPacket.getPacketID());
        aPacket.encode(tBuf);
        aOutput.add(new FMLProxyPacket(tBuf, aContext.channel().attr(NetworkRegistry.FML_CHANNEL).get()));
    }

    @Override
    protected void decode(ChannelHandlerContext aContext, FMLProxyPacket aPacket, List<Object> aOutput)
            throws Exception {
        final ByteArrayDataInput aData = ByteStreams.newDataInput(aPacket.payload().array());
        final GT_Packet tPacket = this.mSubChannels[aData.readByte()].decode(aData);
        tPacket.setINetHandler(aPacket.handler());
        aOutput.add(tPacket);
    }

    @Override
    public void sendToPlayer(GG_Packet aPacket, EntityPlayerMP aPlayer) {
        if (aPacket == null) {
            return;
        }
        if (aPlayer == null) {
            return;
        }
        this.mChannel.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
                .set(FMLOutboundHandler.OutboundTarget.PLAYER);
        this.mChannel.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(aPlayer);
        this.mChannel.get(Side.SERVER).writeAndFlush(aPacket);
    }

    @Override
    public void sendToAllAround(GG_Packet aPacket, NetworkRegistry.TargetPoint aPosition) {
        this.mChannel.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
                .set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        this.mChannel.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(aPosition);
        this.mChannel.get(Side.SERVER).writeAndFlush(aPacket);
    }

    public void sendToServer(GG_Packet aPacket) {
        this.mChannel.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET)
                .set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        this.mChannel.get(Side.CLIENT).writeAndFlush(aPacket);
    }

    @Override
    public void sendPacketToAllPlayersInRange(World aWorld, GG_Packet aPacket, int aX, int aZ) {
        if (!aWorld.isRemote) {
            for (Object tObject : aWorld.playerEntities) {
                if (!(tObject instanceof EntityPlayerMP)) {
                    break;
                }
                EntityPlayerMP tPlayer = (EntityPlayerMP) tObject;
                Chunk tChunk = aWorld.getChunkFromBlockCoords(aX, aZ);
                if (tPlayer.getServerForPlayer().getPlayerManager()
                        .isPlayerWatchingChunk(tPlayer, tChunk.xPosition, tChunk.zPosition)) {
                    sendToPlayer(aPacket, tPlayer);
                }
            }
        }
    }

    @ChannelHandler.Sharable
    static final class HandlerShared extends SimpleChannelInboundHandler<GG_Packet> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, GG_Packet aPacket) {
            final EntityPlayer aPlayer = GT_Values.GT.getThePlayer();
            aPacket.process(aPlayer == null ? null : aPlayer.worldObj);
        }
    }
}
