public class SkyRune extends Rune {

	private String name;
	private boolean needsLearned;
		 
	public SkyRune(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener, properties);
		
		name = properties.getString("runes-sky-name","sky");
		needsLearned = properties.getBoolean("runes-sky-needs-learned",false);
		String rune = properties.getString("runes-sky-rune","-1,-1,-1,-1,-1;-1,-1,1,-1,-1;-1,1,1,1,-1;-1,-1,1,-1,-1;-1,-1,-1,-1,-1");
		setRune(rune);
	}
		 
	public void activate(Player player, Block center) {
		player.sendMessage("Woohoo!");
		destroyRune(center);
		player.setY(127);
	}
	
	public String getName() {
		return name;
	}
	
	public String getDesc() {
		return "";
	}
	
	public boolean needsLearned() {
		return needsLearned;
	}

}
