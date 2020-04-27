package com.evernightfireworks.mcci.block;

import com.evernightfireworks.mcci.CausalEngine;
import com.evernightfireworks.mcci.service.CraftingPolicyService;
import com.evernightfireworks.mcci.service.core.CGraph;
import com.evernightfireworks.mcci.service.FileSystemManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CausalMachineBlockEntity extends BlockEntity implements CausalMachineInventory {
    DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private CGraph currentGraph = null;
    private final Logger logger = LogManager.getFormatterLogger("mcci:block:causal_machine:entity");

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
            var item = stack.getItem();
            this.refreshWebViewGraph(item);
        }
    }

    @Environment(EnvType.CLIENT)
    private void refreshWebViewGraph(Item item) {
        if(currentGraph==null||currentGraph.target!=item) {
            CraftingPolicyService service = CausalEngine.CRAFTING_POLICY_SERVICE;
            this.currentGraph = service.getSubCraftingGraph(this.world, item);
            if(this.currentGraph.target==null) {
                return;
            }
            String serializedGraph = this.currentGraph.toString();
            Identifier id = new Identifier("mcci/web/crafting-graph.html");
            try {
                var templateStream = FileSystemManager.getSourceResourceAsStream("/assets/mcci/webview/crafting-graph.html");
                String content = new String(templateStream.readAllBytes(), StandardCharsets.UTF_8);
                String output = content.replaceAll( "\\{/\\*data\\*/}", serializedGraph);
                FileSystemManager.writeRuntimeResource("webview/crafting-graph.html", output);
            } catch (Exception e){
                this.logger.error(String.format("failed to load resource of %s", id), e);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static URL getWebViewGraphURL() throws MalformedURLException {
        return FileSystemManager.getRuntimeResourceAbsPath("webview/crafting-graph.html").toUri().toURL();
    }
}
