
public class AscendAndDescendSpell extends Spell {

	private String ASCEND_SPELLNAME;
	private String DESCEND_SPELLNAME;

	// cost value
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private int COOLDOWN;
	
	// strings
	private String STR_ASCEND_CAST;
	private String STR_ASCEND_CAST_OTHERS_LEAVE;
	private String STR_ASCEND_CAST_OTHERS_ARRIVE;
	private String STR_ASCEND_FAIL;
	private String STR_DESCEND_CAST;
	private String STR_DESCEND_CAST_OTHERS_LEAVE;
	private String STR_DESCEND_CAST_OTHERS_ARRIVE;
	private String STR_DESCEND_FAIL;
	
	// reagents
	private int [][] reagents;

	public AscendAndDescendSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spells
		ASCEND_SPELLNAME = properties.getString("ascend-spellname","ascend");
		DESCEND_SPELLNAME = properties.getString("descend-spellname","descend");
		listener.registerSpellName(ASCEND_SPELLNAME,this,properties.getString("ascend-desc","Causes you to ascend to a higher location."));
		listener.registerSpellName(DESCEND_SPELLNAME,this,properties.getString("descend-desc","Causes you to descend to a lower location."));
		listener.registerCastPattern(ASCEND_SPELLNAME,properties.getString("ascend-cast-pattern","U,-"));
		listener.registerCastPattern(DESCEND_SPELLNAME,properties.getString("descend-cast-pattern","D,-"));
		
		// get properties
		REDSTONE_COST = properties.getInt("ascend-descend-redstone-cost",10);
		OTHER_COST = properties.getInt("ascend-descend-other-cost-type",0);
		OTHER_COST_NAME = properties.getString("ascend-descend-other-cost-name","");
		OTHER_COST_AMT = properties.getInt("ascend-descend-other-cost-amt",0);
		
		COOLDOWN = properties.getInt("ascend-descend-cooldown-seconds",120);
		
		STR_ASCEND_CAST = properties.getString("ascend-cast-str","You have ascended!");
		STR_ASCEND_CAST_OTHERS_LEAVE = properties.getString("ascend-cast-leave-str","[caster] vanishes!");
		STR_ASCEND_CAST_OTHERS_ARRIVE = properties.getString("ascend-cast-arrive-str","[caster] appears!");
		STR_ASCEND_FAIL = properties.getString("ascend-fail-str","Nowhere to ascend to!");
		STR_DESCEND_CAST = properties.getString("descend-cast-str","You have descended!");
		STR_DESCEND_CAST_OTHERS_LEAVE = properties.getString("descend-cast-leave-str","[caster] vanishes!");
		STR_DESCEND_CAST_OTHERS_ARRIVE = properties.getString("descend-cast-arrive-str","[caster] appears!");
		STR_DESCEND_FAIL = properties.getString("descend-fail-str","Nowhere to descend to!");
		
		// setup reagents
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
		
		if (COOLDOWN > 0) {
			enableCooldown();
		}	
		
	}
	
	public boolean cast(Player player, String [] command) {
		if (COOLDOWN > 0 && isOnCooldown(player, COOLDOWN)) {
			sendMessage(player, STR_ON_COOLDOWN);
			return false;
		} else {
		
			// reagents found and removed - cast spell
			if (command[1].equalsIgnoreCase(DESCEND_SPELLNAME)) {			
				Location location = findPlaceToStand(player, "down");
				if (location != null) {
					if (removeReagents(player,reagents)) {
						sendMessageToPlayersInRange(player,STR_DESCEND_CAST_OTHERS_LEAVE.replace("[caster]",player.getName()));
						player.teleportTo(location);
						sendMessage(player, STR_DESCEND_CAST);
						sendMessageToPlayersInRange(player,STR_DESCEND_CAST_OTHERS_ARRIVE.replace("[caster]",player.getName()));
					
						if (COOLDOWN > 0) {
							startCooldown(player);
						}
						
						return true;
					} else {
						// reagents not found
						sendMessage(player, STR_NO_REAGENTS);
						return false;
					}
					
				} else {
				
					// no spot found to ascend
					sendMessage(player, STR_DESCEND_FAIL);
					return false;
				}
			
			} else if (command[1].equalsIgnoreCase(ASCEND_SPELLNAME)) {				
				Location location = findPlaceToStand(player, "up");
				if (location != null) {
					if (removeReagents(player,reagents)) {
						sendMessageToPlayersInRange(player,STR_ASCEND_CAST_OTHERS_LEAVE.replace("[caster]",player.getName()));
						player.teleportTo(location);
						sendMessage(player, STR_ASCEND_CAST);
						sendMessageToPlayersInRange(player,STR_ASCEND_CAST_OTHERS_ARRIVE.replace("[caster]",player.getName()));
					
						if (COOLDOWN > 0) {
							startCooldown(player);
						}
						
						return true;
					} else {
						// reagents not found
						sendMessage(player, STR_NO_REAGENTS);
						return false;
					}
				} else {
				
					// no spot found to ascend
					sendMessage(player, STR_ASCEND_FAIL);
					return false;
				}
			} else {
				// uuhhhh...
				return false;
			}
		} 
	}
	
	public static Location findPlaceToStand(Player player, String direction) {
		int step;
		if (direction.equals("up")) {
			step = 1;
		} else if (direction.equals("down")) {
			step = -1;
		} else {
			return null;
		}
		
		// get player position
		int x = (int)Math.round(player.getX()-.5);
		int y = (int)Math.round(player.getY()+step+step);
		int z = (int)Math.round(player.getZ()-.5);
		
		Server s = etc.getServer();
				
		// search for a spot to stand
		while (2 < y && y < 125) {
			if (s.getBlockIdAt(x,y,z) != 0 && s.getBlockIdAt(x,y+1,z) == 0 && s.getBlockIdAt(x,y+2,z) == 0) {
				// spot found - return location
				return new Location((double)x+.5,(double)y+1,(double)z+.5,player.getRotation(),player.getPitch());
			}
			y += step;
		}
		
		// no spot found
		return null;
	}

	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"");
	}
}
