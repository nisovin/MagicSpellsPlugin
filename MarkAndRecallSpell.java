import java.util.Hashtable;

public class MarkAndRecallSpell extends Spell {

	// cost values
	private int MARK_REDSTONE_COST_AMT;
	private int MARK_OTHER_COST;
	private String MARK_OTHER_COST_NAME;
	private int MARK_OTHER_COST_AMT;
	private int RECALL_REDSTONE_COST_AMT;
	
	// options
	private boolean MARK_RESET_ON_DISCONNECT;
	private boolean USE_HOMES;
	private int COOLDOWN;
	
	// strings
	private String STR_MARK_SPELLNAME;
	private String STR_RECALL_SPELLNAME;
	private String STR_MARK_CAST;
	private String STR_RECALL_CAST;
	private String STR_RECALL_CAST_OTHERS_LEAVE;
	private String STR_RECALL_CAST_OTHERS_ARRIVE;
	private String STR_RECALL_NO_MARK;

	// reagent arrays
	private int [][] markReagents;
	private int [][] recallReagents;

	private Hashtable<String,String> marks = new Hashtable<String,String>();

	public MarkAndRecallSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spells
		STR_MARK_SPELLNAME = properties.getString("mark-spellname","mark");
		STR_RECALL_SPELLNAME = properties.getString("recall-spellname","recall");
		listener.registerSpellName(STR_MARK_SPELLNAME,this,properties.getString("mark-desc","Marks a location to be recalled to later"));
		listener.registerSpellName(STR_RECALL_SPELLNAME,this,properties.getString("recall-desc","Recalls you to your marked location"));
		listener.requireDisconnectCall(this);
		
		// get property values
		MARK_REDSTONE_COST_AMT = properties.getInt("mark-redstone-cost-amt",20);
		MARK_OTHER_COST = properties.getInt("mark-other-cost-type",265);
		MARK_OTHER_COST_NAME = properties.getString("mark-other-cost-name","iron bar");
		MARK_OTHER_COST_AMT = properties.getInt("mark-other-cost-amt",1);
		RECALL_REDSTONE_COST_AMT = properties.getInt("recall-redstone-cost-amt",5);
		
		USE_HOMES = properties.getBoolean("mark-recall-use-homes",false);
		MARK_RESET_ON_DISCONNECT = properties.getBoolean("mark-reset-on-disconnect",true);
		COOLDOWN = properties.getInt("recall-cooldown-seconds",0);
		
		STR_MARK_CAST = properties.getString("mark-cast-str","You have marked your location.");
		STR_RECALL_CAST = properties.getString("recall-cast-str","You are yanked through nothingness to your marked location!");
		STR_RECALL_CAST_OTHERS_LEAVE = properties.getString("recall-cast-leave-str","[caster] vanishes!");
		STR_RECALL_CAST_OTHERS_ARRIVE = properties.getString("recall-cast-arrive-str","[caster] appears!");
		STR_RECALL_NO_MARK = properties.getString("recall-cast-nomark","You have not marked a location to recall to.");
		
		// setup reagents
		markReagents = new int [][] {{REDSTONE_DUST, MARK_REDSTONE_COST_AMT}, {MARK_OTHER_COST, MARK_OTHER_COST_AMT}};
		recallReagents = new int [][] {new int [] {REDSTONE_DUST, RECALL_REDSTONE_COST_AMT}};
		
		if (COOLDOWN > 0) {
			enableCooldown();
		}
	}
	
	public boolean cast(Player player, String [] command) {
		if (command[1].equalsIgnoreCase(STR_MARK_SPELLNAME)) {
			if (removeReagents(player,markReagents)) {
				// reagents removed- save the mark
				if (USE_HOMES) {
					Warp home = new Warp();
					home.Location = player.getLocation();
					home.Group = "";
					home.Name = player.getName();
					etc.getInstance().changeHome(home);
				} else {
					marks.put(player.getName(),player.getX()+","+player.getY()+","+player.getZ()+","+player.getRotation()+","+player.getPitch());
				}
				player.sendMessage(TEXT_COLOR + STR_MARK_CAST);
				return true;
			} else {
				// reagents missing
				player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
				return false;
			}
		} else if (command[1].equalsIgnoreCase(STR_RECALL_SPELLNAME)) {
			Warp home = null;
			if (USE_HOMES) {
				home = etc.getDataSource().getHome(player.getName());
			}
			if (home != null || marks.containsKey(player.getName())) {
				// player has a mark - cast spell
				if (isOnCooldown(player, COOLDOWN)) {
					player.sendMessage(TEXT_COLOR + STR_ON_COOLDOWN);
					return false;
				} else if (removeReagents(player,recallReagents)) {
					// reagents removed - send player to mark
					sendMessageToPlayersInRange(player,STR_RECALL_CAST_OTHERS_LEAVE.replace("[caster]",player.getName()));
					if (USE_HOMES) {
						player.teleportTo(home.Location);
					} else {
						String [] coords = marks.get(player.getName()).split(",");
						player.teleportTo(new Location(Double.valueOf(coords[0]).doubleValue(),Double.valueOf(coords[1]).doubleValue(),Double.valueOf(coords[2]).doubleValue(),Float.valueOf(coords[3]).floatValue(),Float.valueOf(coords[4]).floatValue()));
					}
					player.sendMessage(TEXT_COLOR + STR_RECALL_CAST);
					sendMessageToPlayersInRange(player,STR_RECALL_CAST_OTHERS_ARRIVE.replace("[caster]",player.getName()));
					if (COOLDOWN > 0) {
						startCooldown(player);
					}
					return true;
				} else {
					// reagents missing
					player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
					return false;
				}
			} else {
				// no marked location
				player.sendMessage(TEXT_COLOR + STR_RECALL_NO_MARK);
				return false;
			}
		} else {
			return false;
		}
	}
	
	public String getCostDesc(String s) {
		if (s.equalsIgnoreCase(STR_MARK_SPELLNAME)) {
			return MARK_REDSTONE_COST_AMT + " " + REDSTONE_NAME + (MARK_OTHER_COST_AMT>0 ? ", " + MARK_OTHER_COST_AMT + " " + MARK_OTHER_COST_NAME : "");
		} else if (s.equalsIgnoreCase(STR_RECALL_SPELLNAME)) {
			return RECALL_REDSTONE_COST_AMT + " " + REDSTONE_NAME;
		} else {
			//huh?
			return "";
		}
	}
	
	public void onDisconnect(Player player) {
		// resets the mark
		if (MARK_RESET_ON_DISCONNECT && !USE_HOMES) {
			marks.remove(player.getName());
		}
	}
}
