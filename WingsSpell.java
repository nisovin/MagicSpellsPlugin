import java.util.Hashtable;

public class WingsSpell extends Spell
{
	private Hashtable<String,Integer> flyingPpl = new Hashtable<String,Integer>();
	
	// cost amounts
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	private int PERIODIC_COST;
	private String PERIODIC_COST_NAME;
	private int PERIODIC_COST_INTERVAL;	
	
	// options
	private int CRUISE_LEVEL;
	private int FLY_OBJECT;
	
	// strings
	private String STR_CAST_FLY;
	private String STR_CAST_FLY_OTHERS;
	private String STR_STOP_FLY;
	private String STR_STOP_FLY_OTHERS;

	private int [][] initialReagents;
	private int [][] periodicReagents;	
	
	public WingsSpell(MagicSpellsListener listener, PropertiesFile properties) {
		// register spell
		listener.registerSpellName(properties.getString("wings-spellname","wings"),this,properties.getString("wings-desc","Allows you to fly"));
		listener.requireArmSwingCall(this);
		
		// get properties
		REDSTONE_COST = properties.getInt("wings-redstone-cost",15);
		OTHER_COST = properties.getInt("wings-other-cost-type",288);
		OTHER_COST_NAME = properties.getString("wings-other-cost-name","feather");
		OTHER_COST_AMT = properties.getInt("wings-other-cost-amt",1);
		PERIODIC_COST = properties.getInt("wings-periodic-cost-type",REDSTONE_DUST);
		PERIODIC_COST_NAME = properties.getString("wings-periodic-cost-name",REDSTONE_NAME);
		PERIODIC_COST_INTERVAL = properties.getInt("wings-periodic-cost-interval",3);
		
		CRUISE_LEVEL = properties.getInt("wings-cruise-level",110);
		FLY_OBJECT = properties.getInt("wings-fly-object",288);
		
		STR_CAST_FLY = properties.getString("wings-cast-str","You feel as light as a feather.");
		STR_CAST_FLY_OTHERS = properties.getString("wings-cast-others-str","[caster] seems to float with the wind.");
		STR_STOP_FLY = properties.getString("wings-stop-str","You feel your weight return to your body.");
		STR_STOP_FLY_OTHERS = properties.getString("wings-stop-others-str","[caster] seems to settle to the ground.");
		
		// set up reagents
		initialReagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
		periodicReagents = new int [][] {{PERIODIC_COST,1}};
	}

	public boolean cast(Player player, String [] command) {
		String playerName = player.getName();
		
		if (!flyingPpl.containsKey(playerName)) {
			if (removeReagents(player,initialReagents)) {
				flyingPpl.put(playerName,0);
				player.sendMessage(TEXT_COLOR + STR_CAST_FLY);
				sendMessageToPlayersInRange(player,STR_CAST_FLY_OTHERS.replace("[caster]",player.getName()));
				return true;
			} else {
				player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
				return false;
			}
		} else {
			flyingPpl.remove(playerName);
			player.sendMessage(TEXT_COLOR + STR_STOP_FLY);
			sendMessageToPlayersInRange(player,STR_STOP_FLY_OTHERS.replace("[caster]",player.getName()));
			return true;
		}
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"") + (PERIODIC_COST>0?"@plus 1 "+PERIODIC_COST_NAME+" every " + PERIODIC_COST_INTERVAL + " flaps":"");
	}

	public void onArmSwing(Player player) {
		String playerName = player.getName();
		if (flyingPpl.containsKey(playerName) && player.getItemInHand() == FLY_OBJECT) {
			double cProt = player.getRotation() % 360.0F;
			if (cProt > 0.0D) {
				cProt -= 720.0D;
			}

			if (flyingPpl.get(playerName) < PERIODIC_COST_INTERVAL) {
				flyingPpl.put(playerName,flyingPpl.get(playerName)+1);
			} else if (removeReagents(player,periodicReagents)) {
				flyingPpl.put(playerName,0);
			} else {
				flyingPpl.remove(playerName);
				player.sendMessage(TEXT_COLOR + STR_STOP_FLY);
				sendMessageToPlayersInRange(player,STR_STOP_FLY_OTHERS);
				return;
			}
			
			double pRot = Math.abs(cProt % 360.0D);
			double pX = 0.0D;
			double pZ = 0.0D;
			double pY = 0.0D;
			double pPit = player.getPitch();
			double pyY = 0.0D;

			if ((pPit < 21.0D) && (pPit > -21.0D)) {
				pX = Math.sin(Math.toRadians(pRot)) * 10.0D;
				pZ = Math.cos(Math.toRadians(pRot)) * 10.0D;
				if ((player.getY() > CRUISE_LEVEL) && (player.getY() <= CRUISE_LEVEL + 5))
					pY = 1.0D;
				else if (player.getY() > CRUISE_LEVEL + 5)
					pY = 0.0D;
				else if (player.getY() < CRUISE_LEVEL)
					pY = 2.5D;
			} else {
				pX = Math.sin(Math.toRadians(pRot)) * 6.0D;
				pZ = Math.cos(Math.toRadians(pRot)) * 6.0D;
				if (pPit < 0.0D) {
					pY = Math.sin(Math.toRadians(Math.abs(pPit))) * 10.0D;
					pyY = Math.cos(Math.toRadians(Math.abs(pPit))) * 10.0D;
					pX = Math.sin(Math.toRadians(pRot)) * pyY;
					pZ = Math.cos(Math.toRadians(pRot)) * pyY;
				} else if ((pPit > 0.0D) && (pPit < 30.0D)) {
					pY = 4.0D;
					pX = Math.sin(Math.toRadians(pRot)) * 6.0D;
					pZ = Math.cos(Math.toRadians(pRot)) * 6.0D;
				} else if ((pPit >= 30.0D) && (pPit < 60.0D)) {
					pY = 5.0D;
					pX = Math.sin(Math.toRadians(pRot)) * 3.0D;
					pZ = Math.cos(Math.toRadians(pRot)) * 3.0D;
				} else if ((pPit >= 60.0D) && (pPit < 75.0D)) {
					pY = 6.0D;
					pX = Math.sin(Math.toRadians(pRot)) * 1.5D;
					pZ = Math.cos(Math.toRadians(pRot)) * 1.5D;
				} else if (pPit >= 75.0D) {
					pY = 8.0D;
					pX = 0.0D;
					pZ = 0.0D;
				}
			}

			player.setY(player.getY() + pY);
			player.setX(player.getX() + pX);
			player.setZ(player.getZ() + pZ);
		}
	}

}
