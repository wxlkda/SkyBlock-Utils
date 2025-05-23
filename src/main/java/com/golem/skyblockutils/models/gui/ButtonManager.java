package com.golem.skyblockutils.models.gui;

import com.golem.skyblockutils.Main;
import com.golem.skyblockutils.configs.overlays.ContainerConfig;
import com.golem.skyblockutils.utils.LocationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static com.golem.skyblockutils.Main.config;

public class ButtonManager {
    private static final HashMap<String, GuiCheckBox> checkBoxes = new HashMap<>();
    public static boolean mousePressed = false;
    ArrayList<String> activeBoxes = new ArrayList<>();

    public ButtonManager() {
        checkBoxes.put("containerValue", new GuiCheckBox(0, 5, 0, "Container Value", false));
//        checkBoxes.put("sellMethod", new GuiCheckBox(1, 5, 0, "Highlight Sell Method", false));
//        checkBoxes.put("auctionHelper", new GuiCheckBox(2, 5, 0, "Auction Helper", false));
//        checkBoxes.put("sortingHelper", new GuiCheckBox(3, 5, 0, "Sorting Helper", false));
    }

    @SubscribeEvent
    public void guiDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!(event.gui instanceof GuiChest)) return;
        activeBoxes.clear();
        if (config.getConfig().overlayCategory.containerValueConfig.containerValueOverlay != ContainerConfig.ContainerValuePosition.OFF) activeBoxes.add("containerValue");
        if (config.getConfig().auctionCategory.highlightSellMethod) activeBoxes.add("sellMethod");
        if (config.getConfig().auctionCategory.auctionHelper) activeBoxes.add("auctionHelper");
        if (config.getConfig().auctionCategory.sortingHelper && Objects.equals(LocationUtils.getLocation(), "dynamic")) activeBoxes.add("sortingHelper");

        int y = event.gui.height - 25;

        for (String box : checkBoxes.keySet()) {
            if (activeBoxes.contains(box)) {
                checkBoxes.get(box).yPosition = y;
                checkBoxes.get(box).drawButton(event.gui.mc, 0, 0);
                y -= 25;
            } else {
                checkBoxes.get(box).setIsChecked(false);
            }
        }
    }

    @SubscribeEvent
    public void onGuiClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (Mouse.getEventButtonState()) {
            int mouseX = Mouse.getEventX() * event.gui.width / Minecraft.getMinecraft().displayWidth;
            int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / Minecraft.getMinecraft().displayHeight - 1;

            checkBoxes.values().forEach(box -> box.mousePressed(Main.mc, mouseX, mouseY));

        }
    }

    public static boolean isChecked(String box) {
        return checkBoxes.get(box).isChecked();
    }
}
