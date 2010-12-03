import java.util.HashMap;

public class SummonRequestRune extends Rune {
		
	private String name;
	private String desc;
	private boolean needsLearned;
	
	private String STR_ERR_NO_SIGN;
	private String STR_ERR_NO_TARGET;
	private String STR_SUCCESS;
	private String STR_SUMMON_WAITING;
	
	public SummonRequestRune(MagicSpellsListener listener, PropertiesFile properties) {
		name = properties.getString("runes-summon-request-name","summon");
		desc = properties.getString("runes-summon-request-desc","Summons another player to your location.");
		needsLearned = properties.getBoolean("runes-summon-request-needs-learned",true);
		String rune = properties.getString("runes-summon-request-rune","0,4,4,4,0;4,17,4,17,4;4,4,1,4,4;4,17,4,17,4;0,4,4,4,0");
		setRune(rune);
		
		Summon.allowedDelay = properties.getInt("runes-summon-allowed-delay",90) * 1000;
		
		STR_ERR_NO_SIGN = properties.getString("runes-summon-request-no-sign-str","You must put a sign with the target's name on the center block.");
		STR_ERR_NO_TARGET = properties.getString("runes-summon-request-no-target-str","That person could not be found.");
		STR_SUCCESS = properties.getString("runes-summon-request-success-str","The summons for [target] was sent.");
		STR_SUMMON_WAITING = properties.getString("runes-summon-request-waiting-str","You feel a pull...[caster] wants to summon you.");
	}
	
	public void activate(Player player, Block center) {
		Sign sign = (Sign)etc.getServer().getComplexBlock(center.getX(), center.getY()+1, center.getZ());
		if (sign == null) {
			// no sign
			player.sendMessage(Spell.TEXT_COLOR + STR_ERR_NO_SIGN);
		} else {
			String n = sign.getText(0);
			Player p = etc.getServer().matchPlayer(n);
			if (p == null) {
				// no player
				player.sendMessage(Spell.TEXT_COLOR + STR_ERR_NO_TARGET);
			} else if (Summon.createSummon(p, center)) {
				destroyRune(center);
				player.sendMessage(Spell.TEXT_COLOR + STR_SUCCESS.replace("[target]",p.getName()));
				p.sendMessage(Spell.TEXT_COLOR + STR_SUMMON_WAITING.replace("[caster]",player.getName()));
			}
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
