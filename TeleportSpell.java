import java.util.HashSet;
import java.util.HashMap;

public class TeleportSpell extends Spell {

	private HashSet<String> hiddenPlayers;
	
	// cost values
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private int COOLDOWN;
	
	// strings
	private String STR_ERR_USAGE;
	private String STR_ERR_NO_PLAYER;
	private String STR_ERR_PLAYER_HIDDEN;
	private String STR_CAST;
	private String STR_CAST_OTHERS_LEAVE;
	private String STR_CAST_OTHERS_ARRIVE;
	private String STR_HIDE_ON;
	private String STR_HIDE_OFF;
	
	private int [][] reagents;

	public TeleportSpell(MagicSpellsListener listener, PropertiesFile properties) {
		listener.registerSpellName(properties.getString("teleport-spellname","teleport"),this,properties.getString("teleport-desc","Teleports you to a player"));
		listener.registerSpellName(properties.getString("telehide-spellname","telehide"),this,properties.getString("telehide-desc","Hides from teleporters."));
		
		hiddenPlayers = new HashSet<String>();
		
		// get properties
		REDSTONE_COST = properties.getInt("teleport-redstone-cost",10);
		OTHER_COST = properties.getInt("teleport-other-cost-type",0);
		OTHER_COST_NAME = properties.getString("teleport-other-cost-name","");
		OTHER_COST_AMT = properties.getInt("teleport-other-cost-amt",0);
		
		COOLDOWN = properties.getInt("teleport-cooldown-seconds",300);
		
		STR_ERR_USAGE = properties.getString("teleport-usage-str","You must specify a player to teleport to.");
		STR_ERR_NO_PLAYER = properties.getString("teleport-no-player-str","You cannot find someone by that name.");
		STR_ERR_PLAYER_HIDDEN = properties.getString("teleport-player-hidden-str","Something blocks you from teleporting to that person.");
		STR_CAST = properties.getString("teleport-cast-str","You teleport to [target]'s side!");
		STR_CAST_OTHERS_LEAVE = properties.getString("teleport-cast-others-leave-str","[caster] vanishes!");
		STR_CAST_OTHERS_ARRIVE = properties.getString("teleport-cast-others-arrive-str","[caster] appears!");
		STR_HIDE_ON = properties.getString("telehide-on-str","You hide from teleporters.");
		STR_HIDE_OFF = properties.getString("telehide-off-str","You are no longer hiding from teleporters.");
		
		// setup reagents
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
		
		if (COOLDOWN > 0) {
			enableCooldown();
		}
	}
	
	public boolean cast(Player player, String [] command) {
		if (command[1].equalsIgnoreCase("teleport")) {
			if (command.length != 3) {
				player.sendMessage(TEXT_COLOR + STR_ERR_USAGE);
				return false;
			} else {
				Player target = etc.getServer().matchPlayer(command[2]);
				if (target == null) {
					player.sendMessage(TEXT_COLOR + STR_ERR_NO_PLAYER);
					return false;
				} else if (isOnCooldown(player, COOLDOWN)) {
					player.sendMessage(TEXT_COLOR + STR_ON_COOLDOWN);
					return false;
				} else if (hiddenPlayers.contains(target.getName().toLowerCase())) {
					player.sendMessage(TEXT_COLOR + STR_ERR_PLAYER_HIDDEN);
					return false;
				} else if (!removeReagents(player,new int [][] {{REDSTONE_DUST,REDSTONE_COST}})) {
					player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
					return false;
				} else {
					sendMessageToPlayersInRange(player,player.getName() + " vanishes!");
					player.teleportTo(target);
					sendMessageToPlayersInRange(player,player.getName() + " appears!");
					player.sendMessage("You teleport to " + target.getName() + "'s side!");
					if (COOLDOWN > 0) {
						startCooldown(player);
					}
					return true;
				}
			}
		} else if (command[1].equalsIgnoreCase("telehide")) {
			String n = player.getName().toLowerCase();
			if (hiddenPlayers.contains(n)) {
				hiddenPlayers.remove(n);
				player.sendMessage(TEXT_COLOR + STR_HIDE_OFF);
			} else {
				hiddenPlayers.add(n);
				player.sendMessage(TEXT_COLOR + STR_HIDE_ON);
			}
			return true;
		} else {
			// huh??
			return false;
		}
	}
	
	public String getCostDesc(String s) {
		if (s.equalsIgnoreCase("teleport")) {
			return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"");
		} else {
			return "free";
		}
	}	

}
