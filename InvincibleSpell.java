import java.util.HashMap;

public class InvincibleSpell extends Spell {

	// cost values
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private int DURATION;
	private int COOLDOWN;
	
	// strings
	private String STR_CAST;
	private String STR_CAST_OTHERS;
	private String STR_FADE;
	private String STR_FADE_OTHERS;
	
	// reagents
	private int [][] reagents;
	
	// data
	private HashMap<String,Long> invinciblePpl;

	public InvincibleSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("invincible-spellname","invincible"),this,properties.getString("invincible-desc","Makes you invulnerable to damage for a time."));
		listener.requireHealthChangeCall(this);
		
		// get properties
		REDSTONE_COST = properties.getInt("invincible-redstone-cost",15);
		OTHER_COST = properties.getInt("invincible-other-cost-type",49);
		OTHER_COST_NAME = properties.getString("invincible-other-cost-name","obsidian");
		OTHER_COST_AMT = properties.getInt("invincible-other-cost-amt",1);
		
		DURATION = properties.getInt("invincible-duration-seconds",60);
		COOLDOWN = properties.getInt("invincible-cooldown-seconds",300);
		
		STR_CAST = properties.getString("invincible-cast-str","You feel your skin harden.");
		STR_CAST_OTHERS = properties.getString("invincible-cast-others-str","[caster]'s skin seems to harden.");
		STR_FADE = properties.getString("invincible-fade-str","You feel your skin return to normal.");
		STR_FADE_OTHERS = properties.getString("invincible-fade-others-str","[caster]'s skin returns to normal.");
		
		// setup reagents
		reagents = new int [][] {{REDSTONE_DUST, REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
		
		invinciblePpl = new HashMap<String,Long>();
		
		if (COOLDOWN > 0) {
			enableCooldown();
		}
	}

	public boolean cast(Player player, String [] command) {
		if (COOLDOWN > 0 && isOnCooldown(player, COOLDOWN)) {
			sendMessage(player, STR_ON_COOLDOWN);
			return false;
		} else if (removeReagents(player, reagents)) {
			long t = 0;
			if (invinciblePpl.containsKey(player.getName())) {
				// already invincible - extend duration
				t = invinciblePpl.get(player.getName()) + (DURATION*1000);
			} else {
				// not invincible yet
				t = System.currentTimeMillis() + (DURATION*1000);
			}
			invinciblePpl.put(player.getName(), t);
			player.sendMessage(TEXT_COLOR + STR_CAST);
			sendMessageToPlayersInRange(player, STR_CAST_OTHERS.replace("[caster]",player.getName()));
			
			if (COOLDOWN > 0) {
				startCooldown(player);
			}
			
			return true;
		} else {
			player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
			return false;
		}
	}
	
	public boolean onHealthChange(Player player, int oldValue, int newValue) {
		if (invinciblePpl.containsKey(player.getName())) {
			if (invinciblePpl.get(player.getName()) > System.currentTimeMillis()) {
				// currently invincible - cancel damage
				return true;
			} else {
				// no longer invincible
				invinciblePpl.remove(player.getName());
				player.sendMessage(TEXT_COLOR + STR_FADE);
				sendMessageToPlayersInRange(player, STR_FADE_OTHERS.replace("[caster]",player.getName()));
				return false;
			}
		} else {
			return false;
		}
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"");
	}
		

}
