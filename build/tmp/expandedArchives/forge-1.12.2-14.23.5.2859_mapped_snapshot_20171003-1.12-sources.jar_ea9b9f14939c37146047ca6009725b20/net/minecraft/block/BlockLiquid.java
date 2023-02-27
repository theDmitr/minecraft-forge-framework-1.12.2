package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockLiquid extends Block
{
    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 15);

    protected BlockLiquid(Material materialIn)
    {
        super(materialIn);
        this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, Integer.valueOf(0)));
        this.setTickRandomly(true);
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return FULL_BLOCK_AABB;
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        return NULL_AABB;
    }

    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        return this.blockMaterial != Material.LAVA;
    }

    public static float getLiquidHeightPercent(int meta)
    {
        if (meta >= 8)
        {
            meta = 0;
        }

        return (float)(meta + 1) / 9.0F;
    }

    protected int getDepth(IBlockState state)
    {
        return state.getMaterial() == this.blockMaterial ? ((Integer)state.getValue(LEVEL)).intValue() : -1;
    }

    protected int getRenderedDepth(IBlockState state)
    {
        int i = this.getDepth(state);
        return i >= 8 ? 0 : i;
    }

    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid)
    {
        return hitIfLiquid && ((Integer)state.getValue(LEVEL)).intValue() == 0;
    }

    private boolean causesDownwardCurrent(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();
        Material material = iblockstate.getMaterial();

        if (material == this.blockMaterial)
        {
            return false;
        }
        else if (side == EnumFacing.UP)
        {
            return true;
        }
        else if (material == Material.ICE)
        {
            return false;
        }
        else
        {
            boolean flag = isExceptBlockForAttachWithPiston(block) || block instanceof BlockStairs;
            return !flag && iblockstate.getBlockFaceShape(worldIn, pos, side) == BlockFaceShape.SOLID;
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        if (blockAccess.getBlockState(pos.offset(side)).getMaterial() == this.blockMaterial)
        {
            return false;
        }
        else
        {
            return side == EnumFacing.UP ? true : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        }
    }

    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.LIQUID;
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Items.AIR;
    }

    public int quantityDropped(Random random)
    {
        return 0;
    }

    protected Vec3d getFlow(IBlockAccess worldIn, BlockPos pos, IBlockState state)
    {
        double d0 = 0.0D;
        double d1 = 0.0D;
        double d2 = 0.0D;
        int i = this.getRenderedDepth(state);
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
        {
            blockpos$pooledmutableblockpos.setPos(pos).move(enumfacing);
            int j = this.getRenderedDepth(worldIn.getBlockState(blockpos$pooledmutableblockpos));

            if (j < 0)
            {
                if (!worldIn.getBlockState(blockpos$pooledmutableblockpos).getMaterial().blocksMovement())
                {
                    j = this.getRenderedDepth(worldIn.getBlockState(blockpos$pooledmutableblockpos.down()));

                    if (j >= 0)
                    {
                        int k = j - (i - 8);
                        d0 += (double)(enumfacing.getFrontOffsetX() * k);
                        d1 += (double)(enumfacing.getFrontOffsetY() * k);
                        d2 += (double)(enumfacing.getFrontOffsetZ() * k);
                    }
                }
            }
            else if (j >= 0)
            {
                int l = j - i;
                d0 += (double)(enumfacing.getFrontOffsetX() * l);
                d1 += (double)(enumfacing.getFrontOffsetY() * l);
                d2 += (double)(enumfacing.getFrontOffsetZ() * l);
            }
        }

        Vec3d vec3d = new Vec3d(d0, d1, d2);

        if (((Integer)state.getValue(LEVEL)).intValue() >= 8)
        {
            for (EnumFacing enumfacing1 : EnumFacing.Plane.HORIZONTAL)
            {
                blockpos$pooledmutableblockpos.setPos(pos).move(enumfacing1);

                if (this.causesDownwardCurrent(worldIn, blockpos$pooledmutableblockpos, enumfacing1) || this.causesDownwardCurrent(worldIn, blockpos$pooledmutableblockpos.up(), enumfacing1))
                {
                    vec3d = vec3d.normalize().addVector(0.0D, -6.0D, 0.0D);
                    break;
                }
            }
        }

        blockpos$pooledmutableblockpos.release();
        return vec3d.normalize();
    }

    public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion)
    {
        return motion.add(this.getFlow(worldIn, pos, worldIn.getBlockState(pos)));
    }

    public int tickRate(World worldIn)
    {
        if (this.blockMaterial == Material.WATER)
        {
            return 5;
        }
        else if (this.blockMaterial == Material.LAVA)
        {
            return worldIn.provider.isNether() ? 10 : 30;
        }
        else
        {
            return 0;
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldRenderSides(IBlockAccess blockAccess, BlockPos pos)
    {
        for (int i = -1; i <= 1; ++i)
        {
            for (int j = -1; j <= 1; ++j)
            {
                IBlockState iblockstate = blockAccess.getBlockState(pos.add(i, 0, j));

                if (iblockstate.getMaterial() != this.blockMaterial && !iblockstate.isFullBlock())
                {
                    return true;
                }
            }
        }

        return false;
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        this.checkForMixing(worldIn, pos, state);
    }

    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        this.checkForMixing(worldIn, pos, state);
    }

    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        int i = source.getCombinedLight(pos, 0);
        int j = source.getCombinedLight(pos.up(), 0);
        int k = i & 255;
        int l = j & 255;
        int i1 = i >> 16 & 255;
        int j1 = j >> 16 & 255;
        return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
    }

    public boolean checkForMixing(World worldIn, BlockPos pos, IBlockState state)
    {
        if (this.blockMaterial == Material.LAVA)
        {
            boolean flag = false;

            for (EnumFacing enumfacing : EnumFacing.values())
            {
                if (enumfacing != EnumFacing.DOWN && worldIn.getBlockState(pos.offset(enumfacing)).getMaterial() == Material.WATER)
                {
                    flag = true;
                    break;
                }
            }

            if (flag)
            {
                Integer integer = (Integer)state.getValue(LEVEL);

                if (integer.intValue() == 0)
                {
                    worldIn.setBlockState(pos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, pos, pos, Blocks.OBSIDIAN.getDefaultState()));
                    this.triggerMixEffects(worldIn, pos);
                    return true;
                }

                if (integer.intValue() <= 4)
                {
                    worldIn.setBlockState(pos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, pos, pos, Blocks.COBBLESTONE.getDefaultState()));
                    this.triggerMixEffects(worldIn, pos);
                    return true;
                }
            }
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return this.blockMaterial == Material.WATER ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        double d0 = (double)pos.getX();
        double d1 = (double)pos.getY();
        double d2 = (double)pos.getZ();

        if (this.blockMaterial == Material.WATER)
        {
            int i = ((Integer)stateIn.getValue(LEVEL)).intValue();

            if (i > 0 && i < 8)
            {
                if (rand.nextInt(64) == 0)
                {
                    worldIn.playSound(d0 + 0.5D, d1 + 0.5D, d2 + 0.5D, SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.BLOCKS, rand.nextFloat() * 0.25F + 0.75F, rand.nextFloat() + 0.5F, false);
                }
            }
            else if (rand.nextInt(10) == 0)
            {
                worldIn.spawnParticle(EnumParticleTypes.SUSPENDED, d0 + (double)rand.nextFloat(), d1 + (double)rand.nextFloat(), d2 + (double)rand.nextFloat(), 0.0D, 0.0D, 0.0D);
            }
        }

        if (this.blockMaterial == Material.LAVA && worldIn.getBlockState(pos.up()).getMaterial() == Material.AIR && !worldIn.getBlockState(pos.up()).isOpaqueCube())
        {
            if (rand.nextInt(100) == 0)
            {
                double d8 = d0 + (double)rand.nextFloat();
                double d4 = d1 + stateIn.getBoundingBox(worldIn, pos).maxY;
                double d6 = d2 + (double)rand.nextFloat();
                worldIn.spawnParticle(EnumParticleTypes.LAVA, d8, d4, d6, 0.0D, 0.0D, 0.0D);
                worldIn.playSound(d8, d4, d6, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
            }

            if (rand.nextInt(200) == 0)
            {
                worldIn.playSound(d0, d1, d2, SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
            }
        }

        if (rand.nextInt(10) == 0 && worldIn.getBlockState(pos.down()).isTopSolid())
        {
            Material material = worldIn.getBlockState(pos.down(2)).getMaterial();

            if (!material.blocksMovement() && !material.isLiquid())
            {
                double d3 = d0 + (double)rand.nextFloat();
                double d5 = d1 - 1.05D;
                double d7 = d2 + (double)rand.nextFloat();

                if (this.blockMaterial == Material.WATER)
                {
                    worldIn.spawnParticle(EnumParticleTypes.DRIP_WATER, d3, d5, d7, 0.0D, 0.0D, 0.0D);
                }
                else
                {
                    worldIn.spawnParticle(EnumParticleTypes.DRIP_LAVA, d3, d5, d7, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static float getSlopeAngle(IBlockAccess worldIn, BlockPos pos, Material materialIn, IBlockState state)
    {
        Vec3d vec3d = getFlowingBlock(materialIn).getFlow(worldIn, pos, state);
        return vec3d.x == 0.0D && vec3d.z == 0.0D ? -1000.0F : (float)MathHelper.atan2(vec3d.z, vec3d.x) - ((float)Math.PI / 2F);
    }

    protected void triggerMixEffects(World worldIn, BlockPos pos)
    {
        double d0 = (double)pos.getX();
        double d1 = (double)pos.getY();
        double d2 = (double)pos.getZ();
        worldIn.playSound((EntityPlayer)null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);

        for (int i = 0; i < 8; ++i)
        {
            worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, d0 + Math.random(), d1 + 1.2D, d2 + Math.random(), 0.0D, 0.0D, 0.0D);
        }
    }

    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(LEVEL, Integer.valueOf(meta));
    }

    public int getMetaFromState(IBlockState state)
    {
        return ((Integer)state.getValue(LEVEL)).intValue();
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {LEVEL});
    }

    public static BlockDynamicLiquid getFlowingBlock(Material materialIn)
    {
        if (materialIn == Material.WATER)
        {
            return Blocks.FLOWING_WATER;
        }
        else if (materialIn == Material.LAVA)
        {
            return Blocks.FLOWING_LAVA;
        }
        else
        {
            throw new IllegalArgumentException("Invalid material");
        }
    }

    public static BlockStaticLiquid getStaticBlock(Material materialIn)
    {
        if (materialIn == Material.WATER)
        {
            return Blocks.WATER;
        }
        else if (materialIn == Material.LAVA)
        {
            return Blocks.LAVA;
        }
        else
        {
            throw new IllegalArgumentException("Invalid material");
        }
    }

    public static float getBlockLiquidHeight(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        int i = ((Integer)state.getValue(LEVEL)).intValue();
        return (i & 7) == 0 && worldIn.getBlockState(pos.up()).getMaterial() == Material.WATER ? 1.0F : 1.0F - getLiquidHeightPercent(i);
    }

    public static float getLiquidHeight(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return (float)pos.getY() + getBlockLiquidHeight(state, worldIn, pos);
    }

    @Override
    public float getBlockLiquidHeight(World world, BlockPos pos, IBlockState state, Material material)
    {
        return BlockLiquid.getBlockLiquidHeight(state, world, pos);
    }

    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face)
    {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @SideOnly (Side.CLIENT)
    public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks)
    {
        Vec3d viewport = net.minecraft.client.renderer.ActiveRenderInfo.projectViewFromEntity(entity, partialTicks);

        if (state.getMaterial().isLiquid())
        {
            float height = 0.0F;
            if (state.getBlock() instanceof BlockLiquid)
            {
                height = getLiquidHeightPercent(state.getValue(LEVEL)) - 0.11111111F;
            }
            float f1 = (float) (pos.getY() + 1) - height;
            if (viewport.y > (double)f1)
            {
                BlockPos upPos = pos.up();
                IBlockState upState = world.getBlockState(upPos);
                return upState.getBlock().getFogColor(world, upPos, upState, entity, originalColor, partialTicks);
            }
        }

        return super.getFogColor(world, pos, state, entity, originalColor, partialTicks);
    }
}
