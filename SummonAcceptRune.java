import java.util.HashMap;

public class SummonAcceptRune extends Rune {
		
	private String name;
	private String desc;
	private boolean needsLearned;
	
	private String STR_ERR_NO_SUMMON;
	private String STR_SUCCESS;
	
	public SummonAcceptRune(MagicSpellsListener listener, PropertiesFile properties) {
		name = properties.getString("runes-summon-accept-name","summon-accept");
		desc = properties.getString("runes-summon-accept-desc","Accepts a summons from a summon rune.");
		needsLearned = properties.getBoolean("runes-summon-accept-needs-learned",true);
		String rune = properties.getString("runes-summon-accept-rune","-1,-1,-1,-1,-1;-1,-1,17,-1,-1;-1,17,1,17,-1;-1,-1,17,-1,-1;-1,-1,-1,-1,-1");
		setRune(rune);
		
		STR_ERR_NO_SUMMON = properties.getString("runes-summon-accept-no-summon-str","Nothing happens.");
		STR_SUCCESS = properties.getString("runes-summon-accept-success-str","You have been summoned!");
	}
	
	public void activate(Player player, Block center) {
		Summon summon = Summon.getPendingSummon(player);
		if (summon == null) {
			player.sendMessage(Spell.TEXT_COLOR + STR_ERR_NO_SUMMON);
		} else {
			destroyRune(center);
			summon.activate();
			player.sendMessage(Spell.TEXT_COLOR + STR_SUCCESS);
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
