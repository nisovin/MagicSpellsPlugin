import java.util.HashMap;
import java.util.ArrayList;

public class BubbleSpell extends Spell {
	
	HashMap<String,ArrayList<int[]>> bubbles;
	HashMap<String,Integer> distance;

	// cost values
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	private int DISTANCE_PER_REDSTONE;
	
	// options
	private int RADIUS;
	private boolean SEA_ONLY;
	
	// strings
	private String STR_CAST;
	private String STR_CAST_OTHERS;
	private String STR_FADE;
	private String STR_FADE_OTHERS;
	
	// reagents
	private int [][] reagents;
	private int [][] walkReagents;

	public BubbleSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("bubble-spellname","bubble"),this,properties.getString("bubble-desc","Creates a bubble of air around you."));
		listener.requirePlayerMoveCall(this);
		listener.requireFlowCall(this);
		listener.requireDisconnectCall(this);
		
		// get properties
		REDSTONE_COST = properties.getInt("bubble-redstone-cost",10);
		OTHER_COST = properties.getInt("bubble-other-cost-type",326);
		OTHER_COST_NAME = properties.getString("bubble-other-cost-name","water bucket");
		OTHER_COST_AMT = properties.getInt("bubble-other-cost-amt",1);
		DISTANCE_PER_REDSTONE = properties.getInt("bubble-distance-per-redstone",10);
		
		RADIUS = properties.getInt("bubble-radius",3);
		SEA_ONLY = properties.getBoolean("bubble-affect-sea-only",true);
		
		STR_CAST = properties.getString("bubble-cast-str","A bubble of air surrounds you.");
		STR_CAST_OTHERS = properties.getString("bubble-cast-others-str","A bubble of air surrounds [caster].");
		STR_FADE = properties.getString("bubble-fade-str","The bubble of air around you pops!");
		STR_FADE_OTHERS = properties.getString("bubble-fade-others-str","The bubble of air around [caster] pops!");
		
		// setup reagents
		reagents = new int [][] {{REDSTONE_DUST, REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
		walkReagents = new int [][] {{REDSTONE_DUST,1}};
		
		bubbles = new HashMap<String,ArrayList<int[]>>();
		distance = new HashMap<String,Integer>();
	}

	public boolean cast(Player player, String [] command) {
		if (bubbles.containsKey(player.getName())) {
			turnOff(player);
		} else if (removeReagents(player, reagents)) {
			bubbles.put(player.getName(),new ArrayList<int[]>());
			distance.put(player.getName(),0);
			sendMessage(player, STR_CAST);
			sendMessageToPlayersInRange(player, STR_CAST_OTHERS.replace("[caster]",player.getName()));
		} else {
			sendMessage(player, STR_NO_REAGENTS);
			return false;
		}
		return true;
	}
	
	public void turnOff(Player player) {
		fillBubble(player, true);
		bubbles.remove(player.getName());
		distance.remove(player.getName());
		sendMessage(player, STR_FADE);
		sendMessageToPlayersInRange(player, STR_FADE_OTHERS.replace("[caster]",player.getName()));
	}
	
	public void onPlayerMove(Player player, Location from, Location to) {
		Server s = etc.getServer();
		if (bubbles.containsKey(player.getName())) {			
			// fill in bubble in places where we've wandered too far from
			fillBubble(player,false);
			
			// create bubble
			ArrayList<int[]> l = bubbles.get(player.getName());
			boolean didSomething = false;
			for (int x = (int)(player.getX())-RADIUS; x <= (int)(player.getX())+RADIUS; x++) {
				for (int y = (int)(player.getY())-RADIUS; y <= (int)(player.getY())+RADIUS; y++) {
					for (int z = (int)(player.getZ())-RADIUS; z <= (int)(player.getZ())+RADIUS; z++) {
						if ((!SEA_ONLY || y <= 64) && (s.getBlockIdAt(x,y,z) == 8 || s.getBlockIdAt(x,y,z) == 9) && inRange(x,y,z,(int)(player.getX()-.5),(int)player.getY(),(int)(player.getZ()-.5),RADIUS)) {
							s.setBlockAt(0,x,y,z);
							l.add(new int [] {x,y,z});
							didSomething = true;
						}
					}
				}
			}
			
			// charge reagents
			if (didSomething && DISTANCE_PER_REDSTONE > 0) {
				int dist = distance.get(player.getName())+1;
				if (dist >= DISTANCE_PER_REDSTONE) {
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
	
	private void fillBubble(Player player, boolean all) {
		ArrayList<int[]> l = bubbles.get(player.getName());
		for (int i = 0; i < l.size(); i++) {
			int [] coords = l.get(i);
			if (all || !inRange(coords[0],coords[1],coords[2],(int)player.getX(),(int)player.getY(), (int)player.getZ(),RADIUS)) {
				etc.getServer().setBlockAt(8,coords[0],coords[1],coords[2]);
				l.remove(i);
				i--;
			}
		}	
	}
	
	public boolean inRange(int x1, int y1, int z1, int x2, int y2, int z2, int range) {
		int dx = x1-x2;
		int dy = y1-y2;
		int dz = z1-z2;
		if (dx*dx + dy*dy + dz*dz <= range*range) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean onFlow(Block from, Block to) {
		if (bubbles.size() > 0 && (from.getType() == 8 || from.getType() == 9) && to.getType() == 0) {
			for (String s : bubbles.keySet()) {
				Player p = etc.getServer().getPlayer(s);
				if (p != null) {
					if (inRange(to.getX(),to.getY(),to.getZ(),(int)(p.getX()-.5),(int)p.getY(),(int)(p.getZ()-.5),RADIUS)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void onDisconnect(Player player) {
		if (bubbles.containsKey(player.getName())) {
			turnOff(player);
		}
	}
	
	public void cancel(Player player) {
		if (bubbles.containsKey(player.getName())) {
			turnOff(player);
		}		
	}
	
	public void disable() {
		for (String s : bubbles.keySet()) {
			turnOff(etc.getServer().getPlayer(s));
		}
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"") + (DISTANCE_PER_REDSTONE>0?"@plus 1 "+REDSTONE_NAME+" per "+DISTANCE_PER_REDSTONE+" blocks walked":"");
	}
		

}
