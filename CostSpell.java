public class CostSpell extends Spell {

	MagicSpellsListener listener;

	// strings
	private String STR_USAGE;
	private String STR_NO_SPELL;
	private String STR_COST_PREFIX;

	public CostSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener, properties);
		
		this.listener = listener;
		
		listener.registerSpellName(properties.getString("cost-spellname","cost"),this,properties.getString("cost-desc","Shows the reagent cost of a spell"));
		
		// get properties
		STR_USAGE = properties.getString("cost-usage-str","Use /cast cost [spell] to check a spell's reagent cost.");
		STR_NO_SPELL = properties.getString("cost-invalid-spell-str","You do not know that spell.");
		STR_COST_PREFIX = properties.getString("cost-prefix-str","[spell] reagent cost:");
	}
	
	public boolean cast(Player player, String [] command) {
		if (command.length != 3) {
			player.sendMessage(Spell.TEXT_COLOR + STR_USAGE);
		} else if (!listener.canCastSpell(player,command[2])) {
			player.sendMessage(Spell.TEXT_COLOR + STR_NO_SPELL);
		} else {
			String [] costs = listener.getSpellNames().get(command[2]).getCostDesc(command[2]).split("@");
			for (int i = 0; i < costs.length; i++) {
				if (!costs[i].equals("")) {
					player.sendMessage(Spell.TEXT_COLOR + (i==0?STR_COST_PREFIX.replace("[spell]",command[2])+" ":"    ") + costs[i]);
				}
			}
		}
		return true;
	}
	
	public boolean needsLearned() {
		return false;
	}
	
	public String getCostDesc(String s) {
		return "free";
	}

}
