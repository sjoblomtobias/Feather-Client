package com.murengezi.minecraft.client.multiplayer;

import com.murengezi.minecraft.block.Block;
import com.murengezi.minecraft.block.material.Material;
import com.murengezi.minecraft.block.state.IBlockState;
import com.murengezi.minecraft.client.Minecraft;
import com.murengezi.minecraft.client.audio.PositionedSoundRecord;
import com.murengezi.minecraft.client.entity.EntityPlayerSP;
import com.murengezi.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C11PacketEnchantItem;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

public class PlayerControllerMP
{
    /** The Minecraft instance. */
    private final Minecraft mc;
    private final NetHandlerPlayClient netClientHandler;
    private BlockPos currentBlock = new BlockPos(-1, -1, -1);

    /** The Item currently being used to destroy a block */
    private ItemStack currentItemHittingBlock;

    /** Current block damage (MP) */
    private float curBlockDamageMP;

    /**
     * Tick counter, when it hits 4 it resets back to 0 and plays the step sound
     */
    private float stepSoundTickCounter;

    /**
     * Delays the first damage on the block after the first click on the block
     */
    private int blockHitDelay;

    /** Tells if the player is hitting a block */
    private boolean isHittingBlock;

    /** Current game type for the player */
    private WorldSettings.GameType currentGameType = WorldSettings.GameType.SURVIVAL;

    /** Index of the current item held by the player in the inventory hotbar */
    private int currentPlayerItem;

    public PlayerControllerMP(Minecraft mcIn, NetHandlerPlayClient p_i45062_2_)
    {
        this.mc = mcIn;
        this.netClientHandler = p_i45062_2_;
    }

    public static void clickBlockCreative(Minecraft mcIn, PlayerControllerMP p_178891_1_, BlockPos p_178891_2_, EnumFacing p_178891_3_)
    {
        if (!mcIn.world.extinguishFire(mcIn.player, p_178891_2_, p_178891_3_))
        {
            p_178891_1_.onPlayerDestroyBlock(p_178891_2_, p_178891_3_);
        }
    }

    /**
     * Sets player capabilities depending on current gametype. params: player
     */
    public void setPlayerCapabilities(EntityPlayer p_78748_1_)
    {
        this.currentGameType.configurePlayerCapabilities(p_78748_1_.capabilities);
    }

    /**
     * None
     */
    public boolean isSpectator()
    {
        return this.currentGameType == WorldSettings.GameType.SPECTATOR;
    }

    /**
     * Sets the game type for the player.
     */
    public void setGameType(WorldSettings.GameType p_78746_1_)
    {
        this.currentGameType = p_78746_1_;
        this.currentGameType.configurePlayerCapabilities(this.mc.player.capabilities);
    }

    /**
     * Flips the player around.
     */
    public void flipPlayer(EntityPlayer player)
    {
        player.rotationYaw = -180.0F;
    }

    public boolean shouldDrawHUD()
    {
        return this.currentGameType.isSurvivalOrAdventure();
    }

    /**
     * Called when a player completes the destruction of a block
     *  
     * @param pos The block's coordinates
     * @param side The side it was destroyed from
     */
    public boolean onPlayerDestroyBlock(BlockPos pos, EnumFacing side)
    {
        if (this.currentGameType.isAdventure())
        {
            if (this.currentGameType == WorldSettings.GameType.SPECTATOR)
            {
                return false;
            }

            if (!this.mc.player.isAllowEdit())
            {
                Block block = this.mc.world.getBlockState(pos).getBlock();
                ItemStack itemstack = this.mc.player.getCurrentEquippedItem();

                if (itemstack == null)
                {
                    return false;
                }

                if (!itemstack.canDestroy(block))
                {
                    return false;
                }
            }
        }

        if (this.currentGameType.isCreative() && this.mc.player.getHeldItem() != null && this.mc.player.getHeldItem().getItem() instanceof ItemSword)
        {
            return false;
        }
        else
        {
            World world = this.mc.world;
            IBlockState iblockstate = world.getBlockState(pos);
            Block block1 = iblockstate.getBlock();

            if (block1.getMaterial() == Material.air)
            {
                return false;
            }
            else
            {
                world.playAuxSFX(2001, pos, Block.getStateId(iblockstate));
                boolean flag = world.setBlockToAir(pos);

                if (flag)
                {
                    block1.onBlockDestroyedByPlayer(world, pos, iblockstate);
                }

                this.currentBlock = new BlockPos(this.currentBlock.getX(), -1, this.currentBlock.getZ());

                if (!this.currentGameType.isCreative())
                {
                    ItemStack itemstack1 = this.mc.player.getCurrentEquippedItem();

                    if (itemstack1 != null)
                    {
                        itemstack1.onBlockDestroyed(world, block1, pos, this.mc.player);

                        if (itemstack1.stackSize == 0)
                        {
                            this.mc.player.destroyCurrentEquippedItem();
                        }
                    }
                }

                return flag;
            }
        }
    }

