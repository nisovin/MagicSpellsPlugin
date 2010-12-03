import java.util.HashMap;
import java.util.Random;

public class LifewalkSpell extends Spell {

	private HashMap<String,Long> lifewalkers = new HashMap<String,Long>();
	private Random random = new Random();
	
	// costs
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private int DURATION;
	private int CHANCE_FOR_SAPLING;
	private int CHANCE_FOR_YELLOW_FLOWER;
	private int CHANCE_FOR_RED_FLOWER;
	
	// strings
	private String STR_CASTER_ON;
	private String STR_CASTER_OFF;
	private String STR_OTHERS_ON;
	private String STR_OTHERS_OFF;
	
	int [][] reagents;

	public LifewalkSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("lifewalk-spellname","lifewalk"),this,properties.getString("lifewalk-desc","Causes flowers to bloom at your feet!"));
		listener.requirePlayerMoveCall(this);
		
		// get properties
		REDSTONE_COST = properties.getInt("lifewalk-redstone-cost",10);
		OTHER_COST = properties.getInt("lifewalk-other-cost-type",38);
		OTHER_COST_NAME = properties.getString("lifewalk-other-cost-name","red flower");
		OTHER_COST_AMT = properties.getInt("lifewalk-other-cost-amt",1);
		
		DURATION = properties.getInt("lifewalk-duration",120);
		CHANCE_FOR_SAPLING = properties.getInt("lifewalk-sapling-chance",2);
		CHANCE_FOR_YELLOW_FLOWER = properties.getInt("lifewalk-yellowflower-chance",12);
		CHANCE_FOR_RED_FLOWER = properties.getInt("lifewalk-redflower-chance",12);
		
		STR_CASTER_ON = properties.getString("lifewalk-cast-caster-str","The land around you springs to life!");
		STR_CASTER_OFF = properties.getString("lifewalk-fade-caster-str","The land around you returns to normal.");
		STR_OTHERS_ON = properties.getString("lifewalk-cast-others-str","The land around [caster] springs to life!");
		STR_OTHERS_OFF = properties.getString("lifewalk-fade-others-str","The land around [caster] returns to normal.");
		
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
	}
	
	public boolean cast(Player player, String [] command) {
		if (!lifewalkers.containsKey(player.getName())) {
			if (removeReagents(player,reagents)) {
				cancelSpells(player,new String [] {"LightwalkSpell","DeathwalkSpell"});
				lifewalkers.put(player.getName(),System.currentTimeMillis());
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
		if (lifewalkers.containsKey(player.getName())) {
			turnOff(player);
		}
	}
	
	public void turnOff(Player player) {
		lifewalkers.remove(player.getName());
		player.sendMessage(TEXT_COLOR + STR_CASTER_OFF);
		sendMessageToPlayersInRange(player,STR_OTHERS_OFF.replace("[caster]",player.getName()));
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"");
	}
	
	public void onPlayerMove(Player player, Location from, Location to) {
		if (lifewalkers.containsKey(player.getName())) {
			// check duration
			if (System.currentTimeMillis() - lifewalkers.get(player.getName()) < DURATION*1000) {
				Block b = etc.getServer().getBlockAt((int)from.x, (int)from.y-1, (int)from.z);
				Block b2 = etc.getServer().getBlockAt((int)from.x, (int)from.y, (int)from.z);
				if (b.getType() == 3) {
					b.setType(2);
					b.update();
				}

				if (b.getType() == 2 && b2.getType() == 0) {
					int r = random.nextInt(100);
					if (r < CHANCE_FOR_SAPLING) {
						b2.setType(6);
						b2.update();
					} else if (r < CHANCE_FOR_YELLOW_FLOWER+CHANCE_FOR_SAPLING) {
						b2.setType(37);
						b2.update();
					} else if (r < CHANCE_FOR_RED_FLOWER+CHANCE_FOR_YELLOW_FLOWER+CHANCE_FOR_SAPLING) {
						b2.setType(38);
						b2.update();
					}
				}
			} else {
				// out of time
				turnOff(player);
			}
		}
	}

}
