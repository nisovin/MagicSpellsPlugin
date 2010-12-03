public class ExtinguishSpell extends Spell {

	// cost values
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private int RADIUS;
	
	// strings
	private String STR_CAST;
	private String STR_CAST_OTHERS;
	
	// reagents
	private int [][] reagents;

	public ExtinguishSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("extinguish-spellname","extinguish"),this,properties.getString("extinguish-desc","Extinguishes all fires around you."));
		
		// get properties
		REDSTONE_COST = properties.getInt("extinguish-redstone-cost",10);
		OTHER_COST = properties.getInt("extinguish-other-cost-type",259);
		OTHER_COST_NAME = properties.getString("extinguish-other-cost-name","flint and steel");
		OTHER_COST_AMT = properties.getInt("extinguish-other-cost-amt",1);
		
		RADIUS = properties.getInt("extinguish-radius",15);
		
		STR_CAST = properties.getString("extinguish-cast-str","You summon a sudden gust of wind that puts the fire out.");
		STR_CAST_OTHERS = properties.getString("extinguish-cast-others-str","[caster] summons a gust of wind to put out fire.");
		
		// setup reagents
		reagents = new int [][] {{REDSTONE_DUST, REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
	}

	public boolean cast(Player player, String [] command) {
		if (removeReagents(player, reagents)) {
			for (int x = (int)player.getX() - RADIUS; x <= (int)player.getX() + RADIUS; x++) {
				for (int y = (int)player.getY() - RADIUS; y <= (int)player.getY() + RADIUS; y++) {
					for (int z = (int)player.getZ() - RADIUS; z <= (int)player.getZ() + RADIUS; z++) {
						if (etc.getServer().getBlockIdAt(x,y,z) == 51) {
							etc.getServer().setBlockAt(0,x,y,z);
						}
					}
				}
			}
			sendMessage(player, STR_CAST);
			sendMessageToPlayersInRange(player, STR_CAST_OTHERS.replace("[caster]", player.getName()));
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
