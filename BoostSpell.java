import java.util.HashSet;

public class BoostSpell extends Spell {
	
	// cost
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private double SPEED;
	private int COOLDOWN;
	
	// strings
	private String STR_CAST_CASTER;
	private String STR_CAST_OTHERS;
	private String STR_ERR_NO_VEHICLE;
	
	private int [][] reagents;

	public BoostSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		listener.registerSpellName(properties.getString("boost-spellname","boost"),this,properties.getString("boost-desc","Gives your vehicle a speed boost."));
		
		// get properties
		REDSTONE_COST = properties.getInt("boost-redstone-cost",10);
		OTHER_COST = properties.getInt("boost-other-cost-type",263);
		OTHER_COST_NAME = properties.getString("boost-other-cost-name","coal");
		OTHER_COST_AMT = properties.getInt("boost-other-cost-amt",1);
		
		SPEED = properties.getInt("boost-speed",1000) / 1000.0;
		COOLDOWN = properties.getInt("boost-cooldown-seconds",60);
		
		STR_CAST_CASTER = properties.getString("boost-cast-caster-str","You get a burst of speed!");
		STR_CAST_OTHERS = properties.getString("boost-cast-others-str","[caster] gets a burst of speed!");
		STR_ERR_NO_VEHICLE = properties.getString("boost-err-no-vehicle-str","You are not in a vehicle.");
		
		if (COOLDOWN > 0) {
			enableCooldown();
		}
		
		// prepare reagents
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
	}

	public boolean cast(Player player, String [] command) {
	
		/*BaseVehicle vehicle = null;
		for (BaseVehicle v : etc.getServer().getVehicleEntityList()) {
			if (v.getPassenger() != null && v.getPassenger().getName().equals(player.getName())) {
				vehicle = v;
				break;
			}
		}
		
		if (vehicle == null) {
			player.sendMessage(TEXT_COLOR + STR_ERR_NO_VEHICLE);
			return false;
		} else if (COOLDOWN > 0 && isOnCooldown(player,COOLDOWN)) {
			player.sendMessage(TEXT_COLOR + STR_ON_COOLDOWN);
			return false;
		} else if (!removeReagents(player,reagents)) {
			player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
			return false;
		} else {
			boolean changed = false;
			
			if (changed) {
				//vehicle.setMotion(mx,my,mz);
			} else {
				if (vehicle instanceof Minecart) {
					float rot = ((player.getRotation() - 90) % 360);
					if (rot < 0) rot += 360;
					String dir = etc.getCompassPointForDirection(rot);
					if (dir.equals("N")) {
						vehicle.setRotation(90);
						vehicle.setMotion(SPEED, 0.0, 0.0);
					} else if (dir.equals("S")) {
						vehicle.setRotation(270);
						vehicle.setMotion(SPEED, 0.0, 0.0);
					} else if (dir.equals("E")) {
						vehicle.setRotation(180);
						vehicle.setMotion(0.0, 0.0, SPEED);
					} else if (dir.equals("W")) {
						vehicle.setRotation(0);
						vehicle.setMotion(0.0, 0.0, SPEED);
					}
				} else if (vehicle instanceof Boat) {
					double sx = Math.abs(Math.cos(player.getRotation()) * SPEED) * -1;
					double sz = Math.abs(Math.sin(player.getRotation()) * SPEED) * -1;
					
					vehicle.setRotation(player.getRotation());
					vehicle.setMotion(sx, 0.0, sz);
				}
			}
			
			return true;
		}*/
		return false;
	}
	
	public double getDistance(Player player, Block target) {
		return Math.sqrt(Math.pow(player.getX()-target.getX(),2) + Math.pow(player.getY()-target.getY(),2) + Math.pow(player.getZ()-target.getZ(),2));
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", " + OTHER_COST_AMT + " " + OTHER_COST_NAME:"");
	}
	

}
