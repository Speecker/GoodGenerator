package goodgenerator.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

import com.google.common.io.ByteArrayDataInput;

import goodgenerator.blocks.tileEntity.YottaFluidTank;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import io.netty.buffer.ByteBuf;

public class YottaFluidTankPacket extends GG_Packet {

    private String fluidAmount;
    private String fluidName;
    private String fluidMax;
    private int x;
    private int z;
    private int y;

    public YottaFluidTankPacket() {
        super(true);
    }

    public YottaFluidTankPacket(int x, int z, int y, String fluidMax, String fluidAmount, String fluidName) {
        super(false);
        this.x = x;
        this.z = z;
        this.y = y;
        this.fluidMax = fluidMax;
        this.fluidAmount = fluidAmount;
        this.fluidName = fluidName;
    }

    @Override
    public GG_Packet decode(ByteArrayDataInput data) {
        x = data.readInt();
        z = data.readInt();
        y = data.readInt();
        StringBuilder mixed = new StringBuilder();
        int length = data.readInt();
        for (int i = 0; i < length; i++) {
            mixed.append(data.readChar());
        }
        String[] mixedArray = mixed.toString().split(" ");
        fluidMax = mixedArray[0];
        fluidAmount = mixedArray[1];
        fluidName = mixedArray[2];
        return new YottaFluidTankPacket(x, z, y, fluidMax, fluidAmount, fluidName);
    }

    @Override
    public void encode(ByteBuf data) {
        data.writeInt(x);
        data.writeInt(z);
        data.writeInt(y);
        String mixed = fluidMax + " " + fluidAmount + " " + fluidName;
        data.writeInt(mixed.length());
        for (char c : mixed.toCharArray()) {
            data.writeChar(c);
        }
    }

    @Override
    public byte getPacketID() {
        return 0;
    }

    @Override
    public void process(IBlockAccess world) {
        if (world == null) {
            return;
        }
        TileEntity entity = world.getTileEntity(x, y, z);
        System.out.println(entity);
        if (!(entity instanceof IGregTechTileEntity)) {
            return;
        }
        IGregTechTileEntity gregEntity = (IGregTechTileEntity) entity;
        IMetaTileEntity metaEntity = gregEntity.getMetaTileEntity();
        if (!(metaEntity instanceof YottaFluidTank)) {
            return;
        }

        YottaFluidTank tank = (YottaFluidTank) metaEntity;
        tank.setCurrentFluid(fluidMax, fluidAmount, fluidName);
    }
}
