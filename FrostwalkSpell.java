import java.util.HashMap;
import java.util.HashSet;

public class FrostwalkSpell extends Spell {

	private HashMap<String,HashSet<Block>> frostwalkers = new HashMap<String,HashSet<Block>>();
	private HashMap<String,Integer> distance = new HashMap<String,Integer>();
	
	// cost values
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	private int DISTANCE_PER_REDSTONE;
	
	private int RANGE;
	private boolean THAW_AFTER;
	
	// strings
	private String STR_CAST_ON;
	private String STR_CAST_ON_OTHERS;
	private String STR_CAST_OFF;
	private String STR_CAST_OFF_OTHERS;
	
	// reagents
	private int [][] castReagents;
	private int [][] walkReagents;

	public FrostwalkSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("frostwalk-spellname","frostwalk"),this,properties.getString("frostwalk-desc","Freezes the water at your feet"));
		listener.requirePlayerMoveCall(this);
		listener.requireBlockDestroyCall(this);
		listener.requireDisconnectCall(this);
		
		// get properties
		REDSTONE_COST = properties.getInt("frostwalk-redstone-cost",10);
		OTHER_COST = properties.getInt("frostwalk-other-cost-type",79);
		OTHER_COST_NAME = properties.getString("frostwalk-other-cost-name","ice");
		OTHER_COST_AMT = properties.getInt("frostwalk-other-cost-amt",1);
		DISTANCE_PER_REDSTONE = properties.getInt("frostwalk-dist-per-redstone",10);
		
		RANGE = properties.getInt("frostwalk-range",2);
		THAW_AFTER = properties.getBoolean("frostwalk-thaw-after",true);
		
		STR_CAST_ON = properties.getString("frostwalk-cast-on-str","The area around you feels colder!");
		STR_CAST_ON_OTHERS = properties.getString("frostwalk-cast-on-others-str","The area around [caster] gets colder!");
		STR_CAST_OFF = properties.getString("frostwalk-cast-off-str","The cold around you goes away.");
		STR_CAST_OFF_OTHERS = properties.getString("frostwalk-cast-off-others-str","The cold around [caster] goes away.");
		
		// setup reagents
		castReagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
		walkReagents = new int [][] {{REDSTONE_DUST,1}};
		
	}
	
	public boolean cast(Player player, String [] command) {
		if (frostwalkers.containsKey(player.getName())) {
			turnOff(player);
			return true;
		} else {
			if (removeReagents(player,castReagents)) {
				frostwalkers.put(player.getName(),new HashSet<Block>());
				distance.put(player.getName(),0);
				player.sendMessage(TEXT_COLOR + STR_CAST_ON);
				sendMessageToPlayersInRange(player, STR_CAST_ON_OTHERS.replace("[caster]",player.getName()));
				return true;
			} else {
				player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
				return false;
			}
		}
	}
	
	public void cancel(Player player) {
		if (frostwalkers.containsKey(player.getName())) {
			turnOff(player);
		}
	}
	
	private void turnOff(Player player) {
		HashSet<Block> blocks = frostwalkers.get(player.getName());
		for (Block b : blocks) {
			b.update();
		}
		frostwalkers.remove(player.getName());
		distance.remove(player.getName());
		player.sendMessage(TEXT_COLOR + STR_CAST_OFF);
		sendMessageToPlayersInRange(player, STR_CAST_OFF_OTHERS.replace("[caster]",player.getName()));
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"") + (DISTANCE_PER_REDSTONE>0?"@plus 1 "+REDSTONE_NAME+" per "+DISTANCE_PER_REDSTONE+" blocks walked":"");
	}
	
	public void onPlayerMove(Player player, Location from, Location to) {
		if (frostwalkers.containsKey(player.getName())) {
			HashSet<Block> blocks = frostwalkers.get(player.getName());
			
			boolean didSomething = false;
			
			// return blocks to water
			if (THAW_AFTER) {
				for (Block b : blocks) {
					if (Math.pow(to.x-b.getX(),2) + Math.pow(to.z-b.getZ(),2) > RANGE*RANGE) {
						b.update();
						didSomething = true;
					}
				}
			}
			
			// set water to ice
			if ((int)to.y == 64) {
				Server s = etc.getServer();
				for (int x = (int)to.x - RANGE; x <= (int)to.x + RANGE; x++) {
					for (int z = (int)to.z - RANGE; z <= (int)to.z + RANGE; z++) {
						//if (Math.pow(to.x-x,2) + Math.pow(to.z-z,2) <= RANGE*RANGE) {
							if (s.getBlockIdAt(x,(int)to.y,z) == 0 && (s.getBlockIdAt(x,(int)to.y-1,z) == 9 || s.getBlockIdAt(x,(int)to.y-1,z) == 8)) {
								blocks.add(s.getBlockAt(x,(int)to.y-1,z));
								s.setBlockAt(79,x,(int)to.y-1,z);
								didSomething = true;
							}
						//}
					}
				}
			}
				
			// check reagents
			if (didSomething && DISTANCE_PER_REDSTONE > 0) {
				int dist = distance.get(player.getName()) + 1;
				if (dist > DISTANCE_PER_REDSTONE) {
					// remove reagents
					if (removeReagents(player,walkReagents)) {
						distance.put(player.getName(),0);
					} else {
						turnOff(player);
					}
				} else {
					distance.put(player.getName(),dist);
				}
			}
		}
	}
	
	public void onDisconnect(Player player) {
		if (frostwalkers.containsKey(player.getName())) {
			turnOff(player);
		}
	}
	
	public void disable() {
		for (String s : frostwalkers.keySet()) {
			HashSet<Block> blocks = frostwalkers.get(s);
			for (Block b : blocks) {
				b.update();
			}
		}
		frostwalkers.clear();
		distance.clear();
	}

}
