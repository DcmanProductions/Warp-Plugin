package com.dcman58.warpplugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("all")
public class Main extends JavaPlugin implements Listener {

	Map<String, Location> warps = new HashMap<String, Location>();
	List<String> names;

	@Override
	public void onEnable() {
		message("Now Enabling the DcCraft Warp Plugin");
		this.getCommand("warp").setExecutor(this);
		getConfig().options().copyDefaults();
		saveDefaultConfig();
		Bukkit.getPluginManager().registerEvents(this, this);

	}

	public void message(String message) {
		System.out.println(message);
	}

	public void editConfig() {
		List<String> s = getConfig().getStringList("Name");
		for (String key : warps.keySet()) {

		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {

		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args[0].equalsIgnoreCase("home")) {
				if (player.getBedSpawnLocation() != null) {
					player.teleport(player.getBedSpawnLocation());
					return true;
				} else {
					player.sendMessage("Currenlty, You have no home set");
				}

			}
			if (args[0].equalsIgnoreCase("set")) {
				if (args[1].equalsIgnoreCase("home")) {
					player.setBedSpawnLocation(player.getLocation());
					player.sendMessage(ChatColor.GREEN + "Player bed spawn set");
					return true;
				} else {
					getConfig().set("Name." + args[1] + ".world", player.getLocation().getWorld().getName());
					getConfig().set("Name." + args[1] + ".x", player.getLocation().getX());
					getConfig().set("Name." + args[1] + ".y", player.getLocation().getY());
					getConfig().set("Name." + args[1] + ".z", player.getLocation().getZ());
					getConfig().set("Name." + args[1] + ".yaw", player.getLocation().getYaw());
					getConfig().set("Name." + args[1] + ".pitch", player.getLocation().getPitch());
					saveConfig();
					player.sendMessage(ChatColor.GREEN + "Warp Saved as: " + ChatColor.GOLD + args[1]);
					return true;

				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				getConfig().getConfigurationSection("Name").set(args[1], null);
			} else if (args[0].equalsIgnoreCase("list")) {
				for (String key : getConfig().getConfigurationSection("Name").getKeys(false)) {
					player.sendMessage(ChatColor.GOLD + key);
				}
				return true;
			} else if (args[0].equalsIgnoreCase("random")) {
				Random r = new Random();
				for (Player online : Bukkit.getOnlinePlayers()) {
					if (args.length < 2) {
						player.teleport(new Location(player.getWorld(), player.getLocation().getX() + r.nextInt(55000), player.getLocation().getY() + 30, player.getLocation().getZ() + r.nextInt(55000)));
						return true;
					} else if (args[1].equalsIgnoreCase(online.getDisplayName())) {
						online.teleport(new Location(player.getWorld(), player.getLocation().getX() + r.nextInt(55000), player.getLocation().getY() + 30, player.getLocation().getZ() + r.nextInt(55000)));
						return true;
					}
				}
				return true;
			} else if (args[0].equalsIgnoreCase("me")) {
				for (Player online : Bukkit.getOnlinePlayers()) {
					if (args[1].equalsIgnoreCase(online.getDisplayName())) {
						player.teleport(online);
					} else {
						player.sendMessage("Player not online");
					}
				}
			} else {
				for (String key : getConfig().getConfigurationSection("Name").getKeys(true)) {
					if (args[0].equalsIgnoreCase(key)) {
						float yaw = getConfig().getInt("Name." + args[0] + ".yaw");
						float pitch = getConfig().getInt("Name." + args[0] + ".pitch");
						double x = getConfig().getDouble("Name." + args[0] + ".x"), y = getConfig().getDouble("Name." + args[0] + ".y"), z = getConfig().getDouble("Name." + args[0] + ".z");
						World w = Bukkit.getWorld(getConfig().getString("Name." + args[0] + ".world"));
						Location loc = new Location(w, x, y, z, yaw, pitch);
						for (Player online : Bukkit.getOnlinePlayers()) {
							if (args.length > 1 && !(args[0].equalsIgnoreCase("list")) && args[1].equalsIgnoreCase(online.getDisplayName())) {
								online.teleport(loc);
								return true;
							}
						}
						player.teleport(loc);
						return true;
					} else if (!(args[0].equalsIgnoreCase(key))) {
					}
				}
			}
		} else {
			message("This command can only be run as a player");
		}
		return false;
	}

}
