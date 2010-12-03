public class DestroySpell extends Spell {

	// spell costs
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private int BLAST_RADIUS;
	
	// strings
	private String STR_CAST;
	private String STR_CAST_OTHERS;
	
	// reagents
	private int [][] reagents;

	public DestroySpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("destroy-spellname","destroy"),this,properties.getString("destroy-desc","Destroys target blocks"));
		
		// get properties
		REDSTONE_COST = properties.getInt("destroy-redstone-cost",10);
		OTHER_COST = properties.getInt("destroy-other-cost-type",289);
		OTHER_COST_NAME = properties.getString("destroy-other-cost-name","gunpowder");
		OTHER_COST_AMT = properties.getInt("destroy-other-cost-amt",1);
		
		BLAST_RADIUS = properties.getInt("destroy-blast-radius",2);
		
		STR_CAST = properties.getString("destroy-cast-str","Boom!");
		STR_CAST_OTHERS = properties.getString("destroy-cast-others-str","Boom!");
		
		// setup reagents
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
		
	}
	
	public boolean cast(Player player, String [] command) {
		if (removeReagents(player,reagents)) {
			// reagents removed - cast spell
			
			// get targeted block
			HitBlox hit = new HitBlox(player);
			Block target = hit.getTargetBlock();
			
			// remove blocks around target block
			Server s = etc.getServer();
			for (int x = target.getX()-BLAST_RADIUS; x <= target.getX()+BLAST_RADIUS; x++) {
				for (int y = target.getY()-BLAST_RADIUS; y <= target.getY()+BLAST_RADIUS; y++) {
					for (int z = target.getZ()-BLAST_RADIUS; z <= target.getZ()+BLAST_RADIUS; z++) {
						s.setBlockAt(0,x,y,z);
					}
				}
			}
			
			// send alerts
			player.sendMessage(TEXT_COLOR + STR_CAST);
			sendMessageToPlayersInRange(player,STR_CAST_OTHERS);
			
			return true;
		} else {
			// reagents missing
			player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
			return false;
		}
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"");
	}

}
