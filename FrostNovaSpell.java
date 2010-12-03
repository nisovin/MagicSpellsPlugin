import java.util.List;

public class FrostNovaSpell extends Spell {
	
	// cost
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private int RANGE;
	private int COOLDOWN;
	private int FREEZE_BLOCK;
	
	// strings
	private String STR_CAST_CASTER;
	private String STR_CAST_OTHERS;
	private String STR_CAST_TARGET;
	
	private int [][] reagents;

	public FrostNovaSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("frostnova-spellname","frostnova"),this,properties.getString("frostnova-desc","Freezes enemies around you in ice."));
		
		// get properties
		REDSTONE_COST = properties.getInt("frostnova-redstone-cost",5);
		OTHER_COST = properties.getInt("frostnova-other-cost-type",0);
		OTHER_COST_NAME = properties.getString("frostnova-other-cost-name","");
		OTHER_COST_AMT = properties.getInt("frostnova-other-cost-amt",0);
		
		RANGE = properties.getInt("frostnova-range",15);
		COOLDOWN = properties.getInt("frostnova-cooldown-seconds",30);
		FREEZE_BLOCK = properties.getInt("frostnova-freeze-block",79);
		
		STR_CAST_CASTER = properties.getString("frostnova-cast-caster-str","Your enemies become frozen in ice!");
		STR_CAST_OTHERS = properties.getString("frostnova-cast-others-str","[caster]'s enemies become frozen in ice!");
		STR_CAST_TARGET = properties.getString("frostnova-cast-target-str","You are frozen in ice!");
		
		if (COOLDOWN > 0) {
			enableCooldown();
		}
		
		// prepare reagents
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
	}

	public boolean cast(Player player, String [] command) {
		Server s = etc.getServer();
		List<LivingEntity> entities = s.getLivingEntityList();
		
		for (LivingEntity e : entities) {
			if (player.getId() != e.getId() && inRange(player, e)) {
				// freeze them!
				int x = (int)e.getX();
				int y = (int)e.getY();
				int z = (int)e.getZ();
				
				// make sure they're centered
				e.teleportTo(x+.5,y,z+.5,e.getRotation(),e.getPitch());
				
				// create ice
				if (s.getBlockIdAt(x,y,z-1) == 0) s.setBlockAt(FREEZE_BLOCK,x,y,z-1);
				if (s.getBlockIdAt(x,y,z+1) == 0) s.setBlockAt(FREEZE_BLOCK,x,y,z+1);
				if (s.getBlockIdAt(x-1,y,z) == 0) s.setBlockAt(FREEZE_BLOCK,x-1,y,z);
				if (s.getBlockIdAt(x+1,y,z) == 0) s.setBlockAt(FREEZE_BLOCK,x+1,y,z);
				if (s.getBlockIdAt(x,y+1,z-1) == 0) s.setBlockAt(FREEZE_BLOCK,x,y+1,z-1);
				if (s.getBlockIdAt(x,y+1,z+1) == 0) s.setBlockAt(FREEZE_BLOCK,x,y+1,z+1);
				if (s.getBlockIdAt(x-1,y+1,z) == 0) s.setBlockAt(FREEZE_BLOCK,x-1,y+1,z);
				if (s.getBlockIdAt(x+1,y+1,z) == 0) s.setBlockAt(FREEZE_BLOCK,x+1,y+1,z);
				
			}
		}
		
		return true;
	}
	
	public boolean inRange(Player player, LivingEntity target) {
		return Math.pow(player.getX()-target.getX(),2) + Math.pow(player.getY()-target.getY(),2) + Math.pow(player.getZ()-target.getZ(),2) < RANGE*RANGE;
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", " + OTHER_COST_AMT + " " + OTHER_COST_NAME:"");
	}
	

}
