package com.evernightfireworks.mcci.blocks;

import com.evernightfireworks.mcci.CausalEngine;
import com.evernightfireworks.mcci.services.core.CGraph;
import com.evernightfireworks.mcci.services.interfaces.CGraphContainer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.DefaultedList;
import net.minecraft.world.World;

public class CraftingPolicyMachineEntity extends BlockEntity implements CraftingPolicyMachineInventory, CGraphContainer {
    DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    public CGraph currentGraph = new CGraph();

    public CraftingPolicyMachineEntity() {
        super(CraftingPolicyMachineBlock.ENTITY);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public boolean canPlayerUseInv(PlayerEntity player) {
        return pos.isWithinDistance(player.getBlockPos(), 4.5);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        Inventories.fromTag(tag, items);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        Inventories.toTag(tag, items);
        return super.toTag(tag);
    }

    @Override
    public void setInvStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getInvMaxStackAmount()) {
            stack.setCount(getInvMaxStackAmount());
        }
        World world = this.getWorld();
        if(world!=null&&world.isClient()) {
            CausalEngine.CRAFTING_POLICY_SERVICE.refreshWebViewGraph(this, stack.getItem(), world);
        }
    }

    @Override
    public CGraph getCGraph() {
        return this.currentGraph;
    }

    @Override
    public void setCGraph(CGraph cGraph) {
        this.currentGraph = cGraph;
    }
}
