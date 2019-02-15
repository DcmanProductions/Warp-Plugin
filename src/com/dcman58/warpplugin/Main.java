package com.dcman58.warpplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("all")
public class Main extends JavaPlugin implements CommandExecutor, Listener/* , TabCompleter */ {

	Map<String, Location> warps = new HashMap<String, Location>();
	List<String> names;
	CommandSender sender;

	static Main plugin;

	@Override
	public void onEnable() {
		plugin = this;
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

	// @EventHandler
	// public void onPlayerJoin(PlayerJoinEvent e) {
	// Player p = e.getPlayer();
	// createPrivateWarp(p, "joinLoc");
	//
	// }

	public void message(String message) {
		System.out.println(message);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		List<String> finalString = new ArrayList<String>();
		List<String> nameString = new ArrayList<String>();
		nameString.addAll(getAllWarps(player));
		List<String> l = Arrays.asList("list", "set", "random", "offline", "remove", "rename", "invite", "move", "me", "help", "?");
		List<String> args2String = new ArrayList<String>();
		List<String> allPlayers = new ArrayList<String>();

		for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
			allPlayers.add(offline.getName());
		}

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

		} else if (args[0].equalsIgnoreCase("me")) {
			if (args.length == 2) {
				finalString.clear();

				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.equals(player))
						return null;
					else if (args[1].startsWith(p.getDisplayName())) {
						finalString.add(p.getDisplayName());
					}
				}
			} else if (args[0].equalsIgnoreCase("rename")) {
				for (String j : nameString) {
					if (j.toLowerCase().startsWith(args[1])) {
						finalString.add(j);
					}
				}

			} else if (args[0].equalsIgnoreCase("remove")) {
				finalString.clear();
				finalString.addAll(getPrivateWarps(player));
				if (player.hasPermission("dccraft.warp.remove") || player.hasPermission("dccraft.warp.*"))
					finalString.addAll(getPublicWarps());
			} else if (args[0].equalsIgnoreCase("set")) {
				finalString.clear();
				finalString.add("private");
			} else if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("move")) {
				if (args.length == 2) {
					finalString.clear();
					finalString.addAll(allPlayers);
					return finalString;
				} else if (args.length == 3) {
					finalString.clear();
					finalString.addAll(getPrivateWarps(player));
					return finalString;
				}
			}
		}
		return finalString;

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		back(player);

		for (String s : getPrivateWarps(player)) {
			if (s.equalsIgnoreCase("home")) {
				Warp(player, "home");
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 0) {
				return false;
			} else if (args[0].equalsIgnoreCase("set") && !(args[1].equalsIgnoreCase("back"))) {
				String name = args[1].toLowerCase();
				if (!(args[1].equalsIgnoreCase("private")) && (player.hasPermission("dccraft.warp.set") || player.hasPermission("dccraft.warp.*"))) {
					for (String s : getAllWarps(player)) {
						if (s.equalsIgnoreCase(name)) {
							player.sendMessage("Sorry " + name + " is in use as a warp...  Please be more creative.");
							return false;
						} else {
							createPublicWarp(player, name);
							saveConfig();
							return true;
						}
					}
				} else {
					if (!args[1].equalsIgnoreCase("private")) {
						for (String s : getAllWarps(player)) {
							if (s.equalsIgnoreCase(name)) {
								player.sendMessage("Sorry " + name + " is in use as a warp...  Please be more creative.");
								return false;
							} else {
								createPrivateWarp(player, name);
								saveConfig();
								return true;
							}
						}
					} else {
						if (args.length < 3) {
							player.sendMessage(ChatColor.RED + "To Create a Private waypoint type: /warp set private <name>");
						} else {
							name = args[2].toLowerCase();
							createPrivateWarp(player, name);
							saveConfig();
							return true;
						}
					}
				}
				if (args[1].equalsIgnoreCase("back")) {
					player.sendMessage("The keyword back is used by another aspect of the warp plugin. Try Something else.");
					return false;
				}
			} else if (args[0].equalsIgnoreCase("invite")) {
				if (args.length < 3) {
					player.sendMessage("/warp invite [player name] [warp name]");
				} else {
					for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
						if (args[1].equalsIgnoreCase(p.getName())) {
							try {
								inviteWarp(player, p, args[2]);
								return true;
							} catch (NullPointerException e) {
								player.sendMessage(ChatColor.RED + "Player 2 is set to NULL");
							} catch (ArrayIndexOutOfBoundsException e) {
								player.sendMessage(ChatColor.RED + "ArrayIndexOutOfBoundException\nERROR: " + e.getMessage());
							} catch (Exception e) {
								player.sendMessage(ChatColor.RED + "ERROR: " + e.getMessage());
								e.printStackTrace();
							}

						}
					}
				}
			} else if (args[0].equalsIgnoreCase("offline")) {
				for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
					player.sendMessage(ChatColor.GOLD + "Player " + p.getName());
				}
			} else if (args[0].equalsIgnoreCase("move")) {
				if (args.length < 3) {
					player.sendMessage("/warp move [player name] [warp name]");
				} else {
					for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
						if (args[1].equalsIgnoreCase(p.getName())) {
							try {
								inviteWarp(player, p, args[2]);
							} catch (NullPointerException e) {
								player.sendMessage(ChatColor.RED + "Player 2 is set to NULL");
							} catch (ArrayIndexOutOfBoundsException e) {
								player.sendMessage(ChatColor.RED + "ArrayIndexOutOfBoundException\nERROR: " + e.getMessage());
							} catch (Exception e) {
								player.sendMessage(ChatColor.RED + "ERROR: " + e.getMessage());
								e.printStackTrace();
							}

						}
					}
				}
			} else if (args[0].equalsIgnoreCase("rename")) {
				renameWarp(player, args[1].toLowerCase(), args[2].toLowerCase());
			} else if (args[0].equalsIgnoreCase("remove")) {
				boolean hasPublic = true, hasPrivate = true;
				if (player.hasPermission("dccraft.warp.remove") || player.hasPermission("dccraft.warp.*")) {
					try {
						getConfig().getConfigurationSection("Name").set(args[1].toLowerCase(), null);
						player.sendMessage("Warp Removed: " + args[1]);
					} catch (Exception e) {
						hasPublic = false;
					}
					try {
						getConfig().getConfigurationSection(player.getDisplayName()).set(args[1].toLowerCase(), null);
					} catch (Exception e) {
						hasPrivate = false;
					}

					if (!hasPrivate && !hasPublic) {
						player.sendMessage(ChatColor.RED + "ERROR: " + args[1] + " doesn't exist try /warp list to find it");
					}
				} else {
					try {
						getConfig().getConfigurationSection(player.getDisplayName()).set(args[2].toLowerCase(), null);
						player.sendMessage("Private Warp Removed: " + args[1]);
					} catch (Exception e) {
						player.sendMessage(ChatColor.RED + "ERROR: " + args[1] + " doesn't Exist or you don't have proper permissions to remove it.");
					}
				}

			} else if (args[0].equalsIgnoreCase("list")) {
				
				for(String s : getAllWarps(player)){
//					PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
//					PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("\"text\":\"%s\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/warp %s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Warp To %s\",\"color\":\"green\"}]}}}"));
					player.sendMessage(ChatColor.GOLD + s);
				}
				
//				 player.sendMessage(ChatColor.GOLD + getAllWarps(player).toString().replace("[", "").replace("]", "").replace(",", "\n"));
				return true;
			} else if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
				player.sendMessage(ChatColor.GREEN + "/warp set <warp name>\n/warp random [player name(optional)]\n/warp set private <warp name>\n/warp list\n/warp me <other player name> -- to silently tp to another player\n");
				return true;
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
				return Warp(player, args[0]);
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

			if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
				message(ChatColor.GREEN + "/warp set <warp name>\n/warp random [player name(optional)]\n/warp set private <warp name>\n/warp list\n/warp me <other player name> -- to silently tp to another player\n");
				return true;
			}

			if (args[0].equalsIgnoreCase("list")) {
				for (String s : getPublicWarps()) {
					message(s);
				}
				return true;
			}

			message("This command can only be run as a player");
		}
		return false;
	}

	private boolean inviteWarp(Player player, OfflinePlayer player2, String name) {
		for (String s : getPrivateWarps(player)) {
			if (s.equalsIgnoreCase(name)) {
				float yaw = getConfig().getInt(player.getDisplayName() + "." + name + ".yaw");
				float pitch = getConfig().getInt(player.getDisplayName() + "." + name + ".pitch");
				double x = getConfig().getDouble(player.getDisplayName() + "." + name + ".x"), y = getConfig().getDouble(player.getDisplayName() + "." + name + ".y"), z = getConfig().getDouble(player.getDisplayName() + "." + name + ".z");
				World w = Bukkit.getWorld(getConfig().getString(player.getDisplayName() + "." + name + ".world"));

				getConfig().set(player2.getName() + "." + name + ".world", w.getName());
				getConfig().set(player2.getName() + "." + name + ".x", x);
				getConfig().set(player2.getName() + "." + name + ".y", y);
				getConfig().set(player2.getName() + "." + name + ".z", z);
				getConfig().set(player2.getName() + "." + name + ".yaw", yaw);
				getConfig().set(player2.getName() + "." + name + ".pitch", pitch);
				player.sendMessage(ChatColor.GREEN + "Warp Invite Sent: " + ChatColor.GOLD + name + " --> " + player2.getName());
				saveConfig();
				return true;
			}
		}
		return false;
	}

	private boolean moveWarp(Player player, OfflinePlayer player2, String name) {
		for (String s : getPrivateWarps(player)) {
			if (s.equalsIgnoreCase(name)) {
				float yaw = getConfig().getInt(player.getDisplayName() + "." + name + ".yaw");
				float pitch = getConfig().getInt(player.getDisplayName() + "." + name + ".pitch");
				double x = getConfig().getDouble(player.getDisplayName() + "." + name + ".x"), y = getConfig().getDouble(player.getDisplayName() + "." + name + ".y"), z = getConfig().getDouble(player.getDisplayName() + "." + name + ".z");
				World w = Bukkit.getWorld(getConfig().getString(player.getDisplayName() + "." + name + ".world"));

				getConfig().set(player2.getName() + "." + name + ".world", w.getName());
				getConfig().set(player2.getName() + "." + name + ".x", x);
				getConfig().set(player2.getName() + "." + name + ".y", y);
				getConfig().set(player2.getName() + "." + name + ".z", z);
				getConfig().set(player2.getName() + "." + name + ".yaw", yaw);
				getConfig().set(player2.getName() + "." + name + ".pitch", pitch);
				player.sendMessage(ChatColor.GREEN + "Warp Owner Changed: " + ChatColor.GOLD + name + " --> " + player2.getName());
				getConfig().getConfigurationSection(player.getDisplayName()).set(name, null);
				saveConfig();
				return true;
			}
		}
		return false;
	}

	private boolean renameWarp(Player player, String name, String renamed) {
		if (player.hasPermission("dccraft.warp.set") || player.hasPermission("dccraft.warp.*")) {
			for (String s : getPublicWarps()) {
				if (s.equalsIgnoreCase(name)) {
					float yaw = getConfig().getInt("Name." + name + ".yaw");
					float pitch = getConfig().getInt("Name." + name + ".pitch");
					double x = getConfig().getDouble("Name." + name + ".x"), y = getConfig().getDouble("Name." + name + ".y"), z = getConfig().getDouble("Name." + name + ".z");
					World w = Bukkit.getWorld(getConfig().getString("Name." + name + ".world"));

					getConfig().set("Name." + renamed + ".world", w.getName());
					getConfig().set("Name." + renamed + ".x", x);
					getConfig().set("Name." + renamed + ".y", y);
					getConfig().set("Name." + renamed + ".z", z);
					getConfig().set("Name." + renamed + ".yaw", yaw);
					getConfig().set("Name." + renamed + ".pitch", pitch);
					player.sendMessage(ChatColor.GREEN + "Public Warp Renamed: " + ChatColor.GOLD + name + " --> " + renamed);
					getConfig().getConfigurationSection("Name").set(name, null);
					saveConfig();
					return true;
				}
			}
			for (String s : getPrivateWarps(player)) {
				if (s.equalsIgnoreCase(name)) {
					float yaw = getConfig().getInt(player.getDisplayName() + "." + name + ".yaw");
					float pitch = getConfig().getInt(player.getDisplayName() + "." + name + ".pitch");
					double x = getConfig().getDouble(player.getDisplayName() + "." + name + ".x"), y = getConfig().getDouble(player.getDisplayName() + "." + name + ".y"), z = getConfig().getDouble(player.getDisplayName() + "." + name + ".z");
					World w = Bukkit.getWorld(getConfig().getString(player.getDisplayName() + "." + name + ".world"));

					getConfig().set(player.getDisplayName() + "." + renamed + ".world", w.getName());
					getConfig().set(player.getDisplayName() + "." + renamed + ".x", x);
					getConfig().set(player.getDisplayName() + "." + renamed + ".y", y);
					getConfig().set(player.getDisplayName() + "." + renamed + ".z", z);
					getConfig().set(player.getDisplayName() + "." + renamed + ".yaw", yaw);
					getConfig().set(player.getDisplayName() + "." + renamed + ".pitch", pitch);
					player.sendMessage(ChatColor.GREEN + "Private Warp Renamed: " + ChatColor.GOLD + name + " --> " + renamed);
					getConfig().getConfigurationSection(player.getDisplayName()).set(name, null);
					saveConfig();
					return true;
				}
			}
		} else {
			for (String s : getPrivateWarps(player)) {
				if (s.equalsIgnoreCase(name)) {
					float yaw = getConfig().getInt(player.getDisplayName() + "." + name + ".yaw");
					float pitch = getConfig().getInt(player.getDisplayName() + "." + name + ".pitch");
					double x = getConfig().getDouble(player.getDisplayName() + "." + name + ".x"), y = getConfig().getDouble(player.getDisplayName() + "." + name + ".y"), z = getConfig().getDouble(player.getDisplayName() + "." + name + ".z");
					World w = Bukkit.getWorld(getConfig().getString(player.getDisplayName() + "." + name + ".world"));

					getConfig().set(player.getDisplayName() + "." + renamed + ".world", w.getName());
					getConfig().set(player.getDisplayName() + "." + renamed + ".x", x);
					getConfig().set(player.getDisplayName() + "." + renamed + ".y", y);
					getConfig().set(player.getDisplayName() + "." + renamed + ".z", z);
					getConfig().set(player.getDisplayName() + "." + renamed + ".yaw", yaw);
					getConfig().set(player.getDisplayName() + "." + renamed + ".pitch", pitch);
					getConfig().getConfigurationSection(player.getDisplayName()).set(name, null);
					player.sendMessage(ChatColor.GREEN + "Private Warp Renamed: " + ChatColor.GOLD + name + " --> " + renamed);
					saveConfig();
					return true;
				}
			}
		}
		return false;
	}

	public boolean Warp(Player player, String arg) {

		for (String key : getConfig().getConfigurationSection(player.getDisplayName()).getKeys(true)) {

			if (arg.equalsIgnoreCase(key)) {
				float yaw = getConfig().getInt(player.getDisplayName() + "." + arg + ".yaw");
				float pitch = getConfig().getInt(player.getDisplayName() + "." + arg + ".pitch");
				double x = getConfig().getDouble(player.getDisplayName() + "." + arg + ".x"), y = getConfig().getDouble(player.getDisplayName() + "." + arg + ".y"), z = getConfig().getDouble(player.getDisplayName() + "." + arg + ".z");
				World w = Bukkit.getWorld(getConfig().getString(player.getDisplayName() + "." + arg + ".world"));
				Location loc = new Location(w, x, y, z, yaw, pitch);
				back(player);
				player.teleport(loc);
				return true;
			}

		}
		for (String key : getConfig().getConfigurationSection("Name").getKeys(true)) {
			String name = arg.toLowerCase();
			if (name.equalsIgnoreCase(key)) {
				float yaw = getConfig().getInt("Name." + name + ".yaw");
				float pitch = getConfig().getInt("Name." + name + ".pitch");
				double x = getConfig().getDouble("Name." + name + ".x"), y = getConfig().getDouble("Name." + name + ".y"), z = getConfig().getDouble("Name." + name + ".z");
				World w = Bukkit.getWorld(getConfig().getString("Name." + name + ".world"));
				Location loc = new Location(w, x, y, z, yaw, pitch);
				back(player);
				player.teleport(loc);
				return true;
			} else if (!(arg.equalsIgnoreCase(key))) {
				player.sendMessage("Warp Doesn't Exist: " + arg);
			}
		}

		return false;

	}

	private void createPublicWarp(Player player, String name) {

		String message = "";
		List<String> messageList = Arrays.asList("I will alart the masses", "They will be ariving soon", "People will be ariving soon", name + " is a good name", "Really " + name + ", do you think people will remember that", "Really " + name + " doesn't make sense for this place I would have called it something way cooler");
		int rand = new Random().nextInt(messageList.size() - 1);
		message = messageList.get(rand);
		getConfig().set("Name." + name + ".world", player.getLocation().getWorld().getName());
		getConfig().set("Name." + name + ".x", player.getLocation().getX());
		getConfig().set("Name." + name + ".y", player.getLocation().getY());
		getConfig().set("Name." + name + ".z", player.getLocation().getZ());
		getConfig().set("Name." + name + ".yaw", player.getLocation().getYaw());
		getConfig().set("Name." + name + ".pitch", player.getLocation().getPitch());
		player.sendMessage(ChatColor.GREEN + "Warp Saved as: " + ChatColor.GOLD + name);
		player.sendMessage(ChatColor.GOLD + message + "......");
	}

	public void createPrivateWarp(Player player, String name) {
		String message = "";
		List<String> messageList = Arrays.asList("This will be our little secret", "Sex Dungon Warp Created", "Private Warp Saved as: Sex Dungon");
		int rand = new Random().nextInt(messageList.size() - 1);
		message = messageList.get(rand);
		getConfig().set(player.getDisplayName() + "." + name + ".world", player.getLocation().getWorld().getName());
		getConfig().set(player.getDisplayName() + "." + name + ".x", player.getLocation().getX());
		getConfig().set(player.getDisplayName() + "." + name + ".y", player.getLocation().getY());
		getConfig().set(player.getDisplayName() + "." + name + ".z", player.getLocation().getZ());
		getConfig().set(player.getDisplayName() + "." + name + ".yaw", player.getLocation().getYaw());
		getConfig().set(player.getDisplayName() + "." + name + ".pitch", player.getLocation().getPitch());
		if (!message.contains("Sex Dungon"))
			player.sendMessage(ChatColor.GREEN + "Private Warp Saved as: " + ChatColor.GOLD + name);
		player.sendMessage(ChatColor.GOLD + message + "......");
	}

	public void back(Player player) {
		player.sendMessage(ChatColor.GREEN + "Back point saved you can type " + ChatColor.GOLD + ChatColor.BOLD + "\"/warp back\"" + ChatColor.GREEN + ChatColor.RESET + " to teleport you back to where you warped from");
		getConfig().set(player.getDisplayName() + ".back.world", player.getLocation().getWorld().getName());
		getConfig().set(player.getDisplayName() + ".back.x", player.getLocation().getX());
		getConfig().set(player.getDisplayName() + ".back.y", player.getLocation().getY());
		getConfig().set(player.getDisplayName() + ".back.z", player.getLocation().getZ());
		getConfig().set(player.getDisplayName() + ".back.yaw", player.getLocation().getYaw());
		getConfig().set(player.getDisplayName() + ".back.pitch", player.getLocation().getPitch());
		saveConfig();
	}

	public List<String> getAllWarps(Player player) {
		List<String> f = new ArrayList<String>();
		try {
			f.addAll(getConfig().getConfigurationSection("Name").getKeys(false));
		} catch (Exception e) {
			f.add("");
		}
		try {
			f.addAll(getConfig().getConfigurationSection(player.getDisplayName()).getKeys(false));
		} catch (Exception e) {
			f.add("");
		}
		return f;
	}

	public List<String> getPrivateWarps(Player player) {
		// Player player = (Player) sender;
		List<String> f = new ArrayList<String>();
		try {
			f.addAll(getConfig().getConfigurationSection(player.getDisplayName()).getKeys(false));
		} catch (Exception e) {
			f.add("You don't have any private warps");
		}
		return f;
	}

	public List<String> getPublicWarps() {
		List<String> f = new ArrayList<String>();
		try {
			f.addAll(getConfig().getConfigurationSection("Name").getKeys(false));
		} catch (Exception e) {
			f.add("There are no Public Warps to speak of");
		}
		return f;
	}

}
