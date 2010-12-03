import java.util.HashMap;

public class LightwalkSpell extends Spell {

	private HashMap<String,Block> lightwalkers = new HashMap<String,Block>();
	private HashMap<String,Integer> distance = new HashMap<String,Integer>();
	private static final int LIGHTBLOCK = 89;
	
	// cost values
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	private int DISTANCE_PER_REDSTONE;
	
	// strings
	private String STR_CAST_ON;
	private String STR_CAST_ON_OTHERS;
	private String STR_CAST_OFF;
	private String STR_CAST_OFF_OTHERS;
	
	// reagents
	private int [][] castReagents;
	private int [][] walkReagents;

	public LightwalkSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("lightwalk-spellname","lightwalk"),this,properties.getString("lightwalk-desc","Gives you light wherever you stand"));
		listener.requirePlayerMoveCall(this);
		listener.requireBlockDestroyCall(this);
		listener.requireDisconnectCall(this);
		
		// get properties
		REDSTONE_COST = properties.getInt("lightwalk-redstone-cost",10);
		OTHER_COST = properties.getInt("lightwalk-other-cost-type",50);
		OTHER_COST_NAME = properties.getString("lightwalk-other-cost-name","torches");
		OTHER_COST_AMT = properties.getInt("lightwalk-other-cost-amt",2);
		DISTANCE_PER_REDSTONE = properties.getInt("lightwalk-dist-per-redstone",10);
		
		STR_CAST_ON = properties.getString("lightwalk-cast-on-str","Light emanates at your feet!");
		STR_CAST_ON_OTHERS = properties.getString("lightwalk-cast-on-others-str","Light emanates at [caster]'s feet!");
		STR_CAST_OFF = properties.getString("lightwalk-cast-off-str","The light at your feet goes out.");
		STR_CAST_OFF_OTHERS = properties.getString("lightwalk-cast-off-others-str","The light at [caster]'s feet goes out.");
		
		// setup reagents
		castReagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
		walkReagents = new int [][] {{REDSTONE_DUST,1}};
		
	}
	
	public boolean cast(Player player, String [] command) {
		if (lightwalkers.containsKey(player.getName())) {
			turnOff(player);
			return true;
		} else {
			if (removeReagents(player,castReagents)) {
				cancelSpells(player, new String [] {"LifewalkSpell","StoneVisionSpell"});
				lightwalkers.put(player.getName(),null);
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
		if (lightwalkers.containsKey(player.getName())) {
			turnOff(player);
		}
	}
	
	private void turnOff(Player player) {
		Block b = lightwalkers.get(player.getName());
		if (b != null) b.update();
		lightwalkers.remove(player.getName());
		distance.remove(player.getName());
		player.sendMessage(TEXT_COLOR + STR_CAST_OFF);
		sendMessageToPlayersInRange(player, STR_CAST_OFF_OTHERS.replace("[caster]",player.getName()));
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"") + (DISTANCE_PER_REDSTONE>0?"@plus 1 "+REDSTONE_NAME+" per "+DISTANCE_PER_REDSTONE+" blocks walked":"");
	}
	
	public void onPlayerMove(Player player, Location from, Location to) {
		if (lightwalkers.containsKey(player.getName())) {
		
			// get blocks standing on			
			Block b = etc.getServer().getBlockAt((int)to.x,(int)to.y-1,(int)to.z);
			int t = b.getType();
			
			// check for light-able
			if (etc.getServer().getBlockIdAt((int)to.x,(int)to.y,(int)to.z) == 0 && (t==1 || t==2 || t==3 || t==4 || t==12 || t==13 || t==78 || t==87 || t==88)) {
				// reset old block
				Block old = lightwalkers.get(player.getName());
				if (old != null) {
					old.update();
				}
				
				// update block
				etc.getServer().setBlockAt(LIGHTBLOCK,(int)to.x,(int)to.y-1,(int)to.z);
				lightwalkers.put(player.getName(), b);
				
				// check reagents
				if (DISTANCE_PER_REDSTONE > 0) {
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
			} else { // can't change to lightblock
				Block old = lightwalkers.get(player.getName());
				if (old != null && Math.sqrt(Math.pow((int)player.getX()-old.getX(),2) + Math.pow((int)player.getZ()-old.getZ(),2)) > 4) {
					// getting far away from lightblock, so turn it off
					old.update();
					lightwalkers.put(player.getName(),null);
				}
			}
		}
	}
	
	public boolean onBlockDestroy(Player player, Block block) {
		if (block.getType() == LIGHTBLOCK) {
			for (String s : lightwalkers.keySet()) {
				Block b = lightwalkers.get(s);
				if (b != null && b.getX() == block.getX() && b.getY() == block.getY() && b.getZ() == block.getZ()) {
					b.update();
					lightwalkers.put(s,null);
				} 
			}
		}
		return false;
	}
	
	public void onDisconnect(Player player) {
		if (lightwalkers.containsKey(player.getName())) {
			turnOff(player);
		}
	}
	
	public void disable() {
		for (String s : lightwalkers.keySet()) {
			Block b = lightwalkers.get(s);
			if (b!=null) b.update();
		}
		lightwalkers.clear();
		distance.clear();
	}

}
