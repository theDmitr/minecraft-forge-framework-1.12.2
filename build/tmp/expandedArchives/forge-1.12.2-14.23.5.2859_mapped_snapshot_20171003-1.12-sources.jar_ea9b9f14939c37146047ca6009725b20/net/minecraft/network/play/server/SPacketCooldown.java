package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketCooldown implements Packet<INetHandlerPlayClient>
{
    private Item item;
    private int ticks;

    public SPacketCooldown()
    {
    }

    public SPacketCooldown(Item itemIn, int ticksIn)
    {
        this.item = itemIn;
        this.ticks = ticksIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.item = Item.getItemById(buf.readVarInt());
        this.ticks = buf.readVarInt();
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarInt(Item.getIdFromItem(this.item));
        buf.writeVarInt(this.ticks);
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleCooldown(this);
    }

    @SideOnly(Side.CLIENT)
    public Item getItem()
    {
        return this.item;
    }

    @SideOnly(Side.CLIENT)
    public int getTicks()
    {
        return this.ticks;
    }
}
