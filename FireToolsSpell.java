import java.util.HashMap;

public class FireToolsSpell extends Spell {
	
	// cost values
	private int INITIAL_REDSTONE_COST_AMT;
	private int INITIAL_OTHER_COST;
	private String INITIAL_OTHER_COST_NAME;
	private int INITIAL_OTHER_COST_AMT;
	private int SWING_COUNT_PER_REDSTONE;
	
	// strings
	private String STR_CAST_ON;
	private String STR_CAST_ON_OTHERS;
	private String STR_CAST_OFF;
	private String STR_CAST_OFF_OTHERS;
	
	private int [][] reagents;
	
	private HashMap<String,Integer> imbuedPlayers = new HashMap<String,Integer>();
	private int [][] itemConversions = new int [][] {{12,20},{1,1},{4,1},{15,265},{14,266}};
	
	public FireToolsSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		listener.registerSpellName(properties.getString("firetools-spellname","firetools"),this,properties.getString("firetools-desc","Imbues your tools with instant-smelting fire."));
		listener.requireBlockDestroyCall(this);
		
		// get properties
		INITIAL_REDSTONE_COST_AMT = properties.getInt("firetools-redstone-cost",15);
		INITIAL_OTHER_COST = properties.getInt("firetools-other-cost-type",263);
		INITIAL_OTHER_COST_NAME = properties.getString("firetools-other-cost-name","coal");
		INITIAL_OTHER_COST_AMT = properties.getInt("firetools-other-cost-amt",5);
		SWING_COUNT_PER_REDSTONE = properties.getInt("firetools-swings-per-redstone",5);
		
		STR_CAST_ON = properties.getString("firetools-cast-str","Your tools are imbued with smelting fire!");
		STR_CAST_ON_OTHERS = properties.getString("firetools-cast-others-str","[caster]'s tools glow with smelting fire!");
		STR_CAST_OFF = properties.getString("firetools-fade-str","The fire on your tools fades.");
		STR_CAST_OFF_OTHERS = properties.getString("firetools-fade-others-str","The glow fades from [caster]'s tools.");
		
		reagents = new int [][] {{REDSTONE_DUST,INITIAL_REDSTONE_COST_AMT},{INITIAL_OTHER_COST,INITIAL_OTHER_COST_AMT}};
	}
	
	public boolean cast(Player player, String [] command) {
		if (!imbuedPlayers.containsKey(player.getName())) {
			if (removeReagents(player,reagents)) {
				imbuedPlayers.put(player.getName(),0);
				player.sendMessage(TEXT_COLOR + STR_CAST_ON);
				sendMessageToPlayersInRange(player,STR_CAST_ON_OTHERS.replace("[caster]",player.getName()));
				return true;
			} else {
				player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
				return false;
			}
		} else {
			turnOff(player);
			return true;
		}
	}
	
	public String getCostDesc(String s) {
		return INITIAL_REDSTONE_COST_AMT + " " + REDSTONE_NAME + (INITIAL_OTHER_COST_AMT>0?", "+INITIAL_OTHER_COST_AMT+" "+INITIAL_OTHER_COST_NAME:"") + (SWING_COUNT_PER_REDSTONE>0?"@plus 1 "+REDSTONE_NAME+" every " + SWING_COUNT_PER_REDSTONE + " blocks smelted":"");
	}
	
	private void turnOff(Player player) {
		imbuedPlayers.remove(player.getName());
		player.sendMessage(TEXT_COLOR + STR_CAST_OFF);
		sendMessageToPlayersInRange(player,STR_CAST_OFF_OTHERS.replace("[caster]",player.getName()));
	}
	
	public boolean onBlockDestroy(Player player, Block block) {
		if (!imbuedPlayers.containsKey(player.getName())) {
			return false;
		} else {
			int t = block.getType();
			boolean returnVal = false;
			for (int i = 0; i < itemConversions.length; i++ ) {
				if (t == itemConversions[i][0]) {
					returnVal = true;
					if (block.getStatus() == 3) {
						etc.getServer().setBlockAt(0,block.getX(),block.getY(),block.getZ());
						etc.getServer().dropItem(block.getX(), block.getY(), block.getZ(), itemConversions[i][1]);						
						
						// do periodic redstone cost
						if (SWING_COUNT_PER_REDSTONE>0) {
							imbuedPlayers.put(player.getName(),imbuedPlayers.get(player.getName())+1);
							if (imbuedPlayers.get(player.getName()) > SWING_COUNT_PER_REDSTONE) {
								if (removeReagents(player,new int [][] {{REDSTONE_DUST,1}})) {
									// had redstone, reset counter to 0
									imbuedPlayers.put(player.getName(),0);
								} else {
									// out of redstone, turn off spell
									turnOff(player);
								}
							}
						}
					}
				}
			}
			
			return returnVal;
		}
	}
}
