import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class DeathwalkSpell extends Spell {

	private HashMap<String,Long> deathwalkers = new HashMap<String,Long>();
	private Random random = new Random();
	
	// costs
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private int DURATION;
	private int RANGE;
	private int KILL_MOB_CHANCE;
	
	// strings
	private String STR_CASTER_ON;
	private String STR_CASTER_OFF;
	private String STR_OTHERS_ON;
	private String STR_OTHERS_OFF;
	
	int [][] reagents;

	public DeathwalkSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("deathwalk-spellname","deathwalk"),this,properties.getString("deathwalk-desc","Causes death all around you"));
		listener.requirePlayerMoveCall(this);
		
		// get properties
		REDSTONE_COST = properties.getInt("deathwalk-redstone-cost",10);
		OTHER_COST = properties.getInt("deathwalk-other-cost-type",38);
		OTHER_COST_NAME = properties.getString("deathwalk-other-cost-name","red flower");
		OTHER_COST_AMT = properties.getInt("deathwalk-other-cost-amt",1);
		
		DURATION = properties.getInt("deathwalk-duration",120);
		RANGE = properties.getInt("deathwalk-death-radius",2);
		KILL_MOB_CHANCE = properties.getInt("deathwalk-kill-mob-chance",20);
		
		STR_CASTER_ON = properties.getString("deathwalk-cast-caster-str","The land around you begins to decay.");
		STR_CASTER_OFF = properties.getString("deathwalk-fade-caster-str","The land around you returns to normal.");
		STR_OTHERS_ON = properties.getString("deathwalk-cast-others-str","The land around [caster] begins to decay.");
		STR_OTHERS_OFF = properties.getString("deathwalk-fade-others-str","The land around [caster] returns to normal.");
		
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
	}
	
	public boolean cast(Player player, String [] command) {
		if (!deathwalkers.containsKey(player.getName())) {
			if (removeReagents(player,reagents)) {
				cancelSpells(player,new String [] {"LightwalkSpell","LifewalkSpell"});
				deathwalkers.put(player.getName(),System.currentTimeMillis());
				player.sendMessage(TEXT_COLOR + STR_CASTER_ON);
				sendMessageToPlayersInRange(player,STR_OTHERS_ON.replace("[caster]",player.getName()));
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
	
	public void cancel(Player player) {
		if (deathwalkers.containsKey(player.getName())) {
			turnOff(player);
		}
	}
	
	public void turnOff(Player player) {
		deathwalkers.remove(player.getName());
		player.sendMessage(TEXT_COLOR + STR_CASTER_OFF);
		sendMessageToPlayersInRange(player,STR_OTHERS_OFF.replace("[caster]",player.getName()));
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"");
	}
	
	public void onPlayerMove(Player player, Location from, Location to) {
		if (deathwalkers.containsKey(player.getName())) {
			// check duration
			if (System.currentTimeMillis() - deathwalkers.get(player.getName()) < DURATION*1000) {
				Server s = etc.getServer();
				
				// kill plants
				int y = (int)to.y;
				for (int x = (int)to.x - RANGE; x <= (int)to.x + RANGE; x++) {
					for (int z = (int)to.z - RANGE; z <= (int)to.z + RANGE; z++) {
						if (s.getBlockIdAt(x,y-1,z) == 2) {
							s.setBlockAt(3,x,y-1,z);
						}
						int id = s.getBlockIdAt(x,y,z);
						if (id==6 || id==37 || id==38 || id==39 || id==40) {
							s.setBlockAt(0,x,y,z);
						}
					}
				}
				
				// kill animals
				if (random.nextInt(100) < KILL_MOB_CHANCE) {
					List<Mob> mobs = s.getMobList();
					for (Mob mob : mobs) {
						if (Math.sqrt(Math.pow(player.getX()-mob.getX(),2) + Math.pow(player.getY()-mob.getY(),2) + Math.pow(player.getZ()-mob.getZ(),2)) < RANGE) {
							mob.setHealth(0);
						}
					}
				}
			} else {
				// out of time
				turnOff(player);
			}
		}
	}

}
