package net.minecraft.item;

import java.util.List;
import com.murengezi.minecraft.block.Block;
import com.murengezi.minecraft.block.BlockPlanks;
import com.murengezi.minecraft.block.IGrowable;
import com.murengezi.minecraft.block.material.Material;
import com.murengezi.minecraft.block.state.IBlockState;
import com.murengezi.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import com.murengezi.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class ItemDye extends Item
{
    public static final int[] dyeColors = new int[] {1973019, 11743532, 3887386, 5320730, 2437522, 8073150, 2651799, 11250603, 4408131, 14188952, 4312372, 14602026, 6719955, 12801229, 15435844, 15790320};

    public ItemDye()
    {
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setCreativeTab(CreativeTabs.tabMaterials);
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(ItemStack stack)
    {
        int i = stack.getMetadata();
        return super.getUnlocalizedName() + "." + EnumDyeColor.byDyeDamage(i).getUnlocalizedName();
    }

    /**
     * Called when a Block is right-clicked with this Item
     *  
     * @param pos The block being right-clicked
     * @param side The side being right-clicked
     */
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!player.canPlayerEdit(pos.offset(side), side, stack))
        {
            return false;
        }
        else
        {
            EnumDyeColor enumdyecolor = EnumDyeColor.byDyeDamage(stack.getMetadata());

            if (enumdyecolor == EnumDyeColor.WHITE)
            {
                if (applyBonemeal(stack, world, pos))
                {
                    if (!world.isRemote)
                    {
                        world.playAuxSFX(2005, pos, 0);
                    }

                    return true;
                }
            }
            else if (enumdyecolor == EnumDyeColor.BROWN)
            {
                IBlockState iblockstate = world.getBlockState(pos);
                Block block = iblockstate.getBlock();

                if (block == Blocks.log && iblockstate.getValue(BlockPlanks.VARIANT) == BlockPlanks.EnumType.JUNGLE)
                {
                    if (side == EnumFacing.DOWN)
                    {
                        return false;
                    }

                    if (side == EnumFacing.UP)
                    {
                        return false;
                    }

                    pos = pos.offset(side);

                    if (world.isAirBlock(pos))
                    {
                        IBlockState iblockstate1 = Blocks.cocoa.onBlockPlaced(world, pos, side, hitX, hitY, hitZ, 0, player);
                        world.setBlockState(pos, iblockstate1, 2);

                        if (!player.capabilities.isCreativeMode)
                        {
                            --stack.stackSize;
                        }
                    }

                    return true;
                }
            }

            return false;
        }
    }

    public static boolean applyBonemeal(ItemStack stack, World world, BlockPos target)
    {
        IBlockState iblockstate = world.getBlockState(target);

        if (iblockstate.getBlock() instanceof IGrowable)
        {
            IGrowable igrowable = (IGrowable)iblockstate.getBlock();

            if (igrowable.canGrow(world, target, iblockstate, world.isRemote))
            {
                if (!world.isRemote)
                {
                    if (igrowable.canUseBonemeal(world, world.rand, target, iblockstate))
                    {
                        igrowable.grow(world, world.rand, target, iblockstate);
                    }

                    --stack.stackSize;
                }

                return true;
            }
        }

        return false;
    }

    public static void spawnBonemealParticles(World world, BlockPos pos, int amount)
    {
        if (amount == 0)
        {
            amount = 15;
        }

        Block block = world.getBlockState(pos).getBlock();

        if (block.getMaterial() != Material.air)
        {
            block.setBlockBoundsBasedOnState(world, pos);

            for (int i = 0; i < amount; ++i)
            {
                double d0 = itemRand.nextGaussian() * 0.02D;
                double d1 = itemRand.nextGaussian() * 0.02D;
                double d2 = itemRand.nextGaussian() * 0.02D;
                world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (float)pos.getX() + itemRand.nextFloat(), (double)pos.getY() + (double)itemRand.nextFloat() * block.getBlockBoundsMaxY(), (float)pos.getZ() + itemRand.nextFloat(), d0, d1, d2);
            }
        }
    }

    /**
     * Returns true if the item can be used on the given entity, e.g. shears on sheep.
     */
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target)
    {
        if (target instanceof EntitySheep)
        {
            EntitySheep entitysheep = (EntitySheep)target;
            EnumDyeColor enumdyecolor = EnumDyeColor.byDyeDamage(stack.getMetadata());

            if (!entitysheep.getSheared() && entitysheep.getFleeceColor() != enumdyecolor)
            {
                entitysheep.setFleeceColor(enumdyecolor);
                --stack.stackSize;
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     *  
     * @param subItems The List of sub-items. This is a List of ItemStacks.
     */
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        for (int i = 0; i < 16; ++i)
        {
            subItems.add(new ItemStack(itemIn, 1, i));
        }
    }
}
