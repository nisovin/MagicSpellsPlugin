public class BlinkSpell extends Spell {
	
	// cost
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private int RANGE;
	private int COOLDOWN;
	
	// strings
	private String STR_CAST_CASTER;
	private String STR_CAST_OTHERS;
	private String STR_ERR_TOO_FAR;
	private String STR_ERR_NO_SPACE;
	
	private int [][] reagents;

	public BlinkSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("blink-spellname","blink"),this,properties.getString("blink-desc","Teleport to your targeted location."));
		
		// get properties
		REDSTONE_COST = properties.getInt("blink-redstone-cost",5);
		OTHER_COST = properties.getInt("blink-other-cost-type",0);
		OTHER_COST_NAME = properties.getString("blink-other-cost-name","");
		OTHER_COST_AMT = properties.getInt("blink-other-cost-amt",0);
		
		RANGE = properties.getInt("blink-range",40);
		COOLDOWN = properties.getInt("blink-cooldown-seconds",30);
		
		STR_CAST_CASTER = properties.getString("blink-cast-caster-str","You blink away!");
		STR_CAST_OTHERS = properties.getString("blink-cast-others-str","[caster] blinks away!");
		STR_ERR_TOO_FAR = properties.getString("blink-err-too-far-str","You cannot blink that far.");
		STR_ERR_NO_SPACE = properties.getString("blink-err-no-space","You cannot blink there.");
		
		if (COOLDOWN > 0) {
			enableCooldown();
		}
		
		// prepare reagents
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
	}

	public boolean cast(Player player, String [] command) {
		HitBlox hit = new HitBlox(player);
		Block target = hit.getTargetBlock();
		Block face = hit.getLastBlock();
		
		if (target == null) {
			player.sendMessage(TEXT_COLOR + STR_ERR_TOO_FAR);
			return false;
		} else if (RANGE > 0 && getDistance(player,target) > RANGE) {
			player.sendMessage(TEXT_COLOR + STR_ERR_TOO_FAR);
			return false;
		} else if (COOLDOWN > 0 && isOnCooldown(player,COOLDOWN)) {
			player.sendMessage(TEXT_COLOR + STR_ON_COOLDOWN);
			return false;
		} else if (!removeReagents(player,reagents)) {
			player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
			return false;
		} else if (etc.getServer().getBlockIdAt(target.getX(),target.getY()+1,target.getZ()) == 0 && etc.getServer().getBlockIdAt(target.getX(),target.getY()+2,target.getZ()) == 0) {
			// teleport to top of target block if possible
			player.sendMessage(TEXT_COLOR + STR_CAST_CASTER);
			sendMessageToPlayersInRange(player,STR_CAST_OTHERS.replace("[caster]",player.getName()));
			player.teleportTo(new Location(target.getX()+.5,(double)target.getY()+1,target.getZ()+.5,player.getRotation(),player.getPitch()));
			if (COOLDOWN > 0) {
				startCooldown(player);
			}
			return true;
		} else if (face.getType() == 0 && etc.getServer().getBlockIdAt(face.getX(),face.getY()+1,face.getZ()) == 0) {
			// otherwise teleport to face of target block
			player.sendMessage(TEXT_COLOR + STR_CAST_CASTER);
			sendMessageToPlayersInRange(player,STR_CAST_OTHERS.replace("[caster]",player.getName()));
			player.teleportTo(new Location(face.getX()+.5,(double)face.getY(),face.getZ()+.5,player.getRotation(),player.getPitch()));
			if (COOLDOWN > 0) {
				startCooldown(player);
			}
			return true;
		} else {
			// no place to stand
			player.sendMessage(TEXT_COLOR + STR_ERR_NO_SPACE);
			return false;
		}
	}
	
	public double getDistance(Player player, Block target) {
		return Math.sqrt(Math.pow(player.getX()-target.getX(),2) + Math.pow(player.getY()-target.getY(),2) + Math.pow(player.getZ()-target.getZ(),2));
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", " + OTHER_COST_AMT + " " + OTHER_COST_NAME:"");
	}
	

}
