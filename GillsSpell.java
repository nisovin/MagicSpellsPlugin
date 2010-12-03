import java.util.HashMap;

public class GillsSpell extends Spell {

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
	private HashMap<String,Long> fishes;

	public GillsSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("gills-spellname","gills"),this,properties.getString("gills-desc","Allows you to breath underwater."));
		listener.requireDamageCall(this);
		
		// get properties
		REDSTONE_COST = properties.getInt("gills-redstone-cost",15);
		OTHER_COST = properties.getInt("gills-other-cost-type",326);
		OTHER_COST_NAME = properties.getString("gills-other-cost-name","water bucket");
		OTHER_COST_AMT = properties.getInt("gills-other-cost-amt",1);
		
		DURATION = properties.getInt("gills-duration-seconds",60);
		COOLDOWN = properties.getInt("gills-cooldown-seconds",300);
		
		STR_CAST = properties.getString("gills-cast-str","Gills form on your neck.");
		STR_CAST_OTHERS = properties.getString("gills-cast-others-str","Gills form on [caster]'s neck.");
		STR_FADE = properties.getString("gills-fade-str","Your gills disappear.");
		STR_FADE_OTHERS = properties.getString("gills-fade-others-str","[caster]'s gills disappear.");
		
		// setup reagents
		reagents = new int [][] {{REDSTONE_DUST, REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
		
		fishes = new HashMap<String,Long>();
		
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
			if (fishes.containsKey(player.getName())) {
				// already invincible - extend duration
				t = fishes.get(player.getName()) + (DURATION*1000);
			} else {
				// not invincible yet
				t = System.currentTimeMillis() + (DURATION*1000);
			}
			fishes.put(player.getName(), t);
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
	
	public boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker, BaseEntity defender, int amount) {
		if (defender.isPlayer()) {
			Player player = defender.getPlayer();
			if (player != null && type == PluginLoader.DamageType.WATER && fishes.containsKey(player.getName())) {
				if (fishes.get(player.getName()) > System.currentTimeMillis()) {
					return true;
				} else {
					fishes.remove(player.getName());
					player.sendMessage(TEXT_COLOR + STR_FADE);
					sendMessageToPlayersInRange(player, STR_FADE_OTHERS.replace("[caster]",player.getName()));
					return false;					
				}
			}
		}
		return false;
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"");
	}
		

}
