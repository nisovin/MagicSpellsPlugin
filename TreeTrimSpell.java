import java.util.HashSet;

public class TreeTrimSpell extends Spell {
	
	// cost values
	private int INITIAL_REDSTONE_COST_AMT;
	private int USE_REDSTONE_COST;

	// strings
	private String STR_CAST_ON;
	private String STR_CAST_ON_OTHERS;
	private String STR_CAST_OFF;
	private String STR_CAST_OFF_OTHERS;
	private String STR_USE;
	private String STR_USE_OTHERS;	

	// options
	private int DESTROY_RADIUS;

	// reagents
	private static int [][] reagents;
	private static int [][] reagentsUse;
	
	private HashSet<String> treeTrimmers = new HashSet<String>();
	

	public TreeTrimSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
	
		// register spell
		listener.registerSpellName(properties.getString("treetrim-spellname","treetrim"),this,properties.getString("treetrim-desc","Trims extra foliage from trees."));
		listener.requireBlockDestroyCall(this);
		
		// get properties
		INITIAL_REDSTONE_COST_AMT = properties.getInt("treetrim-initial-redstone-cost",10);
		USE_REDSTONE_COST = properties.getInt("treetrim-use-redstone-cost",3);
		
		DESTROY_RADIUS = properties.getInt("treetrim-destroy-radius",2);
		
		STR_CAST_ON = properties.getString("treetrim-cast-on-str","Your axe gains super tree trimming powers!");
		STR_CAST_ON_OTHERS = properties.getString("treetrim-cast-on-others-str","[caster]'s axe glows green!");
		STR_CAST_OFF = properties.getString("treetrim-cast-off-str","Your axe loses its tree trimming powers.");
		STR_CAST_OFF_OTHERS = properties.getString("treetrim-cast-off-others-str","[caster]'s axe loses its green glow.");
		STR_USE = properties.getString("treetrim-use-str","Swish!");
		STR_USE_OTHERS = properties.getString("treetrim-use-others-str","Swish!");
		
		// setup reagents
		reagents = new int [][] {{REDSTONE_DUST,INITIAL_REDSTONE_COST_AMT}};
		reagentsUse = new int [][] {{REDSTONE_DUST,USE_REDSTONE_COST}};
	}
	
	public boolean cast(Player player, String [] command) {		
		if (!treeTrimmers.contains(player.getName())) {
			if (removeReagents(player,reagents)) {
				treeTrimmers.add(player.getName());
				player.sendMessage(TEXT_COLOR + STR_CAST_ON);
				sendMessageToPlayersInRange(player,STR_CAST_ON_OTHERS.replace("[caster]",player.getName()));
			} else {
				player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
			}
		} else {
			turnOff(player);
		}
		return true;
	}
	
	public String getCostDesc(String s) {
		return INITIAL_REDSTONE_COST_AMT + " " + REDSTONE_NAME + "@plus " + USE_REDSTONE_COST + " " + REDSTONE_NAME + " per use";
	}
	
	private void turnOff(Player player) {
		treeTrimmers.remove(player.getName());
		player.sendMessage(TEXT_COLOR + STR_CAST_OFF);
		sendMessageToPlayersInRange(player,STR_CAST_OFF_OTHERS.replace("[caster]",player.getName()));
	}
	
	public boolean onBlockDestroy(Player player, Block block) {
		int item = player.getItemInHand();
		if (treeTrimmers.contains(player.getName()) && block.getType() == 18 && (item == 258 || item == 271 || item == 275 || item == 279)) {
			if (removeReagents(player,reagentsUse)) {
				Server s = etc.getServer();
				for (int x = block.getX()-DESTROY_RADIUS; x <= block.getX()+DESTROY_RADIUS; x++) {
					for (int y = block.getY()-DESTROY_RADIUS; y <= block.getY()+DESTROY_RADIUS; y++) {
						for (int z = block.getZ()-DESTROY_RADIUS; z <= block.getZ()+DESTROY_RADIUS; z++) {
							if (s.getBlockIdAt(x,y,z) == 18) {
								s.setBlockAt(0,x,y,z);
							}
						}
					}
				}
				player.sendMessage(TEXT_COLOR + STR_USE);
				sendMessageToPlayersInRange(player,STR_USE_OTHERS.replace("[caster]",player.getName()));
			} else {
				turnOff(player);
			}
		}
		return false;
	}
	
}
