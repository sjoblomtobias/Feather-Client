package com.murengezi.minecraft.block;

import com.google.common.collect.Lists;
import java.util.List;
import com.murengezi.minecraft.block.material.Material;
import com.murengezi.minecraft.block.state.IBlockState;
import com.murengezi.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class BlockNote extends BlockContainer {

    private static final List<String> INSTRUMENTS = Lists.newArrayList("harp", "bd", "snare", "hat", "bassattack");

    public BlockNote() {
        super(Material.wood);
        this.setCreativeTab(CreativeTabs.tabRedstone);
    }

    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        boolean flag = world.isBlockPowered(pos);
        TileEntity tileentity = world.getTileEntity(pos);

        if (tileentity instanceof TileEntityNote) {
            TileEntityNote tileentitynote = (TileEntityNote)tileentity;

            if (tileentitynote.previousRedstoneState != flag) {
                if (flag) {
                    tileentitynote.triggerNote(world, pos);
                }

                tileentitynote.previousRedstoneState = flag;
            }
        }
    }

    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tileentity = world.getTileEntity(pos);

            if (tileentity instanceof TileEntityNote) {
                TileEntityNote tileentitynote = (TileEntityNote) tileentity;
                tileentitynote.changePitch();
                tileentitynote.triggerNote(world, pos);
                player.triggerAchievement(StatList.field_181735_S);
            }
        }
        return true;
    }

    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        if (!world.isRemote) {
            TileEntity tileentity = world.getTileEntity(pos);

            if (tileentity instanceof TileEntityNote) {
                ((TileEntityNote)tileentity).triggerNote(world, pos);
                player.triggerAchievement(StatList.field_181734_R);
            }
        }
    }

    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityNote();
    }

    private String getInstrument(int id) {
        if (id < 0 || id >= INSTRUMENTS.size()) {
            id = 0;
        }

        return INSTRUMENTS.get(id);
    }

    public boolean onBlockEventReceived(World world, BlockPos pos, IBlockState state, int eventID, int eventParam) {
        float f = (float)Math.pow(2.0D, (double)(eventParam - 12) / 12.0D);
        world.playSoundEffect((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, "note." + this.getInstrument(eventID), 3.0F, f);
        world.spawnParticle(EnumParticleTypes.NOTE, (double)pos.getX() + 0.5D, (double)pos.getY() + 1.2D, (double)pos.getZ() + 0.5D, (double)eventParam / 24.0D, 0.0D, 0.0D);
        return true;
    }

    public int getRenderType() {
        return 3;
    }

}