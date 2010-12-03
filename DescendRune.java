public class DescendRune extends Rune {
		
	private String name;
	private String desc;
	private boolean needsLearned;
		
	public DescendRune(MagicSpellsListener listener, PropertiesFile properties) {
		name = properties.getString("runes-descend-name","descend");
		desc = properties.getString("runes-descend-desc","Causes you to move down to a lower location.");
		needsLearned = properties.getBoolean("runes-descend-needs-learned",false);
		String rune = properties.getString("runes-descend-rune","1,20,1,20,1;20,43,44,43,20;1,44,44,44,1;20,43,44,43,20;1,20,1,20,1");
		setRune(rune);
	}
	
	public void activate(Player player, Block center) {
		Location loc = AscendAndDescendSpell.findPlaceToStand(player, "down");
		if (loc != null) {
			player.teleportTo(loc);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public boolean needsLearned() {
		return needsLearned;
	}
}
