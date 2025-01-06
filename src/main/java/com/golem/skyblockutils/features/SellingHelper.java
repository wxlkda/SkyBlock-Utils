package com.golem.skyblockutils.features;

import com.golem.skyblockutils.events.InventoryChangeEvent;
import com.golem.skyblockutils.models.AttributeValueResult;
import com.golem.skyblockutils.models.gui.ButtonManager;
import com.golem.skyblockutils.utils.*;
import lombok.Getter;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.golem.skyblockutils.Main.configFile;
import static com.golem.skyblockutils.Main.mc;

public class SellingHelper {

    private final HashMap<Slot, SellMethod> highlightSlots = new HashMap<>();
    private final Set<Slot> highlightInvSlots = new HashSet<>();
    private final Set<Signature> possibleSimilarities = new HashSet<>();
    private static final HashMap<String, TileEntityChest> chestFilters = new HashMap<>();

    @SubscribeEvent
    public void onInventoryChange(InventoryChangeEvent event) {
        possibleSimilarities.clear();
        highlightSlots.clear();
        highlightInvSlots.clear();
        if (event.event.gui instanceof GuiContainer) {
            if (InventoryData.currentChestName.contains("Paid Chest") || InventoryData.currentChestName.contains("Free Chest")) return;
            if (configFile.sellingHelper) highlightSellMethod();
            if (!Objects.equals(LocationUtils.getLocation(), "dynamic")) return;
            if (configFile.sortingHelper) checkForSimilarItems();
        }
    }

    private void checkForSimilarItems() {
        List<Slot> slots = InventoryData.containerSlots.subList(0, InventoryData.containerSlots.size() - 36);

        possibleSimilarities.addAll(getSimilarity(slots));

        if (possibleSimilarities.isEmpty()) return;

        if (InventoryData.containerSlots.size() < 36) return;
        slots = InventoryData.containerSlots.subList(InventoryData.containerSlots.size() - 36, InventoryData.containerSlots.size()).stream().filter(Slot::getHasStack).collect(Collectors.toList());
        highlightInvSlots.addAll(slots.stream().filter(slot -> {
            if (!slot.getHasStack()) return false;
            Set<Signature> signatures = getSignature(slot);
            if (signatures.isEmpty()) return false;
            return signatures.stream().anyMatch(possibleSimilarities::contains);
        }).collect(Collectors.toList()));
    }

    public static void addChest(TileEntityChest chest, List<Slot> slots) {
        Set<Signature> similarity = getSimilarity(slots);
        String sim = similarity.stream().max(Comparator.comparingInt(Signature::getLevel)).map(Signature::getSignature).orElse(null);
        if (sim == null) return;
        chestFilters.put(sim, chest);
    }

    private static Set<Signature> getSimilarity(List<Slot> slots) {
        Set<Signature> possible = new HashSet<>();
        if (slots.stream().allMatch(Slot::getHasStack)) return possible;
        slots = slots.stream().filter(Slot::getHasStack).collect(Collectors.toList());

        if (slots.isEmpty()) return possible;

        List<Set<Signature>> slotSignatures = slots.stream().map(SellingHelper::getSignature).filter(o -> !o.isEmpty()).collect(Collectors.toList());

        if (slotSignatures.isEmpty()) return possible;

        slotSignatures.forEach(System.out::println);

        possible = new HashSet<>(slotSignatures.get(0));
        slotSignatures.forEach(possible::retainAll);
        return possible;
    }

    private void highlightSellMethod() {
        for (Slot slot : InventoryData.containerSlots.subList(0, InventoryData.containerSlots.size() - 36)) {
            AttributeValueResult result = InventoryData.values.get(slot);
            if (result == null) continue;
            if (Objects.equals(result.top_display, "SAL")) {
                highlightSlots.put(slot, SellMethod.SALVAGE);
            } else if (result.value > 5_000_000) {
                highlightSlots.put(slot, SellMethod.AUCTION_EXPENSIVE);
            } else {
                highlightSlots.put(slot, SellMethod.AUCTION_CHEAP);
            }
        }
    }

