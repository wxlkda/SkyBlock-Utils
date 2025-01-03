package com.golem.skyblockutils.features;

import com.golem.skyblockutils.Main;
import com.golem.skyblockutils.models.AttributeValueResult;
import com.golem.skyblockutils.utils.InventoryData;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;

import java.util.Objects;

import static com.golem.skyblockutils.Main.configFile;

public class AttributeOverlay {


	public static void drawSlot(Slot slot) {
		if (slot == null || !slot.getHasStack() || !Main.configFile.attribute_overlay) return;
		try {
			AttributeValueResult valueData = InventoryData.values.get(slot);
			if (valueData == null) return;
			if (Objects.equals(valueData.top_display, "LBIN") && !configFile.showLbinOverlay) return;

			UGraphics.disableLighting();
			UGraphics.disableDepth();
			UGraphics.disableBlend();
			UMatrixStack matrixStack = new UMatrixStack();
			matrixStack.push();
			matrixStack.translate(slot.xDisplayPosition, slot.yDisplayPosition, 1f);
			matrixStack.scale(0.8, 0.8, 1.0);

			matrixStack.runWithGlobalState(() -> {
				Main.mc.fontRendererObj.drawString(valueData.top_display, 0, 0, 0x00FFFF);
			});

			matrixStack.pop();
			UGraphics.enableLighting();
			UGraphics.enableDepth();
			UGraphics.enableBlend();
			if (valueData.bottom_display > 0) {
				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				GlStateManager.disableBlend();
				Main.mc.fontRendererObj.drawStringWithShadow(String.valueOf(valueData.bottom_display),
						(float) (slot.xDisplayPosition + 17 - Main.mc.fontRendererObj.getStringWidth(String.valueOf(valueData.bottom_display))),
						slot.yDisplayPosition + 9,
						0xFFFFFFFF
				);
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
			}
		} catch (Exception ignored) {
		}

	}
}
