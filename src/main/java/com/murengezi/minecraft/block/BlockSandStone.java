package com.murengezi.minecraft.block;

import java.util.List;
import com.murengezi.minecraft.block.material.MapColor;
import com.murengezi.minecraft.block.material.Material;
import com.murengezi.minecraft.block.properties.PropertyEnum;
import com.murengezi.minecraft.block.state.BlockState;
import com.murengezi.minecraft.block.state.IBlockState;
import com.murengezi.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

public class BlockSandStone extends Block {

    public static final PropertyEnum<BlockSandStone.EnumType> TYPE = PropertyEnum.create("type", BlockSandStone.EnumType.class);

    public BlockSandStone() {
        super(Material.rock);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, BlockSandStone.EnumType.DEFAULT));
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    public int damageDropped(IBlockState state) {
        return state.getValue(TYPE).getMetadata();
    }

    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        for (BlockSandStone.EnumType enumType : BlockSandStone.EnumType.values()) {
            list.add(new ItemStack(item, 1, enumType.getMetadata()));
        }
    }

    public MapColor getMapColor(IBlockState state) {
        return MapColor.sandColor;
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE, BlockSandStone.EnumType.byMetadata(meta));
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(TYPE).getMetadata();
    }

    protected BlockState createBlockState() {
        return new BlockState(this, TYPE);
    }

    public enum EnumType implements IStringSerializable {
        DEFAULT(0, "sandstone", "default"), CHISELED(1, "chiseled_sandstone", "chiseled"), SMOOTH(2, "smooth_sandstone", "smooth");

        private static final BlockSandStone.EnumType[] META_LOOKUP = new BlockSandStone.EnumType[values().length];
        private final int metadata;
        private final String name, unlocalizedName;

        EnumType(int meta, String name, String unlocalizedName) {
            this.metadata = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public int getMetadata() {
            return this.metadata;
        }

        public String toString() {
            return this.name;
        }

        public static BlockSandStone.EnumType byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public String getName() {
            return this.name;
        }

        public String getUnlocalizedName() {
            return this.unlocalizedName;
        }

        static {
            for (BlockSandStone.EnumType enumType : values()) {
                META_LOOKUP[enumType.getMetadata()] = enumType;
            }
        }
    }

}