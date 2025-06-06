package com.golem.skyblockutils;

import com.golem.skyblockutils.init.GuiInit;
import com.golem.skyblockutils.models.CustomItem;
import com.golem.skyblockutils.models.gui.GuiPosition;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import logger.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import static com.golem.skyblockutils.Config.configFolder;

public class PersistentData implements Serializable {
	public static Map<String, GuiPosition> positions = new HashMap<>();
	public static JsonArray splits = new JsonArray();
	private static final File positionFile = new File(configFolder, "positions.json");
	private static final File splitsFile = new File(configFolder, "splits.json");
	private static final File customItemsFile = new File(configFolder, "customitems.json");

	public PersistentData() {
		positions = new HashMap<>();
	}


	public void savePositions() {
		JsonObject json = new JsonObject();
		json.add("Overlays", new Gson().toJsonTree(positions).getAsJsonObject());
		try {
			String jsonData = json.toString();
			Files.write(positionFile.toPath(), jsonData.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			Logger.error("An error occurred while saving the data: " + e.getMessage());
		}
	}

	public void saveSplits() {
		try {
			Files.write(splitsFile.toPath(), splits.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			Logger.error("An error occurred while saving the data: " + e.getMessage());
		}
	}

	public static void saveCustomItems() {
		System.out.println(Main.customItems);
		try {
			Files.write(customItemsFile.toPath(), new Gson().toJsonTree(Main.customItems).toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			Logger.error("An error occurred while saving the data: " + e.getMessage());
		}
	}

	public static void load() {
		// Implementation for loading data
		try {
			if (positionFile.exists()) {
				byte[] jsonData = Files.readAllBytes(positionFile.toPath());
				String json = new String(jsonData);
				JsonObject JsonData = new Gson().fromJson(json, JsonObject.class);
				JsonObject OverlayData = (JsonData.has("Overlays") ? JsonData.get("Overlays").getAsJsonObject() : new JsonObject());
				for (Map.Entry<String, JsonElement> entry : OverlayData.entrySet()) {
					if (entry.getValue() instanceof JsonObject) positions.put(entry.getKey(), new GuiPosition(entry.getValue().getAsJsonObject()));
				}
				Main.StaticPosition = GuiInit.getOverlayLoaded();
			}
			if (splitsFile.exists()) {
				byte[] jsonData = Files.readAllBytes(splitsFile.toPath());
				String json = new String(jsonData);
				splits = new Gson().fromJson(json, JsonArray.class);
			}
			if (customItemsFile.exists()) {
				byte[] jsonData = Files.readAllBytes(customItemsFile.toPath());
				String json = new String(jsonData);
				Type type = new TypeToken<HashMap<String, CustomItem>>(){}.getType();
				Main.customItems = new Gson().fromJson(json, type);
			}
		} catch (IOException e) {
			Logger.error("An error occurred while loading the data: " + e.getMessage());
		}
		new PersistentData();
	}
}
