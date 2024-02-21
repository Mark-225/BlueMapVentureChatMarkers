package com.technicjelle.bluemapchatmarkers;

import com.technicjelle.MCUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Config {
	public static final String MARKER_SET_ID = "chat-markers";

	private final BlueMapChatMarkers plugin;

	private final String markerSetName;
	private final boolean toggleable;
	private final boolean defaultHidden;
	private final long markerDuration;
	private final boolean forceful;

	private final boolean includeDefaultChannel;

	private final boolean includeAllChannels;

	private final List<String> exceptions;

	public Config(BlueMapChatMarkers plugin) {
		this.plugin = plugin;

		try {
			MCUtils.copyPluginResourceToConfigDir(plugin, "config.yml", "config.yml", false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		//Load config from disk
		plugin.reloadConfig();

		//Load config values into variables
		markerSetName = configFile().getString("MarkerSetName", "Chat Messages");
		toggleable = configFile().getBoolean("Toggleable", true);
		defaultHidden = configFile().getBoolean("DefaultHidden", false);
		markerDuration = configFile().getLong("MarkerDuration", 60);
		forceful = configFile().getBoolean("Forceful", false);

		//Venture specific values
		includeDefaultChannel = configFile().getBoolean("VentureChat.IncludeDefaultChannel", true);
		includeAllChannels = configFile().getBoolean("VentureChat.IncludeAllChannels", false);
		exceptions = configFile().getStringList("VentureChat.Exceptions");
	}

	private FileConfiguration configFile() {
		return plugin.getConfig();
	}

	public String getMarkerSetName() {
		return markerSetName;
	}

	public boolean isToggleable() {
		return toggleable;
	}

	public boolean isDefaultHidden() {
		return defaultHidden;
	}

	public long getMarkerDuration() {
		return markerDuration;
	}

	public boolean getForceful() {
		return forceful;
	}

	public boolean isIncludeDefaultChannel() {
		return includeDefaultChannel;
	}

	public boolean isIncludeAllChannels() {
		return includeAllChannels;
	}

	public List<String> getExceptions() {
		return exceptions;
	}
}
