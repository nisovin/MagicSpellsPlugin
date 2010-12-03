import java.util.List;

public class PurgeSpell extends Spell {
	
	// cost
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private int RANGE;
	private int COOLDOWN;
	
	// strings
	private String STR_CAST_CASTER;
	private String STR_CAST_OTHERS;
	
	private int [][] reagents;

	public PurgeSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("purge-spellname","purge"),this,properties.getString("purge-desc","Kill all nearby animals and monsters."));
		
		// get properties
		REDSTONE_COST = properties.getInt("purge-redstone-cost",15);
		OTHER_COST = properties.getInt("purge-other-cost-type",0);
		OTHER_COST_NAME = properties.getString("purge-other-cost-name","");
		OTHER_COST_AMT = properties.getInt("purge-other-cost-amt",0);
		
		RANGE = properties.getInt("purge-range",15);
		COOLDOWN = properties.getInt("purge-cooldown-seconds",60);
		
		STR_CAST_CASTER = properties.getString("purge-cast-caster-str","A wave of death emanates from you.");
		STR_CAST_OTHERS = properties.getString("purge-cast-others-str","A wave of death emanates from [caster].");
		
		if (COOLDOWN > 0) {
			enableCooldown();
		}
		
		// prepare reagents
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
	}

	public boolean cast(Player player, String [] command) {
		if (COOLDOWN > 0 && isOnCooldown(player,COOLDOWN)) {
			player.sendMessage(TEXT_COLOR + STR_ON_COOLDOWN);
			return false;
		} else if (!removeReagents(player,reagents)) {
			player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
			return false;
		} else {
			// send messages
			player.sendMessage(TEXT_COLOR + STR_CAST_CASTER);
			sendMessageToPlayersInRange(player,STR_CAST_OTHERS.replace("[caster]",player.getName()));
			// kill all mobs nearby
			List<Mob> mobs = etc.getServer().getMobList();
			for (Mob mob : mobs) {
				if (Math.sqrt(Math.pow(player.getX()-mob.getX(),2) + Math.pow(player.getY()-mob.getY(),2) + Math.pow(player.getZ()-mob.getZ(),2)) < RANGE) {
					mob.setHealth(0);
				}
			}
			// set cooldown
			if (COOLDOWN > 0) {
				startCooldown(player);
			}
			return true;
		}
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", " + OTHER_COST_AMT + " " + OTHER_COST_NAME:"");
	}
	

}
