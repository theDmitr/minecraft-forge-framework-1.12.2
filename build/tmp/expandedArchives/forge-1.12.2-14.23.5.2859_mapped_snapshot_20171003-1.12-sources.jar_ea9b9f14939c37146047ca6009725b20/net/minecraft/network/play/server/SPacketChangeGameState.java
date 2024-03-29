package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketChangeGameState implements Packet<INetHandlerPlayClient>
{
    public static final String[] MESSAGE_NAMES = new String[] {"tile.bed.notValid"};
    private int state;
    private float value;

    public SPacketChangeGameState()
    {
    }

    public SPacketChangeGameState(int stateIn, float valueIn)
    {
        this.state = stateIn;
        this.value = valueIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.state = buf.readUnsignedByte();
        this.value = buf.readFloat();
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeByte(this.state);
        buf.writeFloat(this.value);
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleChangeGameState(this);
    }

    @SideOnly(Side.CLIENT)
    public int getGameState()
    {
        return this.state;
    }

    @SideOnly(Side.CLIENT)
    public float getValue()
    {
        return this.value;
    }
}
