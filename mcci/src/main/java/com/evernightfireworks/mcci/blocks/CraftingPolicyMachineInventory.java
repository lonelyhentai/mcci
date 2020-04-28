package com.evernightfireworks.mcci.blocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.Direction;

@FunctionalInterface
public interface CraftingPolicyMachineInventory extends SidedInventory {

    DefaultedList<ItemStack> getItems();
    /**
     * Creates an inventory from the item list.
     */
    static CraftingPolicyMachineInventory of(DefaultedList<ItemStack> items) {
        return () -> items;
    }
    /**
     * Creates a new inventory with the size.
     */
    static CraftingPolicyMachineInventory ofSize(int size) {
        return of(DefaultedList.ofSize(size, ItemStack.EMPTY));
    }

    @Override
    default int[] getInvAvailableSlots(Direction side) {
        int[] result = new int[getItems().size()];
        for(int i=0;i<result.length;i++) {
            result[i] = i;
        }
        return result;
    }

    @Override
    default boolean canInsertInvStack(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    default boolean canExtractInvStack(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    default int getInvSize() {
        return getItems().size();
    }

    @Override
    default boolean isInvEmpty() {
        for(int i=0;i<getInvSize();i++) {
            ItemStack stack = getInvStack(i);
            if(!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default ItemStack getInvStack(int slot) {
        return getItems().get(slot);
    }

    @Override
    default ItemStack takeInvStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(getItems(), slot,  amount);
        if(!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    default ItemStack removeInvStack(int slot) {
        return Inventories.removeStack(getItems(), slot);
    }

    @Override
    default void setInvStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if(stack.getCount() > getInvMaxStackAmount()) {
            stack.setCount(getInvMaxStackAmount());
        }
    }

    @Override
    default void markDirty() { }

    @Override
    default boolean canPlayerUseInv(PlayerEntity player) {
        return true;
    }

    @Override
    default void clear() {
        getItems().clear();
    }
}