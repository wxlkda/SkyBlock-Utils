package com.golem.skyblockutils.init;

import com.golem.skyblockutils.ChatListener;
import com.golem.skyblockutils.Main;
import com.golem.skyblockutils.events.ChannelHandlerInput;
import com.golem.skyblockutils.features.*;
import com.golem.skyblockutils.features.General.CustomEmotes;
import com.golem.skyblockutils.features.General.Elite500;
import com.golem.skyblockutils.features.General.Perspective;
import com.golem.skyblockutils.features.KuudraFight.EnderPearl;
import com.golem.skyblockutils.features.KuudraFight.Kuudra;
import com.golem.skyblockutils.features.KuudraFight.TokenHelper;
import com.golem.skyblockutils.features.KuudraFight.Waypoints;
import com.golem.skyblockutils.models.Overlay.TextOverlay.*;
import com.golem.skyblockutils.models.gui.ButtonManager;
import com.golem.skyblockutils.utils.InventoryData;
import com.golem.skyblockutils.utils.LocationUtils;
import com.golem.skyblockutils.utils.ToolTipListener;
import net.minecraftforge.common.MinecraftForge;

public class EventInit {
	public static void registerEvents() {
		Object[] listeners = {
				new Main(),
				new ToolTipListener(),
				new KuudraOverlay(),
				new KeybindsInit(),
				new ContainerValue(),
				new ChatListener(),
				new KuudraHealth(),
				new Kuudra(),
				new Waypoints(),
				new GuiEvent(),
				new CombineHelper(),
				new EnderPearl(),
				new LocationUtils(),
				new InventoryData(),
				new ChatWaypoints(),
				new AutoUpdater(),
				new Perspective(),
				new CustomEmotes(),
				new Elite500(),
				new TokenHelper(),
				new ChannelHandlerInput(),
				new ChestAnalyzer(),
				new SellingHelper(),
				new ButtonManager(),
				new AuctionHelper()
		//new TrackKills()
		};

		for (Object listener : listeners) {
			MinecraftForge.EVENT_BUS.register(listener);
		}



	}

	public static void registerOverlays() {
		Object[] listeners = {
				new AlignOverlay(),
				new RagnarokOverlay(),
				new EndstoneOverlay(),
				new CratesOverlay(),
				new AlertOverlay(),
				new SplitsOverlay(),
				new ReaperOverlay(),
				new DamageOverlay(),
				new FatalTempoOverlay(),
				new ProfitOverlay(),
				new ContainerOverlay(),
				new TimerOverlay()
		};

		for (Object listener : listeners) {
			MinecraftForge.EVENT_BUS.register(listener);
		}
	}
}