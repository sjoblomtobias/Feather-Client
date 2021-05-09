package com.murengezi.minecraft.block.state.pattern;

import com.google.common.base.Predicate;
import com.murengezi.minecraft.block.Block;
import com.murengezi.minecraft.block.state.IBlockState;

public class BlockHelper implements Predicate<IBlockState> {

    private final Block block;

    private BlockHelper(Block blockType) {
        this.block = blockType;
    }

    public static BlockHelper forBlock(Block blockType) {
        return new BlockHelper(blockType);
    }

    public boolean apply(IBlockState blockState) {
        return blockState != null && blockState.getBlock() == this.block;
    }

}