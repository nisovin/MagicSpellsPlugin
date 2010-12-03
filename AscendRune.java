public class AscendRune extends Rune {
		
	private String name;
	private String desc;
	private boolean needsLearned;
		
	public AscendRune(MagicSpellsListener listener, PropertiesFile properties) {
		name = properties.getString("runes-ascend-name","ascend");
		desc = properties.getString("runes-ascend-desc","Causes you to move up to a higher location.");
		needsLearned = properties.getBoolean("runes-ascend-needs-learned",false);
		String rune = properties.getString("runes-ascend-rune","1,20,1,20,1;20,44,43,44,20;1,43,43,43,1;20,44,43,44,20;1,20,1,20,1");
		setRune(rune);
	}
	
	public void activate(Player player, Block center) {
		Location loc = AscendAndDescendSpell.findPlaceToStand(player, "up");
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
