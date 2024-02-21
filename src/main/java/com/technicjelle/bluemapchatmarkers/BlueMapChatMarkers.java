package com.technicjelle.bluemapchatmarkers;

import com.technicjelle.BMUtils;
import com.technicjelle.MCUtils;
import com.technicjelle.UpdateChecker;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.HtmlMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import mineverse.Aust1n46.chat.api.events.VentureChatEvent;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import mineverse.Aust1n46.chat.utilities.Format;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventPriority;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class BlueMapChatMarkers extends JavaPlugin implements Listener {
	private Config config;
	private UpdateChecker updateChecker;

	@Override
	public void onEnable() {

		updateChecker = new UpdateChecker("Mark-225", "BlueMapVentureChatMarkers", getDescription().getVersion());
		updateChecker.checkAsync();

		getServer().getPluginManager().registerEvents(this, this);

		BlueMapAPI.onEnable(onEnableListener);
	}

	Consumer<BlueMapAPI> onEnableListener = api -> {
		updateChecker.logUpdateMessage(getLogger());

		config = new Config(this);

		String styleFile = "textStyle.css";
		try {
			MCUtils.copyPluginResourceToConfigDir(this, styleFile, styleFile, false);
			BMUtils.copyFileToBlueMap(api, getDataFolder().toPath().resolve(styleFile), styleFile, true);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Failed to copy " + styleFile + " to BlueMap", e);
		}
	};

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled() && !config.getForceful()) return;
		BlueMapAPI api = BlueMapAPI.getInstance().orElse(null);
		if (api == null) return; //BlueMap not loaded, ignore

		Player player = event.getPlayer();

		BlueMapWorld bmWorld = api.getWorld(player.getWorld()).orElse(null);
		if (bmWorld == null) return; //world not loaded in BlueMap, ignore

		if (!api.getWebApp().getPlayerVisibility(player.getUniqueId())) return; //player hidden on BlueMap, ignore

		Location location = player.getLocation();

		String message = ChatColor.stripColor(event.getMessage());
		HtmlMarker marker = HtmlMarker.builder()
				.label(player.getName() + ": " + message)
				.position(location.getX(), location.getY() + 1.8, location.getZ()) // +1.8 to put the marker at the player's head level
				.styleClasses("chat-marker")
				.html(message)
				.build();

		//for all BlueMap Maps belonging to the BlueMap World the Player is in, add the Marker to the MarkerSet of that BlueMap World
		bmWorld.getMaps().forEach(map -> {
			// get marker-set of map (or create new marker set if none found)
			MarkerSet markerSet = map.getMarkerSets().computeIfAbsent(Config.MARKER_SET_ID, id -> MarkerSet.builder()
					.label(config.getMarkerSetName())
					.toggleable(config.isToggleable())
					.defaultHidden(config.isDefaultHidden())
					.build());

			String key = "chat-marker_" + event.hashCode();

			//add Marker to the MarkerSet
			markerSet.put(key, marker);

			//wait Seconds and remove the Marker
			Bukkit.getScheduler().runTaskLater(this,
					() -> markerSet.remove(key),
					config.getMarkerDuration() * 20L);
		});
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onVentureChat(VentureChatEvent event){
		ChatChannel channel = event.getChannel();
		if(!(config.isIncludeDefaultChannel() && Boolean.TRUE.equals(channel.isDefaultchannel()) ||
				config.isIncludeAllChannels() != config.getExceptions().contains(channel.getName()))) return;
		String message = Format.stripColor(event.getChat());

		//Absolute hack but makes this fork easier to maintain by reusing the existing method instead of reimplementing/altering it
		AsyncPlayerChatEvent dummyEvent = new AsyncPlayerChatEvent(event.isAsynchronous(), event.getMineverseChatPlayer().getPlayer(), message, event.getRecipients());
		onPlayerChat(dummyEvent);
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
		BlueMapAPI.unregisterListener(onEnableListener);
	}
}
