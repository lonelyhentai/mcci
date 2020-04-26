package com.evernightfireworks.mcci.block;

import com.evernightfireworks.mcci.CausalEngine;
import com.evernightfireworks.mcci.service.CraftingPolicyService;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.DefaultedList;

public class CausalMachineBlockEntity extends BlockEntity implements CausalMachineInventory {
    DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public CausalMachineBlockEntity() {
        super(CausalMachineBlock.ENTITY);
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
        if(stack.getCount() > getInvMaxStackAmount()) {
            stack.setCount(getInvMaxStackAmount());
        }
        if(this.world!=null&&!this.world.isClient()) {
            CraftingPolicyService service = CausalEngine.CRAFTING_POLICY_SERVICE;
            var item = stack.getItem();
            var subgraph = service.getSubCraftingGraph(this.world, item);
            subgraph = subgraph;
        }
    }
}
