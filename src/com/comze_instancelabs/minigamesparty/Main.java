package com.comze_instancelabs.minigamesparty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.BlockIterator;

import com.comze_instancelabs.minigamesparty.minigames.ChickenTag;
import com.comze_instancelabs.minigamesparty.minigames.ColorMatch;
import com.comze_instancelabs.minigamesparty.minigames.DeadEnd;
import com.comze_instancelabs.minigamesparty.minigames.DisIntegration;
import com.comze_instancelabs.minigamesparty.minigames.JumpnRun;
import com.comze_instancelabs.minigamesparty.minigames.LastArcherStanding;
import com.comze_instancelabs.minigamesparty.minigames.MineField;
import com.comze_instancelabs.minigamesparty.minigames.SheepFreenzy;
import com.comze_instancelabs.minigamesparty.minigames.SlapFight;
import com.comze_instancelabs.minigamesparty.minigames.SmokeMonster;
import com.comze_instancelabs.minigamesparty.minigames.Spleef;
import com.comze_instancelabs.minigamesparty.nms.NMSEffectManager;
import com.comze_instancelabs.minigamesparty.sql.MainSQL;

public class Main extends JavaPlugin implements Listener {

	/* setup pattern:
	 * 
	 * # - - -
	 * # - - -
	 * # - - -
	 * # # # #
	 * 
	 * #=minigame
	 * 
	 * IMPORTANT: LOBBY SPAWN MUST BE ABOVE SPAWNS
	 */

	/*
	 * SETUP
	 * 
	 * 1. build main lobby
	 * 2. /mp setlobby
	 * 3. go to location somewhere UNDER lobby
	 * 4. /mp setup
	 * 
	 */

	public static Economy econ = null;

	public ArrayList<Minigame> minigames = new ArrayList<Minigame>();
	public ArrayList<String> players = new ArrayList<String>();
	public ArrayList<String> players_doublejumped = new ArrayList<String>();
	public ArrayList<String> players_outgame = new ArrayList<String>();
	public ArrayList<String> players_left = new ArrayList<String>();
	public HashMap<String, ItemStack[]> pinv = new HashMap<String, ItemStack[]>();
	public static HashMap<String, Boolean> hasChicken = new HashMap<String, Boolean>();

	public int min_players = 1;
	public boolean running = false;

	public int minreward = 0;
	public int maxreward = 0;
	public int item_minreward = 0;
	public int item_maxreward = 0;
	
	public int item_id = 264;
	
	boolean economy = true;
	boolean item_rewards = true;
	
	public Location mainlobby = null;

	public int seconds = 60;
	
	Main m;
	MainSQL msql;

