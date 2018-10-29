package com.dcman58.warpplugin;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class WorldSpawn implements CommandExecutor {

	FileConfiguration config = Main.plugin.getConfig();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
		if (args.length == 0) {
			Player player = (Player) sender;
			String message = "";
			List<String> messageList = Arrays.asList("Wow this feels fimilar", "So this is what spawn smells like now", "Feels good to be Home", "Honey I'm Home", "I've got a bad feeling about this", "...And right over there I was killed by a creeper");
			int rand = new Random().nextInt(messageList.size() - 1);
			message = messageList.get(rand);
			Main.plugin.back(player);
			player.teleport(player.getWorld().getSpawnLocation());
			player.sendMessage(ChatColor.GREEN + message + "......");
			return true;
		}
		return false;
	}

}
