import java.util.List;
import java.util.HashMap;

abstract class Spell {

	public static String TEXT_COLOR = Colors.DarkPurple;
	public static String FREE_SPELL_RANK = "wizards";
	protected static int BROADCAST_RANGE = 20;

	protected static int REDSTONE_DUST = 331;
	protected static String REDSTONE_NAME = "redstone dust";
	
	protected static String STR_NO_REAGENTS = "You do not have the reagents to cast that spell.";
	protected static String STR_ON_COOLDOWN = "You cannot cast that spell again so soon.";

	protected HashMap<String,Long> lastCast;

	Spell() {
		return;
	}
	
	Spell(MagicSpellsListener listener, PropertiesFile properties) {
		TEXT_COLOR = "§" + properties.getString("general-spell-text-color","9");
		FREE_SPELL_RANK = properties.getString("general-free-spell-rank","wizards");
		BROADCAST_RANGE = properties.getInt("general-broadcast-range",20);
		REDSTONE_DUST = properties.getInt("general-main-reagent-type",331);
		REDSTONE_NAME = properties.getString("general-main-reagent-name","redstone dust");
		STR_NO_REAGENTS = properties.getString("general-spell-no-reagents-str","You do not have the reagents to cast that spell.");
		STR_ON_COOLDOWN = properties.getString("general-spell-on-cooldown-str","You cannot cast that spell again so soon.");
	}
	
	abstract boolean cast(Player player, String [] command);
	
	abstract String getCostDesc(String spellName);
	
	boolean needsLearned() {
		return true;
	}
	
	boolean removeReagents(Player player, int [][] cost) {
		if (player.isInGroup(FREE_SPELL_RANK)) {
			return true;
		}
		
		Inventory inv = player.getInventory();
		
		// check if player has items in inventory
		if (inv == null) System.out.println("uhoh");
		for (int i = 0; i < cost.length; i++) {
			if (cost[i][0] > 0 && cost[i][1] > 0 && !inv.hasItem(cost[i][0],cost[i][1],64)) {
				return false;
			}
		}
		
		// remove items
		for (int i = 0; i < cost.length; i++) {
			if (cost[i][0] > 0 && cost[i][1] > 0) {
				inv.removeItem(new Item(cost[i][0],cost[i][1]));
			}
		}
		inv.updateInventory();
		
		return true;
	}
	
	void sendMessage(Player player, String msg) {
		if (!msg.equals("")) {
			player.sendMessage(TEXT_COLOR + msg);
		}
	}
	
	void sendMessageToPlayersInRange(Player player, String msg) {
		sendMessageToPlayersInRange(player, msg, BROADCAST_RANGE, "");
	}
	
	void sendMessageToPlayersInRange(Player player, String msg, int range) {
		sendMessageToPlayersInRange(player, msg, range, "");
	}
	
	void sendMessageToPlayersInRange(Player player, String msg, int range, String except) {
		if (!msg.equals("")) {
			List<Player> allPlayers = etc.getServer().getPlayerList();

			// get players in range
			for (Player p : allPlayers) {
				if (!p.getName().equalsIgnoreCase(player.getName()) && !p.getName().equalsIgnoreCase(except) && isPlayerInRange(p,player,range)) {
					// send msg
					p.sendMessage(TEXT_COLOR + msg);
				}
			}
		}
	}
	
	boolean isPlayerInRange(Player p1, Player p2, int range) {
		return (Math.sqrt(Math.pow(p1.getX()-p2.getX(),2)+Math.pow(p1.getY()-p2.getY(),2)+Math.pow(p1.getZ()-p2.getZ(),2)) <= range);
	}
	
	void enableCooldown() {
		lastCast = new HashMap<String,Long>();
	}
	
	boolean isOnCooldown(Player player, int cooldown) {
		if (lastCast == null) {
			return false;
		} else if (player.isInGroup(FREE_SPELL_RANK)) {
			return false;
		} else if (!lastCast.containsKey(player.getName())) {
			return false;
		} else if (System.currentTimeMillis() - lastCast.get(player.getName()) > cooldown*1000) {
			return false;
		} else {
			return true;
		}
	}
	
	void startCooldown(Player player) {
		if (lastCast != null) {
			lastCast.put(player.getName(), System.currentTimeMillis());
		}
	}
	
	void cancelSpells(Player player, String [] spellsToCancel) {
		MagicSpellsListener.cancelSpells(player, spellsToCancel);
	}
	
	boolean onBlockPlace(Player player, Block placed, Block clicked, Item item) {
		return false;
	}
	
	void onBlockRightClicked(Player player, Block block, Item itemInHand) {
		return;
	}
	
	boolean onBlockDestroy(Player player, Block block) {
		return false;
	}
	
	void onArmSwing(Player player) {
		return;
	}
	
	void onPlayerMove(Player player, Location from, Location to) {
		return;
	}
	
	void onDisconnect(Player player) {
		return;
	}
	
	boolean onHealthChange(Player player, int oldValue, int newValue) {
		return false;
	}
	
	boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker, BaseEntity defender, int amount) {
		return false;
	}
	
	boolean onFlow(Block from, Block to) {
		return false;
	}
	
	void cancel(Player player) {
		return;
	}
	
	void disable() {
		return;
	}
}
