import java.util.HashSet;

public class TeachSpell extends Spell {
	
	MagicSpellsListener listener;
	
	// costs
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private int TEACH_RANGE;
	private String TEACH_TEACH_REQUIRED_RANK;
	
	// strings
	private String STR_TEACH_SPELLNAME;
	private String STR_USAGE;
	private String STR_ERR_TEACH_SELF;
	private String STR_ERR_CANT_CAST;
	private String STR_ERR_CANT_TEACH;
	private String STR_ERR_CANT_LEARN;
	private String STR_ERR_NO_TARGET;
	private String STR_ERR_OUT_OF_RANGE;
	private String STR_ERR_ALREADY_LEARNED;
	private String STR_SUCCESS_CASTER;
	private String STR_SUCCESS_TARGET;
	
	private String STR_FORGET_SPELLNAME;
	private String STR_FORGET_USAGE;
	private String STR_FORGET_CASTER;
	private String STR_FORGET_TARGET;
	private String STR_FORGET_FAIL;
	
	private String STR_SPELLBOOK_SPELLNAME;
	
	private int [][] reagents;
	
	public TeachSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener, properties);
		
		this.listener = listener;
		
		STR_TEACH_SPELLNAME = properties.getString("teach-spellname","teach");
		STR_FORGET_SPELLNAME = properties.getString("forget-spellname","forget");
		STR_SPELLBOOK_SPELLNAME = properties.getString("spellbook-spellname","spellbook");
		listener.registerSpellName(STR_TEACH_SPELLNAME,this,properties.getString("teach-desc","Teaches a player a spell"));
		listener.registerSpellName(STR_FORGET_SPELLNAME,this,properties.getString("forget-desc","Causes a player to forget a spell"));
		
		// get properties
		REDSTONE_COST = properties.getInt("teach-redstone-cost",0);
		OTHER_COST = properties.getInt("teach-other-cost-type",340);
		OTHER_COST_NAME = properties.getString("teach-other-cost-name","book");
		OTHER_COST_AMT = properties.getInt("teach-other-cost-amt",0);
		
		TEACH_RANGE = properties.getInt("teach-forget-range",10);
		TEACH_TEACH_REQUIRED_RANK = properties.getString("teach-req-rank-for-meta-spells","wizards");
		
		STR_USAGE = properties.getString("teach-usage-str","Usage: /cast teach [player] [spell]");
		STR_ERR_TEACH_SELF = properties.getString("teach-self-str","You cannot do that.");
		STR_ERR_CANT_CAST = properties.getString("teach-cant-learn-str","[target] cannot use magic spells.");
		STR_ERR_CANT_TEACH = properties.getString("teach-cant-teach-str","You do not know that spell.");
		STR_ERR_CANT_LEARN = properties.getString("teach-no-comm-perm-str","[target] cannot learn that spell.");
		STR_ERR_NO_TARGET = properties.getString("teach-no-target-str","Cannot find player.");
		STR_ERR_OUT_OF_RANGE = properties.getString("teach-out-of-range-str","That person is out of range.");
		STR_ERR_ALREADY_LEARNED = properties.getString("teach-already-learned-str","That person already knows that spell.");
		STR_SUCCESS_CASTER = properties.getString("teach-success-caster-str","You have taught [target] the '[spell]' spell.");
		STR_SUCCESS_TARGET = properties.getString("teach-success-target-str","[caster] has taught you the '[spell]' spell!");
		STR_FORGET_USAGE = properties.getString("forget-usage-sgr","Usage: /cast forget [player] [spell]");
		STR_FORGET_CASTER = properties.getString("forget-caster-str","[target] has forgotten the '[spell]' spell.");
		STR_FORGET_TARGET = properties.getString("forget-target-str","You have forgotten the '[spell]' spell.");
		STR_FORGET_FAIL = properties.getString("forget-fail-str","[target] cannot forget the '[spell]' spell.");
		
		// setup reagents
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
	}	
	
	public boolean cast(Player player, String [] command) {
			if (command.length != 4) {
				// no params
				if (command[1].equalsIgnoreCase(STR_TEACH_SPELLNAME)) {
					player.sendMessage(Spell.TEXT_COLOR + STR_USAGE);
				} else if (command[1].equalsIgnoreCase(STR_FORGET_SPELLNAME)) {
					player.sendMessage(Spell.TEXT_COLOR + STR_FORGET_USAGE);
				} 
			} else {
				// attempt to get player
				Player p = etc.getServer().matchPlayer(command[2]);
				String s = command[3].toLowerCase();
				if (p != null) {
					if (p.getName().equalsIgnoreCase(player.getName())) {
						// no teaching spells to yourself!
						player.sendMessage(Spell.TEXT_COLOR + STR_ERR_TEACH_SELF);
					} else if (!p.canUseCommand("/cast")) {
						// learner cannot use /cast, so cannot learn the spell
						player.sendMessage(Spell.TEXT_COLOR + STR_ERR_CANT_CAST.replace("[target]",p.getName()));
					} else if ((s.equalsIgnoreCase(STR_TEACH_SPELLNAME) || s.equalsIgnoreCase(STR_SPELLBOOK_SPELLNAME) || s.equalsIgnoreCase(STR_FORGET_SPELLNAME)) && !player.isInGroup(TEACH_TEACH_REQUIRED_RANK)) {
						// teacher is trying to teach the teach or spellbook spell, but can't
						player.sendMessage(TEXT_COLOR + STR_ERR_CANT_TEACH);
					} else if (TEACH_RANGE > 0 && !isPlayerInRange(player, p, TEACH_RANGE)) {
						// players not in range
						player.sendMessage(Spell.TEXT_COLOR + STR_ERR_OUT_OF_RANGE.replace("[target]",p.getName()));
					} else if (!listener.canCastSpell(player,s)) {
						// spell isn't real or teacher doesn't know it
						player.sendMessage(Spell.TEXT_COLOR + STR_ERR_CANT_TEACH);
					} else if (listener.LEARN_REQUIRES_PERM && !p.canUseCommand("/cast"+s)) {
						// target cannot learn that spell due to lacking permission
						player.sendMessage(Spell.TEXT_COLOR + STR_ERR_CANT_LEARN.replace("[target]",p.getName()));
					} else if (command[1].equalsIgnoreCase(STR_TEACH_SPELLNAME) && listener.canCastSpell(p,s)) {
						// target already knows the spell
						player.sendMessage(Spell.TEXT_COLOR + STR_ERR_ALREADY_LEARNED);
					} else if (!removeReagents(player,reagents)) {
						// missing reagents
						player.sendMessage(Spell.TEXT_COLOR + STR_NO_REAGENTS);
					} else {
						if (command[1].equalsIgnoreCase(STR_TEACH_SPELLNAME)) {
							// add spell to learner's learned spells
							if (listener.getLearnedSpells().containsKey(p.getName().toLowerCase())) {
								// learner already has a spell list, so add it
								listener.getLearnedSpells().get(p.getName().toLowerCase()).add(s);
							} else {
								// learner doesn't have a spell list - create one
								HashSet<String> h = new HashSet<String>();
								h.add(s);
								listener.getLearnedSpells().put(p.getName().toLowerCase(),h);
							}
							listener.saveLearnedSpells(p, s);
							// let players know of the teaching
							player.sendMessage(Spell.TEXT_COLOR + STR_SUCCESS_CASTER.replace("[target]",p.getName()).replace("[spell]",s));
							p.sendMessage(Spell.TEXT_COLOR + STR_SUCCESS_TARGET.replace("[caster]",player.getName()).replace("[spell]",s));
						} else if (command[1].equalsIgnoreCase(STR_FORGET_SPELLNAME)) {
							// remove spell from target's spells
							if (listener.getLearnedSpells().containsKey(p.getName().toLowerCase()) && listener.getLearnedSpells().get(p.getName().toLowerCase()).contains(s)) {
								// target knows spell, so remove it
								listener.getLearnedSpells().get(p.getName().toLowerCase()).remove(s);
								listener.saveLearnedSpells(p);
								player.sendMessage(TEXT_COLOR + STR_FORGET_CASTER.replace("[target]",p.getName()).replace("[spell]",s));
								p.sendMessage(TEXT_COLOR + STR_FORGET_TARGET.replace("[caster]",player.getName()).replace("[spell]",s));
							} else {
								// target doesn't know spell
								player.sendMessage(TEXT_COLOR + STR_FORGET_FAIL.replace("[target]",p.getName()).replace("[spell]",s));
							}
						}
					}
				} else {
					player.sendMessage(Spell.TEXT_COLOR + STR_ERR_NO_TARGET);
				}
			}
			return true;
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"");
	}
	
}