    @SubscribeEvent
    public void onTick(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (!configFile.sortingHelper) return;
        if (!ButtonManager.isChecked("sortingHelper")) return;
        if (!Objects.equals(LocationUtils.getLocation(), "dynamic")) return;

        ArrayList<TileEntityChest> highlightChests = new ArrayList<>();

        for (Slot slot : InventoryData.containerSlots) {
            if (!slot.getHasStack()) continue;
            if (slot.inventory != mc.thePlayer.inventory) continue;

            Set<Signature> signatures = getSignature(slot);

            signatures.stream().filter(o -> chestFilters.containsKey(o.signature)).max(Comparator.comparingInt(Signature::getLevel)).ifPresent(o -> highlightChests.add(chestFilters.get(o.signature)));

        }
        for (TileEntityChest pos : highlightChests) {
            RenderUtils.drawBlockBox(pos.getPos(), Color.GREEN, 5, event.partialTicks);
            BlockPos adjacent = ChestAnalyzer.getAdjacentChest(pos);
            if (adjacent != null) RenderUtils.drawBlockBox(adjacent, Color.GREEN, 5, event.partialTicks);
        }
    }

    private static Set<Signature> getSignature(Slot slot) {
        Set<Signature> signatures = new HashSet<>();
        if (slot.getStack().getItem().getRegistryName().equals(Items.enchanted_book.getRegistryName())) {
            try {
                NBTTagCompound enchants = slot.getStack().serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes").getCompoundTag("enchantments");
                for (String enchant : enchants.getKeySet()) {
                    signatures.add(new Signature(enchant, 5));
                }
            } catch (NullPointerException ignored) {}
        }

        AttributeValueResult result = InventoryData.values.get(slot);
        if (result == null) return signatures;
        if (AttributeUtils.isArmor(result.item_id)) {
            signatures.add(new Signature(AttributeUtils.getItemType(result.item_id) + "_" + result.best_attribute.attribute, 4));
            signatures.add(new Signature("armor_" + result.best_attribute.attribute, 3));
            signatures.add(new Signature("attribute_armor", 2));
            return signatures;
        } else if (result.item_id.equals("ATTRIBUTE_SHARD")) {
            signatures.add(new Signature("attribute_shard", 2));
            signatures.add(new Signature("shard_" + result.top_display.toLowerCase(), 3));
            return signatures;
        }
        signatures.add(new Signature(result.item_id + "_" + result.best_attribute.attribute, 4));
        signatures.add(new Signature("attribute_item_" + result.best_attribute.attribute, 3));
        signatures.add(new Signature("attribute_item", 2));
        return signatures;
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!(event.gui instanceof GuiChest)) return;
        if (InventoryData.currentChestName.contains("Paid Chest") || InventoryData.currentChestName.contains("Free Chest")) return;
        GuiChest gui = (GuiChest) event.gui;
        if (ButtonManager.isChecked("sellMethod")) {
            highlightSlots.forEach((slot, method) -> {
                switch (method) {
                    case SALVAGE:
                        RenderUtils.highlight(new Color(255, 0, 0, 127), gui, slot);
                        break;
                    case AUCTION_CHEAP:
                        RenderUtils.highlight(new Color(255, 255, 0, 127), gui, slot);
                        break;
                    case AUCTION_EXPENSIVE:
                        RenderUtils.highlight(new Color(0, 255, 0, 127), gui, slot);
                        break;
                }
            });
        }
        if (ButtonManager.isChecked("sortingHelper")) {
            highlightInvSlots.forEach(o -> RenderUtils.highlight(new Color(0, 255, 255, 127), gui, o));
        }
    }

    enum SellMethod {
        SALVAGE,
        AUCTION_CHEAP,
        AUCTION_EXPENSIVE
    }

    @Getter
    private static class Signature {
        private final String signature;
        private final int level;

        public Signature(String signature, int level) {
            this.signature = signature;
            this.level = level;
        }

        @Override
        public String toString() {
            return signature + " " + level;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Signature signature1 = (Signature) obj;
            return Objects.equals(signature, signature1.signature);
        }

        @Override
        public int hashCode() {
            return Objects.hash(signature, level);
        }
    }
}
