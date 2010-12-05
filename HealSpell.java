public class HealSpell extends Spell {

	// cost values
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private int HEAL_AMOUNT;
	private int COOLDOWN;
	
	// strings
	private String STR_CAST;
	private String STR_CAST_OTHERS;
	private String STR_ERR_FULL_HEALTH;
	
	// reagents
	private int [][] reagents;

	public HealSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		String n = properties.getString("heal-spellname","heal");
		listener.registerSpellName(n,this,properties.getString("heal-desc","Restores your health."));
		listener.registerCastPattern(n,properties.getString("heal-cast-pattern","UR,DR"));
		
		// get properties
		REDSTONE_COST = properties.getInt("heal-redstone-cost",15);
		OTHER_COST = properties.getInt("heal-other-cost-type",0);
		OTHER_COST_NAME = properties.getString("heal-other-cost-name","nothing");
		OTHER_COST_AMT = properties.getInt("heal-other-cost-amt",0);
		
		HEAL_AMOUNT = properties.getInt("heal-amount-healed",20);
		COOLDOWN = properties.getInt("heal-cooldown-seconds",60);
		
		STR_CAST = properties.getString("heal-cast-str","You feel healthier.");
		STR_CAST_OTHERS = properties.getString("heal-cast-others-str","[caster] looks healthier.");
		STR_ERR_FULL_HEALTH = properties.getString("heal-err-full-health-str","You are already at full health.");
		
		// setup reagents
		reagents = new int [][] {{REDSTONE_DUST, REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
		
		if (COOLDOWN > 0) {
			enableCooldown();
		}
	}

	public boolean cast(Player player, String [] command) {
		int health;
		try {
			health = player.getHealth();
		} catch (Exception e) {
			sendMessage(player, "The heal spell is currently broken, sorry!");
			return false;
		}
		if (health == 20) {
			sendMessage(player, STR_ERR_FULL_HEALTH);
			return false;
		} else if (COOLDOWN > 0 && isOnCooldown(player, COOLDOWN)) {
			sendMessage(player, STR_ON_COOLDOWN);
			return false;
		} else if (removeReagents(player,reagents)) {
			health += HEAL_AMOUNT;
			if (health > 20) health = 20;
			player.setHealth(health);
			sendMessage(player, STR_CAST);
			sendMessageToPlayersInRange(player, STR_CAST_OTHERS.replace("[caster]",player.getName()));
			if (COOLDOWN > 0) {
				startCooldown(player);
			}
			return true;
		} else {
			sendMessage(player, STR_NO_REAGENTS);
			return false;
		}
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"");
	}
		

}
