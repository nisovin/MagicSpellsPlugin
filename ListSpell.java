import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

public class ListSpell extends Spell {

	MagicSpellsListener listener;
	
	// options
	private int SPELLS_PER_PAGE;
	
	// strings
	private String STR_NO_SPELLS;
	private String STR_LIST_HEADER;

	public ListSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		this.listener = listener;
		
		// register spell
		listener.registerSpellName(properties.getString("list-spellname","list"),this,properties.getString("list-desc","Lists your learned spells"));
		
		// get properties
		SPELLS_PER_PAGE = properties.getInt("list-spells-per-page",5);
		STR_NO_SPELLS = properties.getString("list-no-spells-str","You do not know any spells.");
		STR_LIST_HEADER = properties.getString("list-header-str","Spells - page [page] of [total]");
	}
	
	public boolean cast(Player player, String [] command) {
		Hashtable<String,String> spellDescs = listener.getSpellDescs();
		
		// get list of learned spells
		ArrayList<String> spellList = new ArrayList<String>();
		for (String s : spellDescs.keySet()) {
			if (listener.canCastSpell(player,s)) {
				spellList.add(s);
			}
		}
		Collections.sort(spellList);
		if (spellList.size() == 0) {
			// player has no spells learned
			player.sendMessage(Spell.TEXT_COLOR + STR_NO_SPELLS);
		} else {
			// get start index
			int start = 0;
			if (command.length > 2 && (Integer.parseInt(command[2])-1) <= spellList.size() / SPELLS_PER_PAGE) {
				start = (Integer.parseInt(command[2])-1) * SPELLS_PER_PAGE;
			}
			// display spells
			player.sendMessage(Spell.TEXT_COLOR + STR_LIST_HEADER.replace("[page]",""+((start/5)+1)).replace("[total]",""+(int)Math.ceil(spellList.size()/(float)SPELLS_PER_PAGE)));
			for (int i = start; i < start+SPELLS_PER_PAGE && i < spellList.size(); i++) {
				player.sendMessage(Spell.TEXT_COLOR + spellList.get(i) + " - " + spellDescs.get(spellList.get(i)));
			}
		}
		
		return true;
	}
	
	public String getCostDesc(String s) {
		return "free";
	}

	boolean needsLearned() {
		return false;
	}

}
