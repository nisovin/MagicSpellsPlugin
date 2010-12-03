import java.util.HashMap;
import java.util.ArrayList;

public class WandSpell extends Spell {

	private HashMap<String,Integer> wanders;
	
	// cost
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	private int USES_PER_REDSTONE;
	
	// options
	private int WAND;
	private boolean GIVE_ITEM;
	private int RANGE;
	private boolean CHECK_PLUGINS;
	private ArrayList<Integer> EXCLUDED_ITEMS;
	private ArrayList<Integer> TRANSPARENT_ITEMS;
	
	// strings
	private String STR_ERR_NO_STICK;
	private String STR_ERR_EXCLUDED;
	private String STR_CAST_ON;
	private String STR_CAST_ON_OTHERS;
	private String STR_CAST_OFF;
	private String STR_CAST_OFF_OTHERS;
	
	private int [][] reagents;
	private int [][] useReagents;

	public WandSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("wand-spellname","wand"),this,properties.getString("wand-desc","Turns a stick into a block-destroying wand."));
		listener.requireArmSwingCall(this);
		
		// get properties
		REDSTONE_COST = properties.getInt("wand-redstone-cost",10);
		OTHER_COST = properties.getInt("wand-other-cost-type",0);
		OTHER_COST_NAME = properties.getString("wand-other-cost-name","");
		OTHER_COST_AMT = properties.getInt("wand-other-cost-amt",0);
		USES_PER_REDSTONE = properties.getInt("wand-uses-per-redstone",5);
		
		WAND = properties.getInt("wand-type",280);
		GIVE_ITEM = properties.getBoolean("wand-give-item",false);
		RANGE = properties.getInt("wand-range",20);
		CHECK_PLUGINS = properties.getBoolean("wand-plugin-check",true);
		String excluded = properties.getString("wand-excluded-items","7");
		EXCLUDED_ITEMS = new ArrayList<Integer>();
		for (String s : excluded.split(",")) {
			EXCLUDED_ITEMS.add(Integer.parseInt(s));
		}
		String transparent = properties.getString("wand-pass-thru-items","8,9,51,90");
		TRANSPARENT_ITEMS = new ArrayList<Integer>();
		for (String s : transparent.split(",")) {
			TRANSPARENT_ITEMS.add(Integer.parseInt(s));
		}
		
		STR_ERR_NO_STICK = properties.getString("wand-err-no-stick-str","You must be holding a stick to use that spell.");
		STR_ERR_EXCLUDED = properties.getString("wand-err-excluded-str","You cannot destroy that block.");
		STR_CAST_ON = properties.getString("wand-cast-on-str","Your stick glows red, and becomes a blasting wand!");
		STR_CAST_ON_OTHERS = properties.getString("wand-cast-on-others-str","[caster]'s stick glows red, becoming a wand!");
		STR_CAST_OFF = properties.getString("wand-cast-off-str","Your wand turns back into a stick.");
		STR_CAST_OFF_OTHERS = properties.getString("wand-cast-off-others-str","[caster]'s wand turns back into a stick.");
		
		// prepare data
		wanders = new HashMap<String,Integer>();
		
		// prepare reagents
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
		useReagents = new int [][] {{REDSTONE_DUST,1}};
	}

	public boolean cast(Player player, String [] command) {
		if (!wanders.containsKey(player.getName())) {
			if (player.getItemInHand() != WAND) {
				player.sendMessage(TEXT_COLOR + STR_ERR_NO_STICK);
				return false;
			} else if (removeReagents(player,reagents)) {
				wanders.put(player.getName(),0);
				player.sendMessage(TEXT_COLOR + STR_CAST_ON);
				sendMessageToPlayersInRange(player,STR_CAST_ON_OTHERS.replace("[caster]",player.getName()));
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
	
	private void turnOff(Player player) {
		wanders.remove(player.getName());
		player.sendMessage(TEXT_COLOR + STR_CAST_OFF);
		sendMessageToPlayersInRange(player,STR_CAST_OFF_OTHERS.replace("[caster]",player.getName()));	
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", " + OTHER_COST_AMT + " " + OTHER_COST_NAME:"") + (USES_PER_REDSTONE>0?"@plus 1 " + REDSTONE_NAME + " per " + USES_PER_REDSTONE + " uses":"");
	}
	
	public void onArmSwing(Player player) {
		if (player.getItemInHand() == WAND && wanders.containsKey(player.getName())) {
			HitBlox hit = new HitBlox(player);
			Block target = hit.getTargetBlock();
			
			// pass through transparencies
			while (target != null && TRANSPARENT_ITEMS.contains(target.getType())) {
				target = hit.getNextBlock();
			}
			
			if (target != null && (RANGE == 0 || getDistance(player,target) <= RANGE)) {
				if (EXCLUDED_ITEMS.contains(target.getType())) {
					player.sendMessage(TEXT_COLOR + STR_ERR_EXCLUDED);
				} else if (!CHECK_PLUGINS || !(Boolean)etc.getLoader().callHook(PluginLoader.Hook.BLOCK_DESTROYED, new Object [] {player.getUser(), target})) {
					if (GIVE_ITEM) {
						player.giveItem(new Item(target.getType(),1));
					}
					target.setType(0);
					target.update();
				
					// add use
					if (USES_PER_REDSTONE > 0) {
						int uses = wanders.get(player.getName()) + 1;
						if (uses > USES_PER_REDSTONE) {
							if (removeReagents(player,useReagents)) {
								wanders.put(player.getName(),0);
							} else {
								turnOff(player);
							}
						} else {
							wanders.put(player.getName(),uses);
						}
					}
				}
			}
		}
	}
	
	public double getDistance(Player player, Block target) {
		return Math.sqrt(Math.pow(player.getX()-target.getX(),2) + Math.pow(player.getY()-target.getY(),2) + Math.pow(player.getZ()-target.getZ(),2));
	}

}
