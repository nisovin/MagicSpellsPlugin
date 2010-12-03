import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;

public class StoneVisionSpell extends Spell {

	private Hashtable<String,ArrayList<int[]>> data = new Hashtable<String,ArrayList<int[]>>();
	private Hashtable<String,Long> started = new Hashtable<String,Long>();
	
	// cost
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;

	// options
	private int VIEW_RANGE;
	private int DURATION;

	// strings
	private String STR_CASTER_ON;
	private String STR_CASTER_OFF;
	private String STR_OTHERS_ON;
	private String STR_OTHERS_OFF;
	
	int [][] reagents;
	
	public StoneVisionSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener, properties);
		
		// register spell
		listener.registerSpellName(properties.getString("stonevision-spellname","stonevision"),this,properties.getString("stonevision-desc","Allows you to see through stone for a time."));
		listener.requirePlayerMoveCall(this);
		listener.requireDisconnectCall(this);
		listener.requireBlockDestroyCall(this);
		
		// get properties
		REDSTONE_COST = properties.getInt("stonevision-redstone-cost",10);
		OTHER_COST = properties.getInt("stonevision-other-cost-type",1);
		OTHER_COST_NAME = properties.getString("stonevision-other-cost-name","smooth stone");
		OTHER_COST_AMT = properties.getInt("stonevision-other-cost-amt",2);
		
		VIEW_RANGE = properties.getInt("stonevision-view-range",4);	
		DURATION = properties.getInt("stonevision-duration-seconds",90);
		
		STR_CASTER_ON = properties.getString("stonevision-cast-caster-str","The stone around you becomes transparent!");
		STR_CASTER_OFF = properties.getString("stonevision-fade-caster-str","The stone around you becomes opaque once more.");
		STR_OTHERS_ON = properties.getString("stonevision-cast-others-str","The stone around [caster] becomes transparent!");
		STR_OTHERS_OFF = properties.getString("stonevision-fade-others-str","The stone around [caster] becomes opaque once more.");
		
		// setup reagents
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
	}
	
	public boolean cast(Player player, String [] command) {
		if (!data.containsKey(player.getName())) {
			if (removeReagents(player,reagents)) {
				ArrayList<int[]> a = new ArrayList<int[]>();
				stoneToGlass((int)player.getX(),(int)player.getY(),(int)player.getZ(),a);
				data.put(player.getName(),a);
				started.put(player.getName(),System.currentTimeMillis());
				player.sendMessage(TEXT_COLOR + STR_CASTER_ON);
				sendMessageToPlayersInRange(player, STR_OTHERS_ON.replace("[caster]",player.getName()), VIEW_RANGE*3);
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
	
	public void turnOff(Player player) {
		glassToStone(0,0,0,data.get(player.getName()),true);
		data.remove(player.getName());
		started.remove(player.getName());
		player.sendMessage(TEXT_COLOR + STR_CASTER_OFF);
		sendMessageToPlayersInRange(player, STR_OTHERS_OFF.replace("[caster]",player.getName()), VIEW_RANGE*3);	
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"");
	}
	
	private void stoneToGlass(int px, int py, int pz, ArrayList<int[]> stones) {
		Server s = etc.getServer();
		for (int x = px-VIEW_RANGE; x <= px+VIEW_RANGE; x++) {
			for (int y = py-VIEW_RANGE; y <= py+VIEW_RANGE; y++) {
				for (int z = pz-VIEW_RANGE; z <= pz+VIEW_RANGE; z++) {
					Block b = s.getBlockAt(x,y,z);
					if (b.getType() == 1 && safeToSet(b)) {
						stones.add(new int[] {x,y,z});
						s.setBlockAt(20,x,y,z);
					}
				}
			}
		}
	}
	
	private void glassToStone(int px, int py, int pz, ArrayList<int[]> stones, boolean allToStone) {
		Server s = etc.getServer();
		for (int i = 0; i < stones.size(); i++) {
			int [] coords = stones.get(i);
			if (s.getBlockIdAt(coords[0],coords[1],coords[2]) == 20 && (allToStone || Math.sqrt( Math.pow(px-stones.get(i)[0],2) + Math.pow(py-stones.get(i)[1],2) + Math.pow(pz-stones.get(i)[2],2) ) > VIEW_RANGE)) {
				s.setBlockAt(1,coords[0],coords[1],coords[2]);
				stones.remove(i);
				i--;
			}
		}
	}

	public static boolean safeToSet(Block block) {
		for(Block adjBlock : getAdjacentBlocks(block)) {
			if(getSupport(adjBlock) != null && sameLocation(getSupport(adjBlock), block)) return false;
		}
		return true;
	}

	public static boolean sameLocation(Block block1, Block block2) {
		if(block1 == null || block2 == null) return false;
		return block1.getX() == block2.getX() && block1.getY() == block2.getY() && block1.getZ() == block2.getZ();
	}

	public static List<Block> getAdjacentBlocks(Block block) {
		Server server = etc.getServer();
		List<Block> blocks = new ArrayList<Block>();
		int x = block.getX();
		int y = block.getY();
		int z = block.getZ();
		blocks.add(server.getBlockAt(x+1,y,z));
		blocks.add(server.getBlockAt(x-1,y,z));
		blocks.add(server.getBlockAt(x,y+1,z));
		blocks.add(server.getBlockAt(x,y-1,z));
		blocks.add(server.getBlockAt(x,y,z+1));
		blocks.add(server.getBlockAt(x,y,z-1));
		return blocks;
	}

	public static Block getSupport(Block block) {
		Server server = etc.getServer();
		int type = block.getType();
		int x = block.getX();
		int y = block.getY();
		int z = block.getZ();
		int data = server.getBlockData(x,y,z);
		if(type == 77) {
			if(data == 1 || data == 9) return server.getBlockAt(x-1,y,z);
			if(data == 2 || data == 10) return server.getBlockAt(x+1,y,z);
			if(data == 3 || data == 11) return server.getBlockAt(x,y,z-1);
			if(data == 4 || data == 12) return server.getBlockAt(x,y,z+1);
		} else if(type == 50 || type == 75 || type == 76) {
			if(data == 1) return server.getBlockAt(x-1,y,z);
			if(data == 2) return server.getBlockAt(x+1,y,z);
			if(data == 3) return server.getBlockAt(x,y,z-1);
			if(data == 4) return server.getBlockAt(x,y,z+1);
			if(data == 5) return server.getBlockAt(x,y-1,z);
		} else if(type == 65) {
			if(data == 2) return server.getBlockAt(x,y,z+1);
			if(data == 3) return server.getBlockAt(x,y,z-1);
			if(data == 4) return server.getBlockAt(x+1,y,z);
			if(data == 5) return server.getBlockAt(x-1,y,z);
		} else if(type == 69) {
			if(data == 1 || data == 9) return server.getBlockAt(x-1,y,z);
			if(data == 2 || data == 10) return server.getBlockAt(x+1,y,z);
			if(data == 3 || data == 11) return server.getBlockAt(x,y,z-1);
			if(data == 4 || data == 12) return server.getBlockAt(x,y,z+1);
			if(data == 5 || data == 13) return server.getBlockAt(x,y-1,z);
		} else if(type == 68) {
			if(data == 2) return server.getBlockAt(x,y,z+1);
			if(data == 3) return server.getBlockAt(x,y,z-1);
			if(data == 4) return server.getBlockAt(x+1,y,z);
			if(data == 5) return server.getBlockAt(x-1,y,z);
		} else if(type == 70 ||
				type == 72 ||
				type == 66 ||
				type == 63 ||
				type == 64 ||
				type == 71 ||
				type == 39 ||
				type == 55 ||
				type == 40) {
			return server.getBlockAt(x,y-1,z);
		}
		return null;
	}
	
	public void onPlayerMove(Player player, Location from, Location to) {
		if (data.containsKey(player.getName())) {
			// check duration
			if (System.currentTimeMillis() - started.get(player.getName()) < DURATION*1000) {
				glassToStone((int)to.x, (int)to.y, (int)to.z, data.get(player.getName()), false);
				stoneToGlass((int)to.x, (int)to.y, (int)to.z, data.get(player.getName()));
			} else {
				turnOff(player);
			}
		}
	}
	
	public boolean onBlockDestroy(Player player, Block block) {
		if (block.getType() == 20 && data.size() > 0) {
			for (String s : data.keySet()) {
				ArrayList<int[]> l = data.get(s);
				for (int i = 0; i < l.size(); i++) {
					int [] coords = l.get(i);
					if (coords[0] == block.getX() && coords[1] == block.getY() && coords[2] == block.getZ()) {
						l.remove(i);
						etc.getServer().setBlockAt(1, coords[0], coords[1], coords[2]);
						break;	
					}
				}
			}
		}
		return false;
	}
	
	public void onDisconnect(Player player) {
		if (data.containsKey(player.getName())) {
			glassToStone(0,0,0,data.get(player.getName()),true);
			data.remove(player.getName());
		}
	}
	
	public void disable() {
		for (String s : data.keySet()) {
			glassToStone(0,0,0,data.get(s),true);
			data.remove(s);
		}
	}
	
}
