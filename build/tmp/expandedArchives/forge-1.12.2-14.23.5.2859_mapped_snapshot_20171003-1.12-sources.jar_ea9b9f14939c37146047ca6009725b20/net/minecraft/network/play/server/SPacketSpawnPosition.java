package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketSpawnPosition implements Packet<INetHandlerPlayClient>
{
    private BlockPos spawnBlockPos;

    public SPacketSpawnPosition()
    {
    }

    public SPacketSpawnPosition(BlockPos posIn)
    {
        this.spawnBlockPos = posIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.spawnBlockPos = buf.readBlockPos();
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeBlockPos(this.spawnBlockPos);
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleSpawnPosition(this);
    }

    @SideOnly(Side.CLIENT)
    public BlockPos getSpawnPos()
    {
        return this.spawnBlockPos;
    }
}
