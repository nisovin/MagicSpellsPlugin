import java.util.HashSet;

public class SafefallSpell extends Spell {

	// cost values
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private int COOLDOWN;
	
	// strings
	private String STR_ERR;
	private String STR_CAST;
	private String STR_USE;
	private String STR_USE_OTHERS;
	
	// reagents
	private int [][] reagents;
	
	// data
	private HashSet<String> safeFallers;

	public SafefallSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("safefall-spellname","safefall"),this,properties.getString("safefall-desc","Fall without taking damage."));
		listener.requireDamageCall(this);
		
		// get properties
		REDSTONE_COST = properties.getInt("safefall-redstone-cost",5);
		OTHER_COST = properties.getInt("safefall-other-cost-type",288);
		OTHER_COST_NAME = properties.getString("safefall-other-cost-name","feather");
		OTHER_COST_AMT = properties.getInt("safefall-other-cost-amt",1);
		
		COOLDOWN = properties.getInt("safefall-cooldown-seconds",60);
		
		STR_ERR = properties.getString("safefall-err-str","You have already activated that spell.");
		STR_CAST = properties.getString("safefall-cast-str","The next time you fall you will not take damage.");
		STR_USE = properties.getString("safefall-fade-str","You land lightly.");
		STR_USE_OTHERS = properties.getString("safefall-fade-others-str","[caster] lands lightly.");
		
		// setup reagents
		reagents = new int [][] {{REDSTONE_DUST, REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
		
		enableCooldown();
		
		safeFallers = new HashSet<String>();

	}

	public boolean cast(Player player, String [] command) {
		if (safeFallers.contains(player.getName())) {
			sendMessage(player, STR_ERR);
			return false;
		} else if (isOnCooldown(player, COOLDOWN)) {
			sendMessage(player, STR_ON_COOLDOWN);
			return false;
		} else if (removeReagents(player, reagents)) {
			safeFallers.add(player.getName());
			sendMessage(player, TEXT_COLOR + STR_CAST);
			startCooldown(player);
			return true;
		} else {
			sendMessage(player, TEXT_COLOR + STR_NO_REAGENTS);
			return false;
		}
	}
	
	public boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker, BaseEntity defender, int amount) {
		if (defender.isPlayer()) {
			Player player = defender.getPlayer();
			if (player != null && safeFallers.contains(player.getName()) && type == PluginLoader.DamageType.FALL) {
				safeFallers.remove(player.getName());
				sendMessage(player, TEXT_COLOR + STR_USE);
				sendMessageToPlayersInRange(player, STR_USE_OTHERS.replace("[caster]",player.getName()));
				return true;
			}
		}
		return false;
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"");
	}
		

}
