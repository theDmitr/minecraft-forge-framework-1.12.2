package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketDisconnect implements Packet<INetHandlerPlayClient>
{
    private ITextComponent reason;

    public SPacketDisconnect()
    {
    }

    public SPacketDisconnect(ITextComponent messageIn)
    {
        this.reason = messageIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.reason = buf.readTextComponent();
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeTextComponent(this.reason);
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleDisconnect(this);
    }

    @SideOnly(Side.CLIENT)
    public ITextComponent getReason()
    {
        return this.reason;
    }
}
