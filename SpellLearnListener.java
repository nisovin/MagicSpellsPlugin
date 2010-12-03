import java.util.HashSet;

public class SpellLearnListener implements PluginInterface {

	MagicSpellsListener listener;
	
	public SpellLearnListener(MagicSpellsListener l) {
		listener = l;
	}

	public String getName() {
		return "MagicSpellLearn";
	}
	
	public int getNumParameters() {
		return 2;
	}
	
	public String checkParameters(Object [] params) {
		if (params.length != 2) {
			return "Must use parameters (Player, String)";
		} else if (params[0].getClass() != Player.class || params[1].getClass() != String.class) {
			return "Must use parameters (Player, String)";
		} else {
			return null;
		}
	}
	
	public Object run(Object [] params) {
		Player player = (Player)params[0];
		String spell = (String)params[1];
		
		if (!player.canUseCommand("/cast")) {
			// player can't cast
			return new Boolean(false);
		} else if (listener.LEARN_REQUIRES_PERM && !player.canUseCommand("/cast"+spell)) {
			// target cannot learn that spell due to lacking permission
			return new Boolean(false);
		} else if (listener.canCastSpell(player,spell)) {
			// target already knows the spell
			return new Boolean(false);
		}  else {
			// add spell to learner's learned spells
			if (listener.getLearnedSpells().containsKey(player.getName().toLowerCase())) {
				// learner already has a spell list, so add it
				listener.getLearnedSpells().get(player.getName().toLowerCase()).add(spell);
			} else {
				// learner doesn't have a spell list - create one
				HashSet<String> h = new HashSet<String>();
				h.add(spell);
				listener.getLearnedSpells().put(player.getName().toLowerCase(),h);
			}
			listener.saveLearnedSpells(player, spell);
			return new Boolean(true);
		}
	}
}