	@Override
	public void onEnable(){
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		m = this;
		msql = new MainSQL(this);
		int id = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run(){
				if(isValidMinigame("ColorMatch") && isValidMinigame("Spleef") && isValidMinigame("MineField") && isValidMinigame("JumpnRun") && isValidMinigame("DeadEnd")){
					ColorMatch cm = new ColorMatch(m, m.getComponentForMinigame("ColorMatch", "spawn"), m.getLobby(), m.getComponentForMinigame("ColorMatch", "spectatorlobby"));
					minigames.add(cm);
					getServer().getPluginManager().registerEvents(cm, m);
					Spleef sp = new Spleef(m, m.getComponentForMinigame("Spleef", "spawn"), m.getLobby(), m.getComponentForMinigame("Spleef", "spectatorlobby"));
					minigames.add(sp);
					getServer().getPluginManager().registerEvents(sp, m);
					MineField mf = new MineField(m, m.getComponentForMinigame("MineField", "spawn"), m.getLobby(), m.getComponentForMinigame("MineField", "spectatorlobby"), m.getComponentForMinigame("MineField", "finishline"));
					minigames.add(mf);
					getServer().getPluginManager().registerEvents(mf, m);
					JumpnRun jr = new JumpnRun(m, m.getComponentForMinigame("JumpnRun", "spawn"), m.getLobby(), m.getComponentForMinigame("JumpnRun", "spectatorlobby"), m.getComponentForMinigame("JumpnRun", "finishline"));
					minigames.add(jr);
					getServer().getPluginManager().registerEvents(jr, m);
					DeadEnd de = new DeadEnd(m, m.getComponentForMinigame("DeadEnd", "spawn"), m.getLobby(), m.getComponentForMinigame("DeadEnd", "spectatorlobby"));
					minigames.add(de);
					getServer().getPluginManager().registerEvents(de, m);
					DisIntegration di = new DisIntegration(m, m.getComponentForMinigame("DisIntegration", "spawn"), m.getLobby(), m.getComponentForMinigame("DisIntegration", "spectatorlobby"));
					minigames.add(di);
					getServer().getPluginManager().registerEvents(di, m);
					LastArcherStanding las = new LastArcherStanding(m, m.getComponentForMinigame("LastArcherStanding", "spawn"), m.getLobby(), m.getComponentForMinigame("LastArcherStanding", "spectatorlobby"));
					minigames.add(las);
					getServer().getPluginManager().registerEvents(las, m);
					SheepFreenzy sf = new SheepFreenzy(m, m.getComponentForMinigame("SheepFreenzy", "spawn"), m.getLobby(), m.getComponentForMinigame("SheepFreenzy", "spectatorlobby"));
					minigames.add(sf);
					getServer().getPluginManager().registerEvents(sf, m);
					SmokeMonster sm = new SmokeMonster(m, m.getComponentForMinigame("SmokeMonster", "spawn"), m.getLobby(), m.getComponentForMinigame("SmokeMonster", "spectatorlobby"));
					minigames.add(sm);
					getServer().getPluginManager().registerEvents(sm, m);
					SlapFight slf = new SlapFight(m, m.getComponentForMinigame("SlapFight", "spawn"), m.getLobby(), m.getComponentForMinigame("SlapFight", "spectatorlobby"));
					minigames.add(slf);
					getServer().getPluginManager().registerEvents(slf, m);
					ChickenTag ct = new ChickenTag(m, m.getComponentForMinigame("ChickenTag", "spawn"), m.getLobby(), m.getComponentForMinigame("ChickenTag", "spectatorlobby"));
					minigames.add(ct);
					getServer().getPluginManager().registerEvents(ct, m);
				}
			}
		}, 20);

		getConfig().options().header("I recommend you to set auto_updating to true for possible future bugfixes.");
		
		// I'm running on windows, just making sure for Linux users:
		getConfig().addDefault("mysql.enabled", false);
		getConfig().addDefault("mysql.host", "127.0.0.1");
		getConfig().addDefault("mysql.database", "bukkit");
		getConfig().addDefault("mysql.user", "root");
		getConfig().addDefault("mysql.pw", "toor");
		getConfig().addDefault("config.auto_updating", true);
		getConfig().addDefault("config.min_players", 1);
		getConfig().addDefault("config.max_players", 50);
		getConfig().addDefault("config.game-on-join", false);
		getConfig().addDefault("config.max_reward", 30);
		getConfig().addDefault("config.min_reward", 10);
		getConfig().addDefault("config.use_economy", false);
		getConfig().addDefault("config.use_item_rewards", false);
		getConfig().addDefault("config.item_reward_maxamount", 10);
		getConfig().addDefault("config.item_reward_minamount", 3);
		getConfig().addDefault("config.item_reward_id", 264);
		getConfig().addDefault("config.scoreboardoutgame", true);
		getConfig().addDefault("config.announcements", true);
		getConfig().addDefault("config.seconds_for_each_minigame", 60); // ô.ô
		
		getConfig().addDefault("strings.you_left", "You left the game.");
		getConfig().addDefault("strings.next_round_30_seconds", "Next round in 30 seconds! You can leave with /mp leave.");
		
		getConfig().addDefault("strings.description.colormatch", "Jump to the color corresponding to the wool color in your inventory!");
		getConfig().addDefault("strings.description.deadend", "Don't fall while the blocks are disappearing behind you!");
		getConfig().addDefault("strings.description.disintegration", "Don't fall while the floor is disappearing!");
		getConfig().addDefault("strings.description.jumpnrun", "Jump to the finish!");
		getConfig().addDefault("strings.description.lastarcherstanding", "Shoot the others with the bow!");
		getConfig().addDefault("strings.description.minefield", "Run to the finish without touching the mines!");
		getConfig().addDefault("strings.description.sheepfreenzy", "Shear as many Sheeps as possible! Attention: Some of them explode.");
		getConfig().addDefault("strings.description.smokemonster", "Avoid the smoke monster!");
		getConfig().addDefault("strings.description.spleef", "Destroy the floor under your opponents to make them fall and lose!");
		getConfig().addDefault("strings.description.slapfight", "Slap the other players to fall! You can use Double Jump in case you fall, too.");
		getConfig().addDefault("strings.description.chickentag", "Pass the chicken to others or you'll lose!");

		getConfig().addDefault("strings.your_place", "You are <place> place.");

		Shop.initShop(this);
		
		getConfig().options().copyDefaults(true);
		this.saveConfig();
		
		Shop.loadPrices(this);

		min_players = getConfig().getInt("config.min_players");

		minreward = getConfig().getInt("config.min_reward");
		maxreward = getConfig().getInt("config.max_reward");
		item_minreward = getConfig().getInt("config.item_reward_minamount");
		item_maxreward = getConfig().getInt("config.item_reward_maxamount");

		item_id = getConfig().getInt("config.item_reward_id"); 
		seconds = getConfig().getInt("config.seconds_for_each_minigame");
		
		if(minreward > maxreward){
			int temp = maxreward;
			maxreward = minreward;
			minreward = temp;
		}
		
		if(item_minreward > item_maxreward){
			int temp = item_maxreward;
			item_maxreward = item_minreward;
			item_minreward = temp;
		}

		economy = getConfig().getBoolean("config.use_economy");
		item_rewards = getConfig().getBoolean("config.use_item_rewards");
		
		try{
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) { }

		if(getConfig().getBoolean("config.auto_updating")){
			Updater updater = new Updater(this, 71596, this.getFile(), Updater.UpdateType.DEFAULT, false);
		}

		if(economy){
			if (!setupEconomy()) {
	            getLogger().severe(String.format("[%s] - No iConomy dependency found! Disabling Economy.", getDescription().getName()));
	            economy = false;
	        }
		}
		
		// let's check which version we're on.
		String version = Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf(".") + 1);
		Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "MinigamesParty is running on " + version + ".");
	}


	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }


	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){    	
		if(cmd.getName().equalsIgnoreCase("minigamesparty") || cmd.getName().equalsIgnoreCase("mp") || cmd.getName().equalsIgnoreCase("mgp")){

			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to run this command.");
				return true;
			}
			final Player p = (Player)sender;

			if(args.length > 0){
				if(args[0].equalsIgnoreCase("setup")){
					// setup all arenas and spawns and lobbies and spectatorlobbies and what not
					if(p.hasPermission("mp.setup")){
						Bukkit.getServer().getScheduler().runTask(this, new Runnable(){
							public void run(){
								setupAll(p.getLocation());
							}
						});
					}
				}else if(args[0].equalsIgnoreCase("setupasync")){
					if(p.hasPermission("mp.setup")){
						Runnable r = new Runnable() {
					        public void run() {
					        	setupAll(p.getLocation());
					        }
					    };
					    new Thread(r).start();
					}
				}else if(args[0].equalsIgnoreCase("setuppoint")){
					if(p.hasPermission("mp.setup")){
						final Location l = this.getComponentForMinigame("ColorMatch", "spawn");
						if(l != null){
							l.add(0.5, -2, 0.5);
							Bukkit.getScheduler().runTaskLater(this, new Runnable(){
								public void run(){
									p.setAllowFlight(true);
									p.setFlying(true);
									p.teleport(l);
								}
							}, 5L);
						}
					}
				}else if(args[0].equalsIgnoreCase("setlobby")){
					if(sender.hasPermission("mp.setlobby")){
						getConfig().set("lobby.world", p.getLocation().getWorld().getName());
						getConfig().set("lobby.location.x", p.getLocation().getBlockX());
						getConfig().set("lobby.location.y", p.getLocation().getBlockY());
						getConfig().set("lobby.location.z", p.getLocation().getBlockZ());
						this.saveConfig();
						p.sendMessage(ChatColor.GREEN + "Saved Main lobby.");	
					}
				}else if(args[0].equalsIgnoreCase("setcomponent")){
					// /mp setcomponent [minigame] [component]
					if(sender.hasPermission("mp.setup")){
						if(args.length > 2){
							this.saveComponentForMinigame(args[1], args[2], p.getLocation());
							p.sendMessage(ChatColor.GREEN + "Saved component");
						}
					}
				}else if(args[0].equalsIgnoreCase("stats")){
					sender.sendMessage(ChatColor.DARK_AQUA + "-- " + ChatColor.GOLD + "Statistics " + ChatColor.DARK_AQUA + "--");
					if(args.length > 1){
						String player = args[1];
						sender.sendMessage(ChatColor.GREEN + player + " has " + Integer.toString(this.getPlayerStats(player, "credits")) + " Credits.");
					}else{
						String player = p.getName();
						sender.sendMessage(ChatColor.GREEN + "You have " + Integer.toString(this.getPlayerStats(player, "credits")) + " Credits.");
					}
				}else if(args[0].equalsIgnoreCase("list")){
					sender.sendMessage(ChatColor.DARK_AQUA + "-- " + ChatColor.GOLD + "Minigames: " + ChatColor.DARK_AQUA + "--");
					for(Minigame m : minigames){
						if(m.isEnabled()){
							sender.sendMessage(ChatColor.GREEN + m.name);
						}else{
							sender.sendMessage(ChatColor.RED + m.name);
						}
					}
				}else if(args[0].equalsIgnoreCase("reloadconfig")){
					this.reloadConfig();
					sender.sendMessage(ChatColor.GREEN + "Successfully reloaded config.");
				}else if(args[0].equalsIgnoreCase("enable")){
					if(args.length > 1){
						if(sender.hasPermission("mp.enable")){
							this.enableMinigame(sender, args[1]);
						}
					}
				}else if(args[0].equalsIgnoreCase("disable")){
					if(args.length > 1){
						if(sender.hasPermission("mp.disable")){
							this.disableMinigame(sender, args[1]);
						}
					}
				}else if(args[0].equalsIgnoreCase("leaderboards")){
					sender.sendMessage(ChatColor.DARK_AQUA + "-- " + ChatColor.GOLD + "Leaderboards: " + ChatColor.DARK_AQUA + "--");
					if(args.length > 1){
						if(args[1].startsWith("credit")){
							outputLeaderboardsByCredits(p);
						}else if(args[1].startsWith("win")){
							outputLeaderboardsByWins(p);
						}else{
							sender.sendMessage(ChatColor.GREEN + "/mp leaderboards [credits/wins].");
						}
					}else{
						outputLeaderboardsByCredits(p);
					}
				}else if(args[0].equalsIgnoreCase("leave")){
					if(players.contains(p.getName())){
						p.teleport(getLobby());
						Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
							public void run(){
								p.teleport(getLobby());
							}
						}, 5);
						updateScoreboardOUTGAME(p.getName());
						p.getInventory().clear();
						p.updateInventory();
						Bukkit.getScheduler().runTaskLater(this, new Runnable(){
							public void run(){
								p.getInventory().setContents(pinv.get(p.getName()));
								p.updateInventory();
							}
						}, 10L);
						if(currentmg > -1){
							minigames.get(currentmg).leave(p);
						}
						players.remove(p.getName());
						p.sendMessage(ChatColor.RED + getConfig().getString("strings.you_left"));
						if(players.size() < min_players){
							Bukkit.getScheduler().runTaskLater(this, new Runnable(){
								public void run(){
									stopFull(p);
								}
							}, 15);
						}
					}
				}else if(args[0].equalsIgnoreCase("join")){
					if(players.contains(p.getName())){
						p.sendMessage(ChatColor.GOLD + "Use /mp leave to leave!");
					}else{
						if(players.size() > getConfig().getInt("config.max_players") - 1){
							p.sendMessage(ChatColor.RED + "You can't join because the minigames party is full!");
							return true;
						}
						players.add(p.getName());
						// if its the first player to join, start the whole minigame
						if(players.size() < min_players + 1){
							pinv.put(p.getName(), p.getInventory().getContents());
							startNew();
							if(min_players > 1){
								p.sendMessage(ChatColor.GREEN + "You joined the queue. There are " + ChatColor.GOLD + Integer.toString(min_players) + ChatColor.GREEN + " players needed to start.");
							}
						}else{ // else: just join the minigame
							try{
								pinv.put(p.getName(), p.getInventory().getContents());
								if(ingame_started){
									minigames.get(currentmg).lost.add(p);
									minigames.get(currentmg).spectate(p);
								}else{
									minigames.get(currentmg).join(p);
								}
							}catch(Exception e){}
							p.sendMessage(ChatColor.GREEN + "You joined the queue. There are " + ChatColor.GOLD + Integer.toString(min_players) + ChatColor.GREEN + " players needed to start.");
						}	
					}
				}else if(args[0].equalsIgnoreCase("shop")){
					Shop.openShop(this, p.getName());
				}else if(args[0].equalsIgnoreCase("skip")){
					if(!sender.hasPermission("mp.skip")){
						return true;
					}
					if(currentmg > -1){
						c_ += seconds-c;
						c = seconds;
					}
					if(args.length > 1){
						String count = args[1];
						currentmg += Integer.parseInt(count) - 1;
						minigames.get(currentmg).join(p);
					}
				}else{
					p.sendMessage(ChatColor.GREEN + "-- " + ChatColor.GOLD + "MinigamesParty Help" + ChatColor.GREEN + " --");
					p.sendMessage(ChatColor.DARK_AQUA + "/mp setlobby");
					p.sendMessage(ChatColor.DARK_AQUA + "/mp setup");
					p.sendMessage(ChatColor.DARK_AQUA + "/mp disable/enable [minigame]");
					p.sendMessage(ChatColor.DARK_AQUA + "/mp stats [player]");
					p.sendMessage(ChatColor.DARK_AQUA + "/mp list");
					p.sendMessage(ChatColor.DARK_AQUA + "/mp leaderboards [wins|credits]");
					p.sendMessage(ChatColor.DARK_AQUA + "/mp leave");
					p.sendMessage(ChatColor.DARK_AQUA + "/mp setcomponent [minigame] [component]");
					p.sendMessage(ChatColor.GOLD + "To setup the game, do the following: ");
					p.sendMessage(ChatColor.DARK_AQUA + "1. Build the main lobby");
					p.sendMessage(ChatColor.DARK_AQUA + "2. /mp setlobby");
					p.sendMessage(ChatColor.DARK_AQUA + "3. Go far away");
					p.sendMessage(ChatColor.DARK_AQUA + "4. /mp setup");
				}
			}else{
				p.sendMessage(ChatColor.GREEN + "-- " + ChatColor.GOLD + "MinigamesParty Help" + ChatColor.GREEN + " --");
				p.sendMessage(ChatColor.DARK_AQUA + "/mp setlobby");
				p.sendMessage(ChatColor.DARK_AQUA + "/mp setup");
				p.sendMessage(ChatColor.DARK_AQUA + "/mp disable/enable [minigame]");
				p.sendMessage(ChatColor.DARK_AQUA + "/mp stats [player]");
				p.sendMessage(ChatColor.DARK_AQUA + "/mp list");
				p.sendMessage(ChatColor.DARK_AQUA + "/mp leaderboards [wins|credits]");
				p.sendMessage(ChatColor.DARK_AQUA + "/mp leave");
				p.sendMessage(ChatColor.DARK_AQUA + "/mp setcomponent [minigame] [component]");
				p.sendMessage(ChatColor.GOLD + "To setup the game, do the following: ");
				p.sendMessage(ChatColor.DARK_AQUA + "1. Build the main lobby");
				p.sendMessage(ChatColor.DARK_AQUA + "2. /mp setlobby");
				p.sendMessage(ChatColor.DARK_AQUA + "3. Go far away");
				p.sendMessage(ChatColor.DARK_AQUA + "4. /mp setup");
			}
			return true;
		}
		return false;
	}


	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event){
		if(players.contains(event.getPlayer().getName())){
			players.remove(event.getPlayer().getName());
			players_left.add(event.getPlayer().getName());
			
			if(players.size() < min_players){
				stopFull();
			}
		}

		if(players.size() < min_players){
			stopFull();
		}
	}

	@EventHandler 
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		
		// update credits from mysql
		try{
			if(msql.getCredits(p.getName()) > -1){
				this.updatePlayerStats(p.getName(), "wins", msql.getWins(p.getName()));
				this.updatePlayerStats(p.getName(), "credits", msql.getCredits(p.getName()));		
			}
		}catch(Exception e){
			getLogger().warning("An error occurred while syncing credits and wins for player " + p.getName());
		}

		if(players_left.contains(p.getName())){
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
				public void run(){
					p.teleport(getLobby());
					p.getInventory().setContents(pinv.get(p.getName()));
					p.updateInventory();
				}
			}, 4);
			players_left.remove(p.getName());
		}

		if (!getConfig().getBoolean("config.game-on-join")) return;

		if(players.contains(event.getPlayer().getName())){
			p.sendMessage(ChatColor.RED + "You are already in the game!");
			return;
		}
		players.add(p.getName());
		event.setJoinMessage(ChatColor.GOLD + p.getName() + " has joined the game!");


		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				
				// if its the first player to join, start the whole minigame
				if(players.size() < min_players + 1){
					pinv.put(p.getName(), p.getInventory().getContents());
					startNew();
					return;
				}
				
				try {
					pinv.put(p.getName(), p.getInventory().getContents());
					if (currentmg > -1) {
						minigames.get(currentmg).join(p);
						p.teleport(minigames.get(currentmg).spawn);
					}
				} catch (Exception ex) {
					p.sendMessage(ChatColor.RED + "An error occured.");
				}
			}
		}, 6);
	}

	@EventHandler
	public void onSignUse(PlayerInteractEvent event)
	{	
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
		{
			if(event.hasBlock()){
				if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
				{
					final Sign s = (Sign) event.getClickedBlock().getState();
					if (s.getLine(1).equalsIgnoreCase(ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + "[PARTY]")){
						if(players.contains(event.getPlayer().getName())){
							event.getPlayer().sendMessage(ChatColor.GOLD + "Use /mp leave to leave!");
						}else{
							if(players.size() > getConfig().getInt("config.max_players") - 1){
								event.getPlayer().sendMessage(ChatColor.RED + "You can't join because the minigames party is full!");
								return;
							}
							players.add(event.getPlayer().getName());
							// if its the first player to join, start the whole minigame
							if(players.size() < min_players + 1){
								pinv.put(event.getPlayer().getName(), event.getPlayer().getInventory().getContents());
								startNew();
								if(min_players > 1){
									event.getPlayer().sendMessage(ChatColor.GREEN + "You joined the queue. There are " + ChatColor.GOLD + Integer.toString(min_players) + ChatColor.GREEN + " players needed to start.");
								}
							}else{ // else: just join the minigame
								try{
									pinv.put(event.getPlayer().getName(), event.getPlayer().getInventory().getContents());
									if(currentmg > -1){
										if(ingame_started){
											minigames.get(currentmg).lost.add(event.getPlayer());
											minigames.get(currentmg).spectate(event.getPlayer());
										}else{
											minigames.get(currentmg).join(event.getPlayer());
										}
									}
								}catch(Exception e){
									event.getPlayer().sendMessage(ChatColor.RED + "An error occured.");
								}
							}
						}
					}
				}	
			}
		}else if(event.getAction().equals(Action.PHYSICAL)){
			if(event.getClickedBlock().getType() == Material.STONE_PLATE){
				if(players.contains(event.getPlayer().getName())){
					final Player p = event.getPlayer();
					NMSEffectManager.createMinefieldEffect(p.getLocation());
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						@Override
						public void run() {
							p.teleport(minigames.get(currentmg).spawn);
						}
					}, 5);
					event.getClickedBlock().setType(Material.AIR);
				}
			}
		}
		
		if(event.hasItem()){
			if(event.getItem() != null){
				if(event.getItem().getItemMeta().getDisplayName() != null){
					if(event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("megagrenade")){
						if(players.contains(event.getPlayer().getName())){
							Egg egg = event.getPlayer().launchProjectile(Egg.class);
							egg.setMetadata("mega", new FixedMetadataValue(m, "mega"));
							event.getPlayer().getInventory().remove(Material.EGG);
							event.getPlayer().updateInventory();
							event.setCancelled(true);
							event.getPlayer().getInventory().addItem(Shop.enchantedItemStack(new ItemStack(Material.EGG, Shop.getPlayerShopComponent(m, event.getPlayer().getName(), "megagrenades") - 1), "MegaGrenade"));
							event.getPlayer().updateInventory();
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player p = event.getPlayer();
		if(event.getLine(0).toLowerCase().contains("[party]") || event.getLine(1).toLowerCase().contains("[party]")){
			if(event.getPlayer().hasPermission("mp.sign")){
				event.setLine(0, "");
				event.setLine(1, ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + "[PARTY]");
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent event){
		try{
			if(players.contains(event.getPlayer().getName())){
				if(event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR){
					if(currentmg > -1 && currentmg < minigames.size()){
						if(m.minigames.get(currentmg).name.equalsIgnoreCase("slapfight")){
							event.getPlayer().setAllowFlight(true);
						}	
					}
				}
				if(currentmg > -1){
					final Minigame current = minigames.get(currentmg);
					if(!current.lost.contains(event.getPlayer())){
						if(started && !ingame_started){
							if(current.name.equalsIgnoreCase("JumpnRun") || current.name.equalsIgnoreCase("MineField")){
								final Player p = event.getPlayer();
								if(p.getLocation().getBlockZ() > current.spawn.getBlockZ() + 1){
									Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
										public void run(){
											p.teleport(current.spawn);
										}
									}, 5);
								}else if(p.getLocation().getBlockY() + 2 < current.spawn.getBlockY()){
									Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
										public void run(){
											p.teleport(current.spawn);
										}
									}, 5);
								}
							}
						}
						if(started && ingame_started){
							if(current.name.equalsIgnoreCase("DeadEnd")){
								World w = event.getPlayer().getWorld();
								Location under = new Location(w, event.getPlayer().getLocation().getBlockX(), event.getPlayer().getLocation().getBlockY() - 1, event.getPlayer().getLocation().getBlockZ());
								if(w.getBlockAt(under).getType() == Material.LAPIS_BLOCK){
									w.getBlockAt(under).setType(Material.AIR);
								}
							}
							if(current.name.equalsIgnoreCase("SmokeMonster")){
								for(Location l : SmokeMonster.locs){
									if(event.getPlayer().getLocation().distance(l) < 3 || event.getPlayer().getLocation().distance(l.add(0D, -1.5D, 0D))  < 3){
										current.lost.add(event.getPlayer());
										int count = 0;
										for(String pl : m.players){
											Player p = Bukkit.getPlayerExact(pl);
											if(p.isOnline()){
												if(!current.lost.contains(p)){
													count++;
												}
											}
										}
										sendPlace(count, event.getPlayer());
										current.spectate(event.getPlayer());
										if(count < 2){
											c_ += seconds-c;
											c = seconds; 
										}
									}
								}
							}
							if(current.name.equalsIgnoreCase("JumpnRun") || current.name.equalsIgnoreCase("MineField")){
								final Player p = event.getPlayer();
								if(p.getLocation().getBlockZ() > current.finish.getBlockZ()){
									c_ += seconds-c;
									c = seconds; // just skips all the remaining seconds and sets to 60, current timer will do the rest
									return;
								}
							}
							if(event.getPlayer().getLocation().getBlockY() + 2 < current.spawn.getBlockY()){
								if(current.name.equalsIgnoreCase("JumpnRun") || current.name.equalsIgnoreCase("MineField")){
									final Player p = event.getPlayer();
									Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
										public void run(){
											p.teleport(current.spawn);
										}
									}, 5);
									return;
								}
								if(current.name.equalsIgnoreCase("slapfight")){
									if(event.getPlayer().getLocation().getBlockY() > current.spawn.getBlockY() - 5){
										return;
									}
								}
								current.lost.add(event.getPlayer());
								int count = 0;
								for(String pl : m.players){
									Player p = Bukkit.getPlayerExact(pl);
									if(p.isOnline()){
										if(!current.lost.contains(p)){
											count++;
										}
									}
								}
								sendPlace(count, event.getPlayer());
								current.spectate(event.getPlayer());
								// there's only one man standing
								if(count < 2){
									c_ += seconds-c;
									c = seconds; // just skips all the remaining seconds and sets to 60, current timer will do the rest
								}
							}
						}
					}else if(current.lost.contains(event.getPlayer())){
						if(started && ingame_started){
							if(event.getPlayer().getLocation().getBlockY() < current.spectatorlobby.getBlockY() || event.getPlayer().getLocation().getBlockY() > current.spectatorlobby.getBlockY()){
								//current.spectate(event.getPlayer());
								final Player p = event.getPlayer();
								final Minigame mg = current;
								final float b = p.getLocation().getYaw();
								final float c = p.getLocation().getPitch();
								Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
									@Override
									public void run() {
										p.setAllowFlight(true);
										p.setFlying(true);
										p.teleport(new Location(p.getWorld(), p.getLocation().getBlockX(), mg.spectatorlobby.getBlockY(), p.getLocation().getBlockZ(), b, c));
										//p.getLocation().setYaw(b);
										//p.getLocation().setPitch(c);
									}
								}, 5);
							}
						}
					}	
				}

			}	
		}catch(Exception e){
			for(StackTraceElement et : e.getStackTrace()){
				System.out.println(et);
			}
		}

	}
	


	@EventHandler
	public void onEntityDamage(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			final Player p = (Player)event.getEntity();
			if(players.contains(p.getName())){
				if(currentmg > -1 && currentmg < minigames.size()){
					if(minigames.get(currentmg).name.equalsIgnoreCase("slapfight")){
						// current minigame is SlapFight, enable all damage (except for fall damage)
						if(ingame_started){
							// enable damage only when cooldown finished
							if(event.getCause() == DamageCause.FALL){
								event.setCancelled(true);
							}
							p.setHealth(20D);
						}else{
							event.setCancelled(true);
						}
					}else if(minigames.get(currentmg).name.equalsIgnoreCase("chickentag")){
						// current minigame is chickentag, enable all damage again
						if(ingame_started){
							// enable damage only when cooldown finished
							if(event.getCause() == DamageCause.FALL){
								event.setCancelled(true);
							}
							// TODO pass the chicken
							if(hasChicken.containsKey(p.getName())){
								if(hasChicken.get(p.getName())){
									// if the player already has the chicken, don't allow passing to him
									return;
								}	
							}
							if(event instanceof EntityDamageByEntityEvent){
								EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
								if(e.getDamager() instanceof Player){
									Player p2 = (Player) e.getDamager();
									if(hasChicken.containsKey(p2.getName())){
										hasChicken.put(p2.getName(), false);
										Entity t = p2.getPassenger();
										p2.eject();
										t.remove();
									}
									NMSEffectManager.createBloodEffect(m, p.getLocation().add(0, 0.5, 0));
									p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + p2.getName() + " passed his Chicken to you! Try to get rid of it!");
								}
							}
							hasChicken.put(p.getName(), true);
							final Chicken c = (Chicken) p.getWorld().spawnEntity(p.getLocation(), EntityType.CHICKEN);
							Bukkit.getScheduler().runTaskLater(m, new Runnable(){
								public void run(){
									p.setPassenger(c);
								}
							}, 2L);
							p.setHealth(20D);
						}else{
							event.setCancelled(true);
						}
					}else{
						event.setCancelled(true);
					}
				}
			}
		}
		
		// last archer standing
		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event_ = (EntityDamageByEntityEvent) event;
			if (event_.getDamager() instanceof Arrow) {
				final Arrow arrow = (Arrow) event_.getDamager();
				if (arrow.getShooter() instanceof Player && event.getEntity() instanceof Player) {
					Player p = (Player) event.getEntity();
					Player damager = (Player) arrow.getShooter();
					if(players.contains(p.getName()) && players.contains(damager.getName())){
						if(currentmg > -1){
							final Minigame current = minigames.get(currentmg);
							
							if(!current.lost.contains(p)){
								if(started && ingame_started){
									current.lost.add(p);
									NMSEffectManager.createBloodEffect(m, p.getLocation().add(0, 0.5, 0));
									int count = 0;
									for(String pl : m.players){
										Player p_ = Bukkit.getPlayerExact(pl);
										if(p_.isOnline()){
											if(!current.lost.contains(p_)){
												count++;
											}
										}
									}
									sendPlace(count, p);
									current.spectate(p);
									// there's only one man standing
									if(count < 2){
										c_ += seconds-c;
										c = seconds; // just skips all the remaining seconds and sets to 60, current timer will do the rest
									}
									
									damager.sendMessage(ChatColor.GOLD + "You shot " + ChatColor.DARK_PURPLE + p.getName() + ChatColor.GOLD + "!");
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onHunger(FoodLevelChangeEvent event){
		if(event.getEntity() instanceof Player){
			Player p = (Player)event.getEntity();
			if(players.contains(p.getName())){
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		if(players.contains(event.getPlayer().getName())){
			//SPLEEF
			if(ingame_started){
				if(event.getBlock().getType() == Material.SNOW_BLOCK){
					event.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL, 2));
					event.getPlayer().updateInventory();
					event.getBlock().setType(Material.AIR);
					event.setCancelled(true);
				}else{
					event.setCancelled(true);
				}
			}else{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		if(players.contains(event.getPlayer().getName())){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event){
		if(players.contains(((Player)event.getWhoClicked()).getName())){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if(players.contains(event.getPlayer().getName())){
			event.getItemDrop().remove();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onProjectileLand(ProjectileHitEvent e) {   
		if (e.getEntity().getShooter() instanceof Player) {
			if (e.getEntity() instanceof Snowball) {
				Player player = (Player) e.getEntity().getShooter();
				if(players.contains(player.getName())){
					BlockIterator bi = new BlockIterator(e.getEntity().getWorld(), e.getEntity().getLocation().toVector(), e.getEntity().getVelocity().normalize(), 0.0D, 4);
					Block hit = null;
					while (bi.hasNext()) {
						hit = bi.next();
						if (hit.getTypeId() != 0) {
							break;
						}
					}
					try {
						if (hit.getLocation().getBlockY() < minigames.get(currentmg).spawn.getBlockY() && hit.getType() == Material.SNOW_BLOCK) {
							hit.setTypeId(0);

							player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1F, 1F);
							
						}
					} catch (Exception ex) { 
						
					}
				}
			} else if (e.getEntity() instanceof Egg){
				Player player = (Player) e.getEntity().getShooter();
				if(players.contains(player.getName())){
					BlockIterator bi = new BlockIterator(e.getEntity().getWorld(), e.getEntity().getLocation().toVector(), e.getEntity().getVelocity().normalize(), 0.0D, 4);
					Block hit = null;
					while (bi.hasNext()) {
						hit = bi.next();
						if (hit.getTypeId() != 0) {
							break;
						}
					}
					try {
						Location l = hit.getLocation();
						if (hit.getLocation().getBlockY() < minigames.get(currentmg).spawn.getBlockY() && hit.getType() == Material.SNOW_BLOCK) {
							if(e.getEntity().hasMetadata("mega")){
								for(int x = 1; x <= 5; x++){
									for(int z = 1; z <= 5; z++){
										Block b = l.getWorld().getBlockAt(new Location(l.getWorld(), l.getBlockX() + x - 3, l.getBlockY(), l.getBlockZ() + z - 3));
										b.setTypeId(0);
									}
								}
								Shop.removeFromPlayerShopComponent(m, player.getName(), "megagrenades", 1);
							}else{
								for(int x = 1; x <= 3; x++){
									for(int z = 1; z <= 3; z++){
										Block b = l.getWorld().getBlockAt(new Location(l.getWorld(), l.getBlockX() + x - 2, l.getBlockY(), l.getBlockZ() + z - 2));
										b.setTypeId(0);
									}
								}
								Shop.removeFromPlayerShopComponent(m, player.getName(), "grenades", 1);
							}
							hit.setTypeId(0);

							player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1F, 1F);
							for (Player sp : players) {
	                            sp.getPlayer().playEffect(new Location(hit.getWorld(), hit.getLocation().getBlockX(), hit.getLocation().getBlockY() + 1.0D, hit.getLocation().getBlockZ()), Effect.MOBSPAWNER_FLAMES, 25);
	                    	}
						}
					} catch (Exception ex) { 
						
					}
				}
			}
		}
	}
	

	Random rand = new Random();
	@EventHandler
	public void onPlayerShearSheep(PlayerShearEntityEvent event){
		if(players.contains(event.getPlayer().getName())){
			int i = rand.nextInt(100);
			if(!currentscore.containsKey(event.getPlayer().getName())){
				currentscore.put(event.getPlayer().getName(), 0);
			}
			if(i < 5){
				currentscore.put(event.getPlayer().getName(), currentscore.get(event.getPlayer().getName()) + 3);
				int temp = Shop.getPlayerShopComponent(m, event.getPlayer().getName(), "sheepfreenzy_explosion_immunity");
				if(temp < 1){
					Shop.removeFromPlayerShopComponent(m, event.getPlayer().getName(), "sheepfreenzy_explosion_immunity", 1);
					event.getPlayer().getLocation().getWorld().createExplosion(event.getEntity().getLocation(), 2F);
				}
			}else{
				currentscore.put(event.getPlayer().getName(), currentscore.get(event.getPlayer().getName()) + 1);
			}
			NMSEffectManager.createSheepFreenzyEffect(event.getEntity().getLocation());
			event.getEntity().remove();
		}
	}
	
	
	

	public void nextMinigame(Player p){
		// get current minigame and make winners
		// get new minigame and tp all to the new one
		p.setAllowFlight(false);
		p.setFlying(false);
		minigames.get(currentmg).getWinner();
		if(currentmg < minigames.size() - 1){
			currentmg += 1;
		}else{
			stop(currentid);
		}
		for(Minigame mg : minigames){
			mg.lost.clear();
		}
		minigames.get(currentmg).join(p);
	}

	public void win(Player p){
		if(p == null){
			getLogger().severe("Could not resolve winner: " + Integer.toString(currentmg));
			return;
		}
		p.sendMessage(ChatColor.GOLD + "You won this round!");
		this.updatePlayerStats(p.getName(), "wins", getPlayerStats(p.getName(), "wins") + 1);
		Random r = new Random();
		int reward = r.nextInt((maxreward - minreward) + 1) + minreward;
		if(p.hasPermission("mp.double_coins")){
			reward = reward * 2;
		}else if(p.hasPermission("mp.triple_coins")){
			reward = reward * 3;
		}
		this.updatePlayerStats(p.getName(), "credits", getPlayerStats(p.getName(), "credits") + reward);		

		if(getConfig().getBoolean("config.announcements")){
			getServer().broadcastMessage(ChatColor.GOLD	+ p.getName() + " won this round and earned " + ChatColor.BLUE + Integer.toString(reward) + ChatColor.GOLD + " Credits!");
		}

		p.sendMessage("§aYou earned " + Integer.toString(reward) + " Credits this round.");

		msql.updateWinnerStats(p.getName(), reward);
		
		if(economy){
			EconomyResponse r_ = econ.depositPlayer(p.getName(), reward);
			if(!r_.transactionSuccess()) {
				getServer().getPlayer(p.getName()).sendMessage(ChatColor.RED + String.format("An error occured: %s", r_.errorMessage));
            }
		}
		
		if(item_rewards){
			int reward_ = r.nextInt((item_maxreward - item_minreward) + 1) + item_minreward;
			if(p.hasPermission("mp.double_coins")){
				reward_ = reward_ * 2;
			}else if(p.hasPermission("mp.triple_coins")){
				reward_ = reward_ * 3;
			}
			p.sendMessage("§aYou earned " + Integer.toString(reward_) + " " + Material.getMaterial(item_id).name() + " this round. You'll get them at the end.");
			if(rewardcount.containsKey(p.getName())){
				reward_ += rewardcount.get(p.getName());
			}
			rewardcount.put(p.getName(), reward_);
		}
		
		updateScoreboardOUTGAME(p.getName());
	}
	
	public final HashMap<String, Integer> rewardcount = new HashMap<String, Integer>();
	
	public void giveItemRewards(final Player p, boolean task){
		if(task){
			Bukkit.getScheduler().runTaskLater(this, new Runnable(){
				@Override
				public void run(){
					if(!rewardcount.containsKey(p.getName())){
						return;
					}
					p.getInventory().addItem(new ItemStack(item_id, rewardcount.get(p.getName())));
					p.updateInventory();
					rewardcount.remove(p.getName());
				}
			}, 10L);
		}else{
			if(!rewardcount.containsKey(p.getName())){
				return;
			}
			p.getInventory().addItem(new ItemStack(item_id, rewardcount.get(p.getName())));
			p.updateInventory();
			rewardcount.remove(p.getName());
		}
	}


	/**
	 * NEW TIMER PART
	 */
	public int c = 0; // count
	public int c_ = 0; 
	boolean ingame_started = false;
	boolean started = false;
	BukkitTask t = null;
	public int currentmg = 0;
	BukkitTask currentid = null;
	public void secondsTick(int disabledMinigamesC){
		
		if(!ingame_started){
			return;
		}
		
		// update scoreboard
		updateScoreboard(seconds - c);

		// stop the whole party after some rounds
		if(c_ > (minigames.size() - disabledMinigamesC) * seconds - 3){
			Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable(){
			Bukkit.getScheduler().runTaskLater(this, new Runnable(){
				public void run(){
					startNew();
				}
			}, 30 * 20); // 30 secs
			t.cancel();
			started = false;
			ingame_started = false;

			if(currentid != null){
				currentid.cancel();
			}
			
			
			if(!minigames.get(minigames.size() - 1).name.toLowerCase().equalsIgnoreCase("minefield")){
				minigames.get(minigames.size() - 1).reset(this.getComponentForMinigame(minigames.get(minigames.size() - 1).name, "spawn"));
			}else{
				Location t = this.getComponentForMinigame("MineField", "spawn");
				minigames.get(minigames.size() - 1).reset(new Location(t.getWorld(), t.getBlockX(), t.getBlockY(), t.getBlockZ() + 30));
			}
			
			ArrayList<String> remove = new ArrayList<String>();
			for(String pl : players){
				Player p = Bukkit.getPlayerExact(pl);
				if(p.isOnline()){
					minigames.get(minigames.size() - 1).leave(p);
					p.sendMessage(ChatColor.GOLD + getConfig().getString("strings.next_round_30_seconds"));
					p.getInventory().clear();
					p.updateInventory();
					updateScoreboardOUTGAME(pl);
				}else{
					remove.add(p.getName());
				}
			}

			// removes players that aren't online anymore
			for(String p : remove){
				players.remove(p);
			}

			remove.clear();

			currentmg = -1;
			currentid = null;

			// reset all:
			Bukkit.getScheduler().runTaskLater(this, new Runnable(){
				public void run(){
					resetAll(false);
				}
			}, 20L);
			
			c = 0;
			c_ = 0;
			if(currentid != null){
				currentid.cancel();
			}
		}

		// start the next minigame after 60 seconds
		if(c == seconds || c > seconds){
			c = 0;
			if(currentid != null){
				currentid.cancel();
			}
			currentid = nextMinigame();
			nextMinigame();
		}


		c += 1;
		c_ += 1;
	}

	
	public BukkitTask nextMinigame(){
	public void nextMinigame(){
		
		ingame_started = false;
		Minigame cmg = null;
		
		if(currentmg > -1){
			cmg = minigames.get(currentmg);
			System.out.println(currentmg + " " + cmg.isEnabled());
		}
		
		// check disabled minigames at the end
		int count = 0;
		for(int i = minigames.size() - 1; i > -1; i--){
			if(!minigames.get(i).isEnabled()){
				count++;
			}else{
				break;
			}
		}
		
		if(currentmg < minigames.size() - 1 - count){
			currentmg += 1;
		}else{
			if(currentid != null){
				stop(currentid);
				if(minigames.get(minigames.size() -1).name.equalsIgnoreCase("minefield")){
					Location t = this.getComponentForMinigame("MineField", "spawn");
					minigames.get(minigames.size() - 1).reset(new Location(t.getWorld(), t.getBlockX(), t.getBlockY(), t.getBlockZ() + 30));
				}
				minigames.get(minigames.size() - 1).reset(this.getComponentForMinigame(minigames.get(minigames.size() - 1).name, "spawn"));
				return;
			}
		}
		if(cmg != null){
			
			// reset current minigame
			final Minigame cmg_ = cmg;
			if(!cmg.name.toLowerCase().equalsIgnoreCase("minefield")){
				Bukkit.getScheduler().runTaskLater(this, new Runnable(){
					public void run(){
						cmg_.reset(m.getComponentForMinigame(cmg_.name, "spawn"));
					}
				}, 20L);
			}else{
				Location t = this.getComponentForMinigame("MineField", "spawn");
				cmg.reset(new Location(t.getWorld(), t.getBlockX(), t.getBlockY(), t.getBlockZ() + 30));
			}
			
			if(cmg.isEnabled()){
				cmg.getWinner();
			}
			
			if(currentmg > -1){
				if(!minigames.get(currentmg).isEnabled()){
					currentscore.clear();
					for(Minigame mg : minigames){
						mg.lost.clear();
					}
					System.out.println(c_ + " " + (minigames.size() - disabledMinigamesCount) * seconds);
					c_ += seconds;
					System.out.println("Turned to " + c_ + " " + (minigames.size() - disabledMinigamesCount) * seconds);
					nextMinigame();
					return;
				}
			}
		}
		currentscore.clear();
		for(Minigame mg : minigames){
			mg.lost.clear();
		}

		System.out.println(currentmg);
		
		if(currentmg > -1){
			if(!minigames.get(currentmg).isEnabled()){
				currentscore.clear();
				for(Minigame mg : minigames){
					mg.lost.clear();
				}
				System.out.println(c_ + " " + (minigames.size() - disabledMinigamesCount) * seconds);
				c_ += seconds;
				System.out.println("Turned to " + c_ + " " + (minigames.size() - disabledMinigamesCount) * seconds);
				nextMinigame();
				return;
			}
		}
		
		for(String pl : players){
			final Player p = Bukkit.getPlayerExact(pl);
			if(p.isOnline()){
				p.setAllowFlight(false);
				p.setFlying(false);
				p.getInventory().clear();

				if(minigames.size() < 1){ // that looks like bs, I should fix that
					if(currentmg < minigames.size() - 1){
						currentmg += 1;
					}
					minigames.get(currentmg).join(p);
				}else{
					minigames.get(currentmg).join(p);
				}
			}
		}


		
		if(currentmg > -1 && currentmg < minigames.size()){
			return minigames.get(currentmg).start();
			minigames.get(currentmg).startCooldown();
		}else{
			return null;
		}
	}
	
	public void registerMinigameStart(BukkitTask minigame){
		currentid = minigame;
	}

	int disabledMinigamesCount = 0;
	public void startNew(){
		if(!started && !ingame_started){
			if(players.size() > min_players - 1){
				// reset all
				for(Minigame m : minigames){
					m.lost.clear();
				}
				currentmg = -1;
				currentid = null;

				// randomize minigames order
				this.shuffleMinigames();
				
				// start first minigame
				//currentid = nextMinigame();
				nextMinigame();
				
				// calculate the amount of disabled minigames
				disabledMinigamesCount = 0;
				for(Minigame m : minigames){
					if(!m.isEnabled()){
						disabledMinigamesCount++;
					}
				}
				
				// start main timer
				t = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable(){
				t = Bukkit.getServer().getScheduler().runTaskTimer(this, new Runnable(){
					public void run(){
						secondsTick(disabledMinigamesCount);
					}
				}, 120, 20);

				started = true;
			}	
		}
	}
	/**
	 * NEW TIMER PART
	 */

	public HashMap<String, Integer> currentscore = new HashMap<String, Integer>();

	public void updateScoreboard(int c){

		ScoreboardManager manager = Bukkit.getScoreboardManager();

		boolean isNeeded = false;
		boolean isSheep = false;
		
		if(minigames.get(currentmg).name.equalsIgnoreCase("minefield") || minigames.get(currentmg).name.equalsIgnoreCase("jumpnrun")){
			isNeeded = true;
		}

		if(minigames.get(currentmg).name.equalsIgnoreCase("sheepfreenzy")){
			isNeeded = true;
			isSheep = true;
		}
		
		for(String pl : players){
			Player p = Bukkit.getPlayerExact(pl);
			Scoreboard board = manager.getNewScoreboard();

			Objective objective = board.registerNewObjective("test", "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);

			objective.setDisplayName("[" + Integer.toString(currentmg + 1) + "/" + Integer.toString(minigames.size()) + "] [" + Integer.toString(c) + "]");

			for(String pl_ : players){
				Player p_ = Bukkit.getPlayerExact(pl_);
				if(isNeeded){
					int score = 0;
					if(!isSheep){
						score = p_.getLocation().getBlockZ() - minigames.get(currentmg).finish.getBlockZ();
						if(currentscore.containsKey(pl_)){
							int oldscore = currentscore.get(pl_);
							if(score > oldscore){
								currentscore.put(pl_, score);
							}else{
								score = oldscore;
							}
						}else{
							currentscore.put(pl_, score);
						}
						if(currentmg > -1 && currentmg < minigames.size()){
							if(minigames.get(currentmg).lost.contains(p_)){
								String tempn = ChatColor.RED + pl_;
								if(p_.getName().length() > 14){
									tempn = ChatColor.RED + pl_.substring(0, pl_.length() - 3);
								}
								objective.getScore(Bukkit.getOfflinePlayer(tempn)).setScore(score);
							}else{
								String tempn = ChatColor.GREEN + pl_;
								if(p_.getName().length() > 14){
									tempn = ChatColor.GREEN + pl_.substring(0, pl_.length() - 3);
								}
								objective.getScore(Bukkit.getOfflinePlayer(tempn)).setScore(score);
							}
						}else{
							objective.getScore(p_).setScore(score);
						}
					}else{
						if(!currentscore.containsKey(pl_)){
							currentscore.put(pl_, 0);
						}
						objective.getScore(p_).setScore(currentscore.get(pl_));
					}
				}else{
					if(currentmg > -1 && currentmg < minigames.size()){
						if(minigames.get(currentmg).lost.contains(p_)){
							String tempn = ChatColor.RED + pl_;
							if(p_.getName().length() > 14){
								tempn = ChatColor.RED + pl_.substring(0, pl_.length() - 3);
							}
							objective.getScore(Bukkit.getOfflinePlayer(tempn)).setScore(0);
						}else{
							String tempn = ChatColor.GREEN + pl_;
							if(p_.getName().length() > 14){
								tempn = ChatColor.GREEN + pl_.substring(0, pl_.length() - 3);
							}
							objective.getScore(Bukkit.getOfflinePlayer(tempn)).setScore(0);
						}
					}else{
						objective.getScore(p_).setScore(0);
					}
				}
			}

			p.setScoreboard(board);
		}
	}


	public void updateScoreboardOUTGAME(final String player){
		
		if(!getConfig().getBoolean("config.scoreboardoutgame")){
			return;
		}
		
		ScoreboardManager manager = Bukkit.getScoreboardManager();

		final Player p = Bukkit.getPlayer(player);

		Scoreboard board = manager.getNewScoreboard();

		Objective objective = board.registerNewObjective("minigamesparty", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		objective.setDisplayName(ChatColor.GOLD + "MinigamesParty!");

		objective.getScore(Bukkit.getOfflinePlayer("Credits")).setScore(this.getPlayerStats(player, "credits"));

		p.setScoreboard(board);

		Runnable r = new Runnable() {
	        public void run() {
	        	m.removeScoreboard(p);
	        }
	    };
	    
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, r, 20 * 10);

	}

	public void removeScoreboard(Player p) {
		try {
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard sc = manager.getNewScoreboard();
			
			sc.clearSlot(DisplaySlot.SIDEBAR);
			p.setScoreboard(sc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start(){
		// if not running -> start
		// else just join current game
		//    if no current game, join into waiting lobby

		if(players.size() > min_players - 1){
			// reset all
			for(Minigame m : minigames){
				m.lost.clear();
			}
			currentmg = 0;
			currentid = 0;

			// every player joins again (or maybe first time)
			for(Player p : players){
				minigames.get(0).join(p);
			}
			final int stopid = minigames.get(0).start();
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run(){
					Bukkit.getServer().getScheduler().cancelTask(stopid);
				}
			}, 1200);

			// main running timer
			if(!running){
				final int id__ = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
					@Override
					public void run(){
						int count = 0;
						ArrayList<Player> remove = new ArrayList<Player>();
						for(Player p : players){
							if(p.isOnline()){
								nextMinigame(p);
								count += 1;
							}else{
								remove.add(p);
							}
						}

						for(Player p : remove){
							players.remove(p);
						}

						remove.clear();

						if(count < min_players){ // one player left
							stopFull();
						}
					}
				}, 1200, 1200); // each 60 seconds -> change minigame	

				currentid = id__;

				int id = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					@Override
					public void run(){
						start();
					}
				}, minigames.size() * 1200 + 20 * 30); // 20 * 30: wait 30 seconds after all games	

				int id_ = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					@Override
					public void run(){
						stop(id__);
					}
				}, minigames.size() * 1200 - 40);

				running = true;
			}
		}
	}

	public void stop(BukkitTask id){
		id.cancel();
		running = false;

		for(Minigame mg : minigames){
			mg.lost.clear();
		}
		players_doublejumped.clear();
		
		ArrayList<Player> remove = new ArrayList<Player>();
		for(String pl : players){
			Player p = Bukkit.getPlayerExact(pl);
			if(p.isOnline()){
				for (PotionEffect effect : p.getActivePotionEffects()) {
					if(p.hasPotionEffect(effect.getType())){
						try {
							p.removePotionEffect(effect.getType());
						} catch (Exception e) {
							
						}
					}
				}
				if(p.hasPotionEffect(PotionEffectType.JUMP)){
					p.removePotionEffect(PotionEffectType.JUMP);
				}
				try{
					minigames.get(minigames.size() - 1).leave(p);
				}catch(Exception e){
					if(currentmg > -1 && currentmg < minigames.size()){
						minigames.get(currentmg).leave(p);
					}
				}
				
				p.sendMessage(ChatColor.GOLD + getConfig().getString("strings.next_round_30_seconds"));
				p.getInventory().clear();
				p.updateInventory();
			}else{
				remove.add(p);
			}
		}

		// removes players that aren't online anymore
		for(Player p : remove){
			players.remove(p.getName());
		}

		remove.clear();

		currentmg = -1;

		Bukkit.getScheduler().runTask(this, new Runnable(){
			public void run(){
				resetAll(false);
			}
		});

	}

	public void stopFull(){
		Bukkit.getServer().getScheduler().cancelTasks(this);
		
		for(String pl : players){
			final Player p = Bukkit.getPlayerExact(pl);
			if(p.isOnline()){
				p.getInventory().clear();
				p.updateInventory();
				Bukkit.getScheduler().runTaskLater(this, new Runnable(){
					public void run(){
						p.getInventory().setContents(pinv.get(p.getName()));
						p.updateInventory();
					}
				}, 10L);
				
				minigames.get(minigames.size() - 1).leave(p);
				p.sendMessage(ChatColor.RED + "Stopping minigame, because there are less players than required! (" + Integer.toString(min_players) + ")");
			}
		}

		running = false;
		started = false;
		ingame_started = false;
		players.clear();
		players_doublejumped.clear();
		currentmg = 0;

		Bukkit.getScheduler().runTaskLater(this, new Runnable(){
			public void run(){
				resetAll(true);
			}
		}, 20L);
	}
	
	public void stopFull(Player p_){
		stopFull();
		updateScoreboardOUTGAME(p_.getName());
	}

	public Location getLobby(){
		if(!getConfig().isSet("lobby.location")){
			getLogger().severe(ChatColor.BLUE + "A LOBBY COULD NOT BE FOUND. PLEASE FIX THIS WITH /mp setlobby.");
			for(Player p : Bukkit.getOnlinePlayers()){
				if(p.isOp()){
					p.sendMessage(ChatColor.BLUE + "[MinigamesParty] " + ChatColor.RED + "A lobby could NOT be found, which leads to errors in the console. Please fix this with " + ChatColor.GOLD + "/mp setlobby.");
				}
			}
		}
		return new Location(getServer().getWorld(getConfig().getString("lobby.world")), getConfig().getInt("lobby.location.x"), getConfig().getInt("lobby.location.y"), getConfig().getInt("lobby.location.z"));
	}

	public Location getComponentForMinigame(String minigame, String component, String count){
		if(isValidMinigame(minigame)){
			String base = "minigames." + minigame + "." + component + count;
			return new Location(Bukkit.getWorld(getConfig().getString(base + ".world")), getConfig().getInt(base + ".location.x"), getConfig().getInt(base + ".location.y"), getConfig().getInt(base + ".location.z"));
		}
		return null;
	}

	public Location getComponentForMinigame(String minigame, String component){
		if(isValidMinigame(minigame)){
			String base = "minigames." + minigame + "." + component;
			return new Location(Bukkit.getWorld(getConfig().getString(base + ".world")), getConfig().getInt(base + ".location.x"), getConfig().getInt(base + ".location.y"), getConfig().getInt(base + ".location.z"));
		}
		return null;
	}

	public void saveComponentForMinigame(String minigame, String component, Location comploc){
		String base = "minigames." + minigame + "." + component;
		getConfig().set(base + ".world", comploc.getWorld().getName());
		getConfig().set(base + ".location.x", comploc.getBlockX());
		getConfig().set(base + ".location.y", comploc.getBlockY());
		getConfig().set(base + ".location.z", comploc.getBlockZ());
		this.saveConfig();
	}

	public boolean isValidMinigame(String minigame){
		if(getConfig().isSet("minigames." + minigame) && getConfig().isSet("minigames." + minigame + ".lobby") && getConfig().isSet("minigames." + minigame + ".spawn") && getConfig().isSet("minigames." + minigame + ".spectatorlobby")){
			return true;
		}
		return false;
	}

	public void setupAll(Location start){
		int x = start.getBlockX();
		int y = start.getBlockY();
		int z = start.getBlockZ();

		ColorMatch.setup(start, this, "ColorMatch");
		Spleef.setup(new Location(start.getWorld(), x, y, z + 64 + 20), this, "Spleef");
		MineField.setup(new Location(start.getWorld(), x, y, z + 64 * 2 + 20 * 2), this, "MineField");
		JumpnRun.setup(new Location(start.getWorld(), x, y, z + 64 * 3 + 20 * 3), this, "JumpnRun");
		DeadEnd.setup(new Location(start.getWorld(), x + 64 + 20, y, z), this, "DeadEnd");
		DisIntegration.setup(new Location(start.getWorld(), x + 64 * 2 + 20 * 2, y, z), this, "DisIntegration");
		LastArcherStanding.setup(new Location(start.getWorld(), x + 64 * 3 + 20 * 3, y, z), this, "LastArcherStanding");
		SheepFreenzy.setup(new Location(start.getWorld(), x + 64 + 20, y, z + 64 + 20), this, "SheepFreenzy");
		SmokeMonster.setup(new Location(start.getWorld(), x + 64 * 2 + 20 * 2, y, z + 64 + 20), this, "SmokeMonster");
		SlapFight.setup(new Location(start.getWorld(), x + 64 * 3 + 20 * 3, y, z + 64 + 20), this, "SlapFight");
		ChickenTag.setup(new Location(start.getWorld(), x + 64 + 20, y, z + 64 * 2 + 20 * 2), this, "ChickenTag");

		
		/*
		 * next minigame locations: (TODO FOR LATER USE)
		 * 
		 * new Location(start.getWorld(), x, y, z + 64 * 2 + 20 * 2) [MINEFIELD]
		 * new Location(start.getWorld(), x, y, z + 64 * 3 + 20 * 3) [JUMPNRUN]
		 * new Location(start.getWorld(), x + 64 + 20, y, z) [DEADEND]
		 * new Location(start.getWorld(), x + 64 * 2 + 20 * 2, y, z) [DISINTEGRATION]
		 * new Location(start.getWorld(), x + 64 * 3 + 20 * 3, y, z) [LASTARCHERSTANDING]
		 * 
		 * would create the following pattern:
		 * 
		 * # - - -
		 * # - - -
		 * # - - -
		 * # # # #
		 * 
		 * #=minigame
		 * 
		 * IMPORTANT: LOBBY SPAWN MUST BE ABOVE SPAWNS!
		 */ 

		minigames.clear();
		minigames.add(new ColorMatch(this, this.getComponentForMinigame("ColorMatch", "spawn"), this.getComponentForMinigame("ColorMatch", "lobby"), this.getComponentForMinigame("ColorMatch", "spectatorlobby")));
		minigames.add(new Spleef(this, this.getComponentForMinigame("Spleef", "spawn"), this.getComponentForMinigame("Spleef", "lobby"), this.getComponentForMinigame("Spleef", "spectatorlobby")));
		minigames.add(new MineField(this, this.getComponentForMinigame("MineField", "spawn"), this.getComponentForMinigame("MineField", "lobby"), this.getComponentForMinigame("MineField", "spectatorlobby"), m.getComponentForMinigame("MineField", "finishline")));
		minigames.add(new JumpnRun(this, this.getComponentForMinigame("JumpnRun", "spawn"), this.getComponentForMinigame("JumpnRun", "lobby"), this.getComponentForMinigame("JumpnRun", "spectatorlobby"), m.getComponentForMinigame("JumpnRun", "finishline")));
		minigames.add(new DeadEnd(this, this.getComponentForMinigame("DeadEnd", "spawn"), this.getComponentForMinigame("DeadEnd", "lobby"), this.getComponentForMinigame("DeadEnd", "spectatorlobby")));
		minigames.add(new DisIntegration(this, this.getComponentForMinigame("DisIntegration", "spawn"), this.getComponentForMinigame("DisIntegration", "lobby"), this.getComponentForMinigame("DisIntegration", "spectatorlobby")));
		minigames.add(new LastArcherStanding(this, this.getComponentForMinigame("LastArcherStanding", "spawn"), this.getComponentForMinigame("LastArcherStanding", "lobby"), this.getComponentForMinigame("LastArcherStanding", "spectatorlobby")));
		minigames.add(new SheepFreenzy(this, this.getComponentForMinigame("SheepFreenzy", "spawn"), this.getComponentForMinigame("SheepFreenzy", "lobby"), this.getComponentForMinigame("SheepFreenzy", "spectatorlobby")));
		minigames.add(new SmokeMonster(this, this.getComponentForMinigame("SmokeMonster", "spawn"), this.getComponentForMinigame("SmokeMonster", "lobby"), this.getComponentForMinigame("SmokeMonster", "spectatorlobby")));
		minigames.add(new SlapFight(this, this.getComponentForMinigame("SlapFight", "spawn"), this.getComponentForMinigame("SlapFight", "lobby"), this.getComponentForMinigame("SlapFight", "spectatorlobby")));
		minigames.add(new ChickenTag(this, this.getComponentForMinigame("ChickenTag", "spawn"), this.getComponentForMinigame("ChickenTag", "lobby"), this.getComponentForMinigame("ChickenTag", "spectatorlobby")));

		getLogger().info("[MinigamesParty] Finished Setup.");
	}

	public void resetAll(boolean flag){
		getLogger().info("Resetting in ALL mode: " +  Boolean.toString(flag));
		
		if(flag){
			for(Minigame m : minigames){
				if(m.name.toLowerCase().equalsIgnoreCase("minefield")){
					Location t = this.getComponentForMinigame("MineField", "spawn");
					m.reset(new Location(t.getWorld(), t.getBlockX(), t.getBlockY(), t.getBlockZ() + 30));
				}else{
					m.reset(this.getComponentForMinigame(m.name, "spawn"));
				}
				
			}
			ColorMatch.reset(this.getComponentForMinigame("ColorMatch", "spawn"));
			Spleef.reset(this.getComponentForMinigame("Spleef", "spawn"));
			Location t = this.getComponentForMinigame("MineField", "spawn");
			MineField.reset(new Location(t.getWorld(), t.getBlockX(), t.getBlockY(), t.getBlockZ() + 30));
			DeadEnd.reset(this.getComponentForMinigame("DeadEnd", "spawn"));
			DisIntegration.reset(this.getComponentForMinigame("DisIntegration", "spawn"));
		}
	}


	/***
	 * saves player statistics
	 * @param player
	 * @param component component to be updated; can be "wins" or "credits"
	 * @param value value to be saved
	 */
	public void updatePlayerStats(String player, String component, int value){
		getConfig().set(player + "." + component, value);
		this.saveConfig();
	}

	public int getPlayerStats(String player, String component){
		int ret = 0;
		if(getConfig().isSet(player + "." + component)){
			ret = getConfig().getInt(player + "." + component);
		}
		return ret;
	}


	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
		if(players.contains(event.getPlayer().getName())){
			if(event.getMessage().startsWith("/leave") || event.getMessage().equalsIgnoreCase("/quit")){
				final Player p = event.getPlayer();
				p.teleport(getLobby());
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
					public void run(){
						p.teleport(getLobby());
					}
				}, 5);
				updateScoreboardOUTGAME(p.getName());
				p.getInventory().clear();
				p.updateInventory();
				Bukkit.getScheduler().runTaskLater(this, new Runnable(){
					public void run(){
						p.getInventory().setContents(pinv.get(p.getName()));
						p.updateInventory();
					}
				}, 10L);
				if(currentmg > -1){
					minigames.get(currentmg).leave(p);
				}
				players.remove(p.getName());
				p.sendMessage(ChatColor.RED + getConfig().getString("strings.you_left"));
				if(players.size() < min_players){
					Bukkit.getScheduler().runTaskLater(this, new Runnable(){
						public void run(){
							stopFull(p);
						}
					}, 15);
				}
				event.setCancelled(true);
				return;
			}else if(event.getMessage().equalsIgnoreCase("/shop")){
				Shop.openShop(this, event.getPlayer().getName());
				event.setCancelled(true);
			}
			
			if(!event.getPlayer().isOp()){
				if(event.getMessage().startsWith("/mp") || event.getMessage().equalsIgnoreCase("/minigamesparty")){
					// nothing
				}else{
					event.setCancelled(true);
					event.getPlayer().sendMessage("§3You're in MinigamesParty. Please use §6/mp leave §3to leave the minigame.");
				}
			}
		}
	}
	
	
	public void outputLeaderboardsByCredits(Player p){
		HashMap<String,Integer> map = new HashMap<String,Integer>();
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
        
		Set<String> keys = getConfig().getKeys(false);
		keys.remove("mysql");
		keys.remove("config");
		keys.remove("shop");
		keys.remove("minigames");
		keys.remove("lobby");
		keys.remove("strings");
		for(String key : keys){
			map.put(key, getConfig().getInt(key + ".credits"));
		}
		
		sorted_map.putAll(map);
		
		int i = 0;
		
		for(String player : sorted_map.keySet()){
			if(i > 10){
				return;
			}
			i++;
			p.sendMessage(ChatColor.DARK_PURPLE + player + ChatColor.GOLD + " : " + ChatColor.DARK_PURPLE + getConfig().getInt(player + ".credits"));
		}
	}
	
	public void outputLeaderboardsByWins(Player p){
		HashMap<String,Integer> map = new HashMap<String,Integer>();
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
        
		Set<String> keys = getConfig().getKeys(false);
		keys.remove("mysql");
		keys.remove("config");
		keys.remove("shop");
		keys.remove("minigames");
		keys.remove("lobby");
		keys.remove("strings");
		for(String key : keys){
			map.put(key, getConfig().getInt(key + ".wins"));
		}
		
		sorted_map.putAll(map);
		
		int i = 0;
		
		for(String player : sorted_map.keySet()){
			if(i > 10){
				return;
			}
			i++;
			p.sendMessage(ChatColor.DARK_PURPLE + player + ChatColor.GOLD + " : " + ChatColor.DARK_PURPLE + getConfig().getInt(player + ".wins"));
		}
	}
	
	class ValueComparator implements Comparator<String> {

	    Map<String, Integer> base;
	    public ValueComparator(Map<String, Integer> base) {
	        this.base = base;
	    }

	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        }
	    }
	}

	
	
	public void sendPlace(int count, Player p){
		String place = Integer.toString(count + 1) + "th";
		if(count == 0){
			place = Integer.toString(count + 1) + "st";
		}else if(count == 1){
			place = Integer.toString(count + 1) + "nd";
		}else if(count == 2){
			place = Integer.toString(count + 1) + "rd";
		}
		p.sendMessage(ChatColor.BLUE + getConfig().getString("strings.your_place").replaceAll("<place>", place));
	}
	
	
	public void disableMinigame(CommandSender sender, String minigame){
		if(!running){
			for(Minigame mg : minigames){
				if(mg.name.toLowerCase().equalsIgnoreCase(minigame)){
					mg.setEnabled(false);
					sender.sendMessage(ChatColor.RED + "Disabled " + mg.name + ".");
					return;
				}
			}
			sender.sendMessage(ChatColor.RED + "Could not find given Minigame.");
		}else{
			sender.sendMessage(ChatColor.RED + "You cannot change the state of a minigame while a game is running.");
		}
	}
	
	public void enableMinigame(CommandSender sender, String minigame){
		if(!running){
			for(Minigame mg : minigames){
				if(mg.name.toLowerCase().equalsIgnoreCase(minigame)){
					mg.setEnabled(true);
					sender.sendMessage(ChatColor.GREEN + "Enabled " + mg.name + ".");
					return;
				}
			}
			sender.sendMessage(ChatColor.RED + "Could not find given Minigame.");
		}else{
			sender.sendMessage(ChatColor.RED + "You cannot change the state of a minigame while a game is running.");
		}
	}
	
	@EventHandler
	public void onPlayerPortalEnter(EntityPortalEnterEvent event){
		if(event.getEntity() instanceof Player){
			final Player p = (Player) event.getEntity();
			if(players.contains(p.getName())){
				p.getInventory().clear();
				p.updateInventory();
				Bukkit.getScheduler().runTaskLater(this, new Runnable(){
					public void run(){
						p.getInventory().setContents(pinv.get(p.getName()));
						p.updateInventory();
					}
				}, 10L);
				if(currentmg > -1){
					minigames.get(currentmg).leave(p);
				}
				players.remove(p.getName());
				p.sendMessage(ChatColor.RED + getConfig().getString("strings.you_left"));
				if(players.size() < min_players){
					Bukkit.getScheduler().runTaskLater(this, new Runnable(){
						public void run(){
							stopFull(p);
						}
					}, 15);
				}
			}
		}
	}
	
	
	@EventHandler
	public void onFlightAttempt(PlayerToggleFlightEvent event) {
		final Player p = event.getPlayer();
	    if(p.getGameMode() != GameMode.CREATIVE) {
	    	if(currentmg < 0 || currentmg > minigames.size() - 1 || !players.contains(p.getName())){
	    		return;
	    	}
	    	if(players.contains(p.getName()) && m.minigames.get(currentmg).name.equalsIgnoreCase("slapfight")){
	    		if(!players_doublejumped.contains(p.getName())){
		    		event.getPlayer().setAllowFlight(false);
			        event.getPlayer().setFlying(false);
			        event.setCancelled(true);
		    		event.getPlayer().setVelocity(p.getVelocity().setY(1.4F)); // add(new Vector(0,0.7,0)
			        players_doublejumped.add(p.getName());
			        Bukkit.getScheduler().runTaskLater(this, new Runnable(){
			        	public void run(){
			        		players_doublejumped.remove(p.getName());
			        	}
			        }, 20 * 10);
	    		}else{
	    			p.sendMessage(ChatColor.RED + "You can only use Double Jump after a cooldown of 10 seconds.");
	    			p.setAllowFlight(false);
			        p.setFlying(false);
			        event.setCancelled(true);
	    		}
	    	}
	    }
	}
	
	
	public void shuffleMinigames(){
		Collections.shuffle(minigames);
	}
	
	
	
	// Teleportation fix
	// I really think this is nothing important, but whatever, people need it so much
	
	// Thanks to Comphenix and mbaxter
	// Ref: https://forums.bukkit.org/threads/invisible-teleport-bug-really-need-it-fixed.102135/
	
	// Nope nope nope nope sometimes simply crashes
	// will look into that later
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		final Player player = event.getPlayer();
		final int visibleDistance = getServer().getViewDistance() * 16;
		// Fix the visibility issue one tick later
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				// Refresh nearby clients
				final List<Player> nearby = getPlayersWithin(player, visibleDistance);
				// Hide every player
				updateEntities(player, nearby, false);
				// Then show them again
				getServer().getScheduler().scheduleSyncDelayedTask(m, new Runnable() {
					@Override
					public void run() {
						updateEntities(player, nearby, true);
					}
				}, 1);
			}
		}, 15);
	}

	private void updateEntities(Player tpedPlayer, List<Player> players_, boolean visible) {
		for (Player player : players_) {
			if (visible) {
				tpedPlayer.showPlayer(player);
				player.showPlayer(tpedPlayer);
			} else {
				tpedPlayer.hidePlayer(player);
				player.hidePlayer(tpedPlayer);
			}
		}
	}

	private List<Player> getPlayersWithin(Player player, int distance) {
		List<Player> res = new ArrayList<Player>();
		int d2 = distance * distance;
		for (Player p : getServer().getOnlinePlayers()) {
			if (p != player && p.getWorld() == player.getWorld() && p.getLocation().distanceSquared(player.getLocation()) <= d2) {
				res.add(p);
			}
		}
		return res;
	}
}
