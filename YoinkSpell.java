import java.util.HashMap;

public class YoinkSpell extends Spell {

	// spell cost
	private int COST_TYPE;
	private String COST_NAME;
	private int COST_AMT;

	// options
	private int YOINK_RANGE;
	private int COOLDOWN;
	
	// strings
	private String STR_ERR_USAGE;
	private String STR_ERR_NO_TARGET;
	private String STR_ERR_TOO_FAR;
	private String STR_ERR_NO_ITEM;
	private String STR_SUCCESS_CASTER;
	private String STR_SUCCESS_TARGET;
	private String STR_SUCCESS_OTHERS;
	
	// reagents
	int [][] reagents;
	
	public YoinkSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener, properties);
		
		// register spell
		listener.registerSpellName(properties.getString("yoink-spellname","yoink"),this,properties.getString("yoink-desc","Steals the item another player is holding."));
		
		// get properties
		COST_TYPE = properties.getInt("yoink-cost-type",REDSTONE_DUST);
		COST_NAME = properties.getString("yoink-cost-name",REDSTONE_NAME);
		COST_AMT = properties.getInt("yoink-cost-amt",5);
		
		YOINK_RANGE = properties.getInt("yoink-range",5);
		COOLDOWN = properties.getInt("yoink-cooldown-seconds",120);
		
		STR_ERR_USAGE = properties.getString("yoink-usage-str","Usage: /cast yoink [playername]");
		STR_ERR_NO_TARGET = properties.getString("yoink-err-notarget-str","Unable to find that person.");
		STR_ERR_TOO_FAR = properties.getString("yoink-err-too-far-str","That person is too far away!");
		STR_ERR_NO_ITEM = properties.getString("yoink-err-no-item-str","That person isn't holding anything.");
		STR_SUCCESS_CASTER = properties.getString("yoink-caster-str","You yoink [target]'s item!");
		STR_SUCCESS_TARGET = properties.getString("yoink-target-str","[caster] yoinked your item!");
		STR_SUCCESS_OTHERS = properties.getString("yoink-others-str","[caster] yoinked [target]'s item!");
		
		// setup reagents
		reagents = new int [][] {{COST_TYPE,COST_AMT}};
		
		// setup cooldown
		if (COOLDOWN > 0) {
			enableCooldown();
		}
	}
	
	public boolean cast(Player player, String [] command) {
		if (command.length != 3) {
			// invalid usage
			player.sendMessage(TEXT_COLOR + STR_ERR_USAGE);
			return false;
		} else {
			Player target = etc.getServer().matchPlayer(command[2]);
			if (target == null) {
				// no target
				player.sendMessage(TEXT_COLOR + STR_ERR_NO_TARGET);
				return false;
			} else if (!isPlayerInRange(player, target, YOINK_RANGE)) {
				// too far away
				player.sendMessage(TEXT_COLOR + STR_ERR_TOO_FAR);
				return false;
			} else if (target.getItemInHand() <= 0) {
				// target isn't holding anything
				player.sendMessage(TEXT_COLOR + STR_ERR_NO_ITEM);
				return false;
			} else if (isOnCooldown(player, COOLDOWN)) {
				// on cooldown
				player.sendMessage(TEXT_COLOR + STR_ON_COOLDOWN);
				return false;
			} else if (!removeReagents(player, reagents)) {
				// no reagents
				player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
				return false;
			} else {
				// transfer item
				target.getInventory().removeItem(new Item(target.getItemInHand(),1));
				target.getInventory().updateInventory();
				player.giveItem(target.getItemInHand(),1);
				// send messages
				target.sendMessage(TEXT_COLOR + STR_SUCCESS_TARGET.replace("[caster]",player.getName()));
				player.sendMessage(TEXT_COLOR + STR_SUCCESS_CASTER.replace("[target]",target.getName()));
				sendMessageToPlayersInRange(player,STR_SUCCESS_OTHERS.replace("[caster]",player.getName()).replace("[target]",player.getName()),15,target.getName());
				// start cooldown
				startCooldown(player);
				return true;
			}
		}
	}
	
	public String getCostDesc(String s) {
		return COST_AMT + " " + COST_NAME;
	}
	
}