    /**
     * Called when the player is hitting a block with an item.
     *  
     * @param loc location of the block being clicked
     * @param face Blockface being clicked
     */
    public boolean clickBlock(BlockPos loc, EnumFacing face)
    {
        if (this.currentGameType.isAdventure())
        {
            if (this.currentGameType == WorldSettings.GameType.SPECTATOR)
            {
                return false;
            }

            if (!this.mc.player.isAllowEdit())
            {
                Block block = this.mc.world.getBlockState(loc).getBlock();
                ItemStack itemstack = this.mc.player.getCurrentEquippedItem();

                if (itemstack == null)
                {
                    return false;
                }

                if (!itemstack.canDestroy(block))
                {
                    return false;
                }
            }
        }

        if (!this.mc.world.getWorldBorder().contains(loc))
        {
            return false;
        }
        else
        {
            if (this.currentGameType.isCreative())
            {
                this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                clickBlockCreative(this.mc, this, loc, face);
                this.blockHitDelay = 5;
            }
            else if (!this.isHittingBlock || !this.isHittingPosition(loc))
            {
                if (this.isHittingBlock)
                {
                    this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, face));
                }

                this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                Block block1 = this.mc.world.getBlockState(loc).getBlock();
                boolean flag = block1.getMaterial() != Material.air;

                if (flag && this.curBlockDamageMP == 0.0F)
                {
                    block1.onBlockClicked(this.mc.world, loc, this.mc.player);
                }

                if (flag && block1.getPlayerRelativeBlockHardness(this.mc.player) >= 1.0F)
                {
                    this.onPlayerDestroyBlock(loc, face);
                }
                else
                {
                    this.isHittingBlock = true;
                    this.currentBlock = loc;
                    this.currentItemHittingBlock = this.mc.player.getHeldItem();
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    this.mc.world.sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, (int)(this.curBlockDamageMP * 10.0F) - 1);
                }
            }

            return true;
        }
    }

    /**
     * Resets current block damage and isHittingBlock
     */
    public void resetBlockRemoving()
    {
        if (this.isHittingBlock)
        {
            this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, EnumFacing.DOWN));
            this.isHittingBlock = false;
            this.curBlockDamageMP = 0.0F;
            this.mc.world.sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, -1);
        }
    }

    public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing)
    {
        this.syncCurrentPlayItem();

        if (this.blockHitDelay > 0)
        {
            --this.blockHitDelay;
            return true;
        }
        else if (this.currentGameType.isCreative() && this.mc.world.getWorldBorder().contains(posBlock))
        {
            this.blockHitDelay = 5;
            this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
            clickBlockCreative(this.mc, this, posBlock, directionFacing);
            return true;
        }
        else if (this.isHittingPosition(posBlock))
        {
            Block block = this.mc.world.getBlockState(posBlock).getBlock();

            if (block.getMaterial() == Material.air)
            {
                this.isHittingBlock = false;
                return false;
            }
            else
            {
                this.curBlockDamageMP += block.getPlayerRelativeBlockHardness(this.mc.player);

                if (this.stepSoundTickCounter % 4.0F == 0.0F)
                {
                    this.mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getStepSound()), (block.stepSound.getVolume() + 1.0F) / 8.0F, block.stepSound.getFrequency() * 0.5F, (float)posBlock.getX() + 0.5F, (float)posBlock.getY() + 0.5F, (float)posBlock.getZ() + 0.5F));
                }

                ++this.stepSoundTickCounter;

                if (this.curBlockDamageMP >= 1.0F)
                {
                    this.isHittingBlock = false;
                    this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
                    this.onPlayerDestroyBlock(posBlock, directionFacing);
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    this.blockHitDelay = 5;
                }

                this.mc.world.sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, (int)(this.curBlockDamageMP * 10.0F) - 1);
                return true;
            }
        }
        else
        {
            return this.clickBlock(posBlock, directionFacing);
        }
    }

    /**
     * player reach distance = 4F
     */
    public float getBlockReachDistance()
    {
        return this.currentGameType.isCreative() ? 5.0F : 4.5F;
    }

    public void updateController()
    {
        this.syncCurrentPlayItem();

        if (this.netClientHandler.getNetworkManager().isChannelOpen())
        {
            this.netClientHandler.getNetworkManager().processReceivedPackets();
        }
        else
        {
            this.netClientHandler.getNetworkManager().checkDisconnected();
        }
    }

    private boolean isHittingPosition(BlockPos pos)
    {
        ItemStack itemstack = this.mc.player.getHeldItem();
        boolean flag = this.currentItemHittingBlock == null && itemstack == null;

        if (this.currentItemHittingBlock != null && itemstack != null)
        {
            flag = itemstack.getItem() == this.currentItemHittingBlock.getItem() && ItemStack.areItemStackTagsEqual(itemstack, this.currentItemHittingBlock) && (itemstack.isItemStackDamageable() || itemstack.getMetadata() == this.currentItemHittingBlock.getMetadata());
        }

        return pos.equals(this.currentBlock) && flag;
    }

    /**
     * Syncs the current player item with the server
     */
    private void syncCurrentPlayItem()
    {
        int i = this.mc.player.inventory.currentItem;

        if (i != this.currentPlayerItem)
        {
            this.currentPlayerItem = i;
            this.netClientHandler.addToSendQueue(new C09PacketHeldItemChange(this.currentPlayerItem));
        }
    }

    public boolean onPlayerRightClick(EntityPlayerSP player, WorldClient world, ItemStack heldStack, BlockPos hitPos, EnumFacing side, Vec3 hitVec)
    {
        this.syncCurrentPlayItem();
        float f = (float)(hitVec.xCoord - (double)hitPos.getX());
        float f1 = (float)(hitVec.yCoord - (double)hitPos.getY());
        float f2 = (float)(hitVec.zCoord - (double)hitPos.getZ());
        boolean flag = false;

        if (!this.mc.world.getWorldBorder().contains(hitPos))
        {
            return false;
        }
        else
        {
            if (this.currentGameType != WorldSettings.GameType.SPECTATOR)
            {
                IBlockState iblockstate = world.getBlockState(hitPos);

                if ((!player.isSneaking() || player.getHeldItem() == null) && iblockstate.getBlock().onBlockActivated(world, hitPos, iblockstate, player, side, f, f1, f2))
                {
                    flag = true;
                }

                if (!flag && heldStack != null && heldStack.getItem() instanceof ItemBlock)
                {
                    ItemBlock itemblock = (ItemBlock)heldStack.getItem();

                    if (!itemblock.canPlaceBlockOnSide(world, hitPos, side, player, heldStack))
                    {
                        return false;
                    }
                }
            }

            this.netClientHandler.addToSendQueue(new C08PacketPlayerBlockPlacement(hitPos, side.getIndex(), player.inventory.getCurrentItem(), f, f1, f2));

            if (!flag && this.currentGameType != WorldSettings.GameType.SPECTATOR)
            {
                if (heldStack == null)
                {
                    return false;
                }
                else if (this.currentGameType.isCreative())
                {
                    int i = heldStack.getMetadata();
                    int j = heldStack.stackSize;
                    boolean flag1 = heldStack.onItemUse(player, world, hitPos, side, f, f1, f2);
                    heldStack.setItemDamage(i);
                    heldStack.stackSize = j;
                    return flag1;
                }
                else
                {
                    return heldStack.onItemUse(player, world, hitPos, side, f, f1, f2);
                }
            }
            else
            {
                return true;
            }
        }
    }

    /**
     * Notifies the server of things like consuming food, etc...
     */
    public boolean sendUseItem(EntityPlayer player, World world, ItemStack itemStackIn)
    {
        if (this.currentGameType == WorldSettings.GameType.SPECTATOR)
        {
            return false;
        }
        else
        {
            this.syncCurrentPlayItem();
            this.netClientHandler.addToSendQueue(new C08PacketPlayerBlockPlacement(player.inventory.getCurrentItem()));
            int i = itemStackIn.stackSize;
            ItemStack itemstack = itemStackIn.useItemRightClick(world, player);

            if (itemstack != itemStackIn || itemstack != null && itemstack.stackSize != i)
            {
                player.inventory.mainInventory[player.inventory.currentItem] = itemstack;

                if (itemstack.stackSize == 0)
                {
                    player.inventory.mainInventory[player.inventory.currentItem] = null;
                }

                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public EntityPlayerSP func_178892_a(World world, StatFileWriter p_178892_2_)
    {
        return new EntityPlayerSP(this.mc, world, this.netClientHandler, p_178892_2_);
    }

    /**
     * Attacks an entity
     */
    public void attackEntity(EntityPlayer player, Entity targetEntity)
    {
        this.syncCurrentPlayItem();
        this.netClientHandler.addToSendQueue(new C02PacketUseEntity(targetEntity, C02PacketUseEntity.Action.ATTACK));

        if (this.currentGameType != WorldSettings.GameType.SPECTATOR)
        {
            player.attackTargetEntityWithCurrentItem(targetEntity);
        }
    }

    /**
     * Send packet to server - player is interacting with another entity (left click)
     */
    public boolean interactWithEntitySendPacket(EntityPlayer player, Entity targetEntity)
    {
        this.syncCurrentPlayItem();
        this.netClientHandler.addToSendQueue(new C02PacketUseEntity(targetEntity, C02PacketUseEntity.Action.INTERACT));
        return this.currentGameType != WorldSettings.GameType.SPECTATOR && player.interactWith(targetEntity);
    }

    public boolean func_178894_a(EntityPlayer p_178894_1_, Entity p_178894_2_, MovingObjectPosition p_178894_3_)
    {
        this.syncCurrentPlayItem();
        Vec3 vec3 = new Vec3(p_178894_3_.hitVec.xCoord - p_178894_2_.posX, p_178894_3_.hitVec.yCoord - p_178894_2_.posY, p_178894_3_.hitVec.zCoord - p_178894_2_.posZ);
        this.netClientHandler.addToSendQueue(new C02PacketUseEntity(p_178894_2_, vec3));
        return this.currentGameType != WorldSettings.GameType.SPECTATOR && p_178894_2_.interactAt(p_178894_1_, vec3);
    }

    /**
     * Handles slot clicks sends a packet to the server.
     */
    public ItemStack windowClick(int windowId, int slotId, int mouseButtonClicked, int mode, EntityPlayer player)
    {
        short short1 = player.openContainer.getNextTransactionID(player.inventory);
        ItemStack itemstack = player.openContainer.slotClick(slotId, mouseButtonClicked, mode, player);
        this.netClientHandler.addToSendQueue(new C0EPacketClickWindow(windowId, slotId, mouseButtonClicked, mode, itemstack, short1));
        return itemstack;
    }

    /**
     * GuiEnchantment uses this during multiplayer to tell PlayerControllerMP to send a packet indicating the
     * enchantment action the player has taken.
     */
    public void sendEnchantPacket(int p_78756_1_, int p_78756_2_)
    {
        this.netClientHandler.addToSendQueue(new C11PacketEnchantItem(p_78756_1_, p_78756_2_));
    }

    /**
     * Used in PlayerControllerMP to update the server with an ItemStack in a slot.
     */
    public void sendSlotPacket(ItemStack itemStackIn, int slotId)
    {
        if (this.currentGameType.isCreative())
        {
            this.netClientHandler.addToSendQueue(new C10PacketCreativeInventoryAction(slotId, itemStackIn));
        }
    }

    /**
     * Sends a Packet107 to the server to drop the item on the ground
     */
    public void sendPacketDropItem(ItemStack itemStackIn)
    {
        if (this.currentGameType.isCreative() && itemStackIn != null)
        {
            this.netClientHandler.addToSendQueue(new C10PacketCreativeInventoryAction(-1, itemStackIn));
        }
    }

    public void onStoppedUsingItem(EntityPlayer player)
    {
        this.syncCurrentPlayItem();
        this.netClientHandler.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        player.stopUsingItem();
    }

    public boolean gameIsSurvivalOrAdventure()
    {
        return this.currentGameType.isSurvivalOrAdventure();
    }

    /**
     * Checks if the player is not creative, used for checking if it should break a block instantly
     */
    public boolean isNotCreative()
    {
        return !this.currentGameType.isCreative();
    }

    /**
     * returns true if player is in creative mode
     */
    public boolean isInCreativeMode()
    {
        return this.currentGameType.isCreative();
    }

    /**
     * true for hitting entities far away.
     */
    public boolean extendedReach()
    {
        return this.currentGameType.isCreative();
    }

    /**
     * Checks if the player is riding a horse, used to chose the GUI to open
     */
    public boolean isRidingHorse()
    {
        return this.mc.player.isRiding() && this.mc.player.ridingEntity instanceof EntityHorse;
    }

    public boolean isSpectatorMode()
    {
        return this.currentGameType == WorldSettings.GameType.SPECTATOR;
    }

    public WorldSettings.GameType getCurrentGameType()
    {
        return this.currentGameType;
    }

    public boolean func_181040_m()
    {
        return this.isHittingBlock;
    }
}
