package net.minecraft.network.login.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketDisconnect implements Packet<INetHandlerLoginClient>
{
    private ITextComponent reason;

    public SPacketDisconnect()
    {
    }

    public SPacketDisconnect(ITextComponent p_i46853_1_)
    {
        this.reason = p_i46853_1_;
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.reason = ITextComponent.Serializer.fromJsonLenient(buf.readString(32767));
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeTextComponent(this.reason);
    }

    public void processPacket(INetHandlerLoginClient handler)
    {
        handler.handleDisconnect(this);
    }

    @SideOnly(Side.CLIENT)
    public ITextComponent getReason()
    {
        return this.reason;
    }
}
