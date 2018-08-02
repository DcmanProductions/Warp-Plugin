package com.dcman58.warpplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

@SuppressWarnings("all")
public class Main extends JavaPlugin implements Listener, TabCompleter {

	Map<String, Location> warps = new HashMap<String, Location>();
	List<String> names;
	CommandSender sender;

	@Override
	public void onEnable() {
		message(ChatColor.GOLD + "Now Enabling the DcCraft Warp Plugin");
		this.getCommand("warp").setExecutor(this);
		this.getCommand("warp").setTabCompleter(this);
		this.getCommand("spawn").setExecutor(new WorldSpawn());
		getConfig().options().copyDefaults();
		saveDefaultConfig();
		Bukkit.getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		message(ChatColor.GREEN + "Disabling Warp Plugin");
	}

	public void message(String message) {
		System.out.println(message);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		List<String> finalString = new ArrayList<String>();
		List<String> nameString = new ArrayList<String>();
		nameString.addAll(getConfig().getConfigurationSection("Name").getKeys(false));

		List<String> l = Arrays.asList("home", "list", "set", "random", "me", "help", "?");
		List<String> args2String = new ArrayList<String>();
		if (args.length == 1) {
			for (String j : l) {
				if (j.toLowerCase().startsWith(args[0])) {
					finalString.add(j);
				}
			}
			for (String j : nameString) {
				if (j.toLowerCase().startsWith(args[0])) {
					finalString.add(j);
				}
			}

		} else if (args.length == 2) {

			finalString.clear();

			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.equals(player))
					return null;
				else if (args[1].startsWith(p.getDisplayName())) {
					finalString.add(p.getDisplayName());
				}
			}
		}
		return finalString;

	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		back(player);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
		this.sender = sender;

		if (sender instanceof Player) {
			Player player = (Player) sender;
			// if (args[0].equalsIgnoreCase("home")) {
			// if (player.getBedSpawnLocation() != null) {
			// back(player);
			// player.teleport(new
			// Location(player.getBedSpawnLocation().getWorld(),
			// player.getBedSpawnLocation().getX(),
			// player.getBedSpawnLocation().getY() + 1,
			// player.getBedSpawnLocation().getZ()));
			// return true;
			// } else {
			// player.sendMessage("Currenlty, You have no home set");
			// }
			//
			// }
			if (args[0].equalsIgnoreCase("set") && !(args[1].equalsIgnoreCase("back")) && !(args[1].equalsIgnoreCase("private")) && player.hasPermission("dccraft.warp.set")) {
				// if (args[1].equalsIgnoreCase("home")) {
				// player.setBedSpawnLocation(player.getLocation());
				// player.sendMessage(ChatColor.GREEN + "Player bed spawn set");
				// return true;
				// } else {
				String name = args[1].toLowerCase();
				getConfig().set("Name." + name + ".world", player.getLocation().getWorld().getName());
				getConfig().set("Name." + name + ".x", player.getLocation().getX());
				getConfig().set("Name." + name + ".y", player.getLocation().getY());
				getConfig().set("Name." + name + ".z", player.getLocation().getZ());
				getConfig().set("Name." + name + ".yaw", player.getLocation().getYaw());
				getConfig().set("Name." + name + ".pitch", player.getLocation().getPitch());
				saveConfig();
				player.sendMessage(ChatColor.GREEN + "Warp Saved as: " + ChatColor.GOLD + args[1]);
				return true;

				// }
			} else if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("back")) {
				player.sendMessage("The keyword back is used by another aspect of the warp plugin. Try Something else.");
			} else if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("private")) {
				if (args.length < 3) {
					player.sendMessage(ChatColor.RED + "To Create a Private waypoint type: /warp set private <name>");
				} else {
					String name = args[2].toLowerCase();
					getConfig().set(player.getDisplayName() + "." + name + ".world", player.getLocation().getWorld().getName());
					getConfig().set(player.getDisplayName() + "." + name + ".x", player.getLocation().getX());
					getConfig().set(player.getDisplayName() + "." + name + ".y", player.getLocation().getY());
					getConfig().set(player.getDisplayName() + "." + name + ".z", player.getLocation().getZ());
					getConfig().set(player.getDisplayName() + "." +name + ".yaw", player.getLocation().getYaw());
					getConfig().set(player.getDisplayName() + "." + name + ".pitch", player.getLocation().getPitch());
					saveConfig();
					player.sendMessage(ChatColor.GREEN + "Private Warp Saved as: " + ChatColor.GOLD + args[2]);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("remove") && player.hasPermission("dccraft.warp.remove")) {
				getConfig().getConfigurationSection("Name").set(args[1].toLowerCase(), null);

			} else if (args[0].equalsIgnoreCase("remove") && !(player.hasPermission("dccraft.warp.remove"))) {
				if(args[1].equalsIgnoreCase("private")){
					getConfig().getConfigurationSection(player.getDisplayName()).set(args[2].toLowerCase(), null);
				}
			}else if (args[0].equalsIgnoreCase("list")) {
				player.sendMessage(ChatColor.GOLD + getList().toString());
				return true;
			} else if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {

				player.sendMessage(ChatColor.GREEN + "/warp set <warp name>\n/warp random [player name(optional)]\n/warp set private <warp name>\n/warp list\n/warp me <other player name> -- to silently tp to another player\n");

			} else if (args[0].equalsIgnoreCase("random")) {
				Random r = new Random();
				for (Player online : Bukkit.getOnlinePlayers()) {
					if (args.length < 2) {
						back(player);
						double x = player.getLocation().getX() + r.nextInt(55000), z = player.getLocation().getZ() + r.nextInt(55000);
						double y = 160;
						Location loc = new Location(player.getWorld(), x, y, z);
						while (loc.getBlock().getType() == Material.AIR && y > 60) {
							y--;
							loc = new Location(player.getWorld(), x, y, z);
						}
						player.sendMessage("Trying to asses the grounds " + y + "\nGround Block " + loc.getBlock().getType());
						loc = new Location(player.getWorld(), x, y + 2, z);
						player.teleport(loc);
						return true;
					} else if (args[1].equalsIgnoreCase(online.getDisplayName())) {
						back(online);
						online.teleport(new Location(online.getWorld(), online.getLocation().getX() + r.nextInt(55000), online.getLocation().getY(), online.getLocation().getZ() + r.nextInt(55000)));
						return true;
					}
				}
				return true;
			} else if (args[0].equalsIgnoreCase("me")) {
				for (Player online : Bukkit.getOnlinePlayers()) {
					if (args[1].equalsIgnoreCase(online.getDisplayName())) {
						back(player);
						player.teleport(online);
					} else {
						player.sendMessage("Player not online");
					}
				}
			} else if (args[0].equalsIgnoreCase("back")) {
				float yaw = getConfig().getInt(player.getDisplayName() + ".back.yaw"), pitch = getConfig().getInt(player.getDisplayName() + "back.pitch");
				double x = getConfig().getInt(player.getDisplayName() + ".back.x"), y = getConfig().getInt(player.getDisplayName() + ".back.y"), z = getConfig().getInt(player.getDisplayName() + ".back.z");
				World w = Bukkit.getWorld(getConfig().getString(player.getDisplayName() + ".back.world"));
				Location loc = new Location(w, x, y, z, yaw, pitch);
				back(player);
				player.teleport(loc);
				return true;
			} else {
				for (String key : getConfig().getConfigurationSection(player.getDisplayName()).getKeys(true)) {

					if (args[0].equalsIgnoreCase(key)) {
						float yaw = getConfig().getInt(player.getDisplayName() + "." + args[0] + ".yaw");
						float pitch = getConfig().getInt(player.getDisplayName() + "." + args[0] + ".pitch");
						double x = getConfig().getDouble(player.getDisplayName() + "." + args[0] + ".x"), y = getConfig().getDouble(player.getDisplayName() + "." + args[0] + ".y"), z = getConfig().getDouble(player.getDisplayName() + "." + args[0] + ".z");
						World w = Bukkit.getWorld(getConfig().getString(player.getDisplayName() + "." + args[0] + ".world"));
						Location loc = new Location(w, x, y, z, yaw, pitch);
						for (Player online : Bukkit.getOnlinePlayers()) {
							if (args.length > 1 && !(args[0].equalsIgnoreCase("list")) && args[1].equalsIgnoreCase(online.getDisplayName())) {
								back(online);
								online.teleport(loc);
								return true;
							}
						}
						back(player);
						player.teleport(loc);
						return true;
					}

				}
				for (String key : getConfig().getConfigurationSection("Name").getKeys(true)) {
					String name = args[0].toLowerCase();
					if (name.equalsIgnoreCase(key)) {
						float yaw = getConfig().getInt("Name." + name + ".yaw");
						float pitch = getConfig().getInt("Name." + name + ".pitch");
						double x = getConfig().getDouble("Name." + name + ".x"), y = getConfig().getDouble("Name." + name + ".y"), z = getConfig().getDouble("Name." + name + ".z");
						World w = Bukkit.getWorld(getConfig().getString("Name." + name + ".world"));
						Location loc = new Location(w, x, y, z, yaw, pitch);
						for (Player online : Bukkit.getOnlinePlayers()) {
							if (args.length > 1 && !(name.equalsIgnoreCase("list")) && args[1].equalsIgnoreCase(online.getDisplayName())) {
								back(online);
								online.teleport(loc);
								return true;
							}
						}
						back(player);
						player.teleport(loc);
						return true;
					} else if (!(args[0].equalsIgnoreCase(key))) {
					}
				}
			}
		} else {
			Random r = new Random();
			for (Player online : Bukkit.getOnlinePlayers()) {
				if (args[0].equalsIgnoreCase("random") && args[1].equalsIgnoreCase(online.getDisplayName())) {
					back(online);
					int x = (int) online.getLocation().getX() + r.nextInt(55000), z = (int) online.getLocation().getZ() + r.nextInt(55000);
					int y = online.getWorld().getHighestBlockYAt(x, z);
					Location loc = new Location(online.getWorld(), x, 160, z);
					online.sendMessage("Y Coord is " + y);
					while (loc.getBlock().getType() == Material.AIR) {
						y--;
					}
					loc = new Location(online.getWorld(), x, y, z);
					online.teleport(loc);
				}

				for (String key : getConfig().getConfigurationSection("Name").getKeys(true)) {
					if (args[0].equalsIgnoreCase(key)) {
						float yaw = getConfig().getInt("Name." + args[0] + ".yaw");
						float pitch = getConfig().getInt("Name." + args[0] + ".pitch");
						double x = getConfig().getDouble("Name." + args[0] + ".x"), y = getConfig().getDouble("Name." + args[0] + ".y"), z = getConfig().getDouble("Name." + args[0] + ".z");
						World w = Bukkit.getWorld(getConfig().getString("Name." + args[0] + ".world"));
						Location loc = new Location(w, x, y, z, yaw, pitch);
						if (args.length > 1 && !(args[0].equalsIgnoreCase("list")) && args[1].equalsIgnoreCase(online.getDisplayName())) {
							back(online);
							online.teleport(loc);
							return true;
						}
					}
				}

			}

			message("This command can only be run as a player");
		}
		return false;
	}

	public void back(Player player) {
		player.sendMessage(ChatColor.GREEN + "Back point saved you can type \"" + ChatColor.GOLD + ChatColor.BOLD + "/warp back\"" + ChatColor.GREEN + ChatColor.RESET + " to teleport you back to where you warped from");
		getConfig().set(player.getDisplayName() + ".back.world", player.getLocation().getWorld().getName());
		getConfig().set(player.getDisplayName() + ".back.x", player.getLocation().getX());
		getConfig().set(player.getDisplayName() + ".back.y", player.getLocation().getY());
		getConfig().set(player.getDisplayName() + ".back.z", player.getLocation().getZ());
		getConfig().set(player.getDisplayName() + ".back.yaw", player.getLocation().getYaw());
		getConfig().set(player.getDisplayName() + ".back.pitch", player.getLocation().getPitch());
		saveConfig();
	}

	public List<String> getList() {
		Player player = (Player) sender;
		List<String> f = Lists.newArrayList();
		f.addAll(getConfig().getConfigurationSection("Name").getKeys(false));
		f.addAll(getConfig().getConfigurationSection(player.getDisplayName()).getKeys(false));
		return f;
	}

}
