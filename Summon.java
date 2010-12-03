import java.util.ArrayList;

public class Summon {
	public static ArrayList<Summon> pending = new ArrayList<Summon>();
	public static int allowedDelay = 60000;

	private Player player;
	private int x;
	private int y;
	private int z;
	private long created;
	
	public Summon(Player player, Block target) {
		this.player = player;
		this.x = target.getX();
		this.y = target.getY();
		this.z = target.getZ();
		this.created = System.currentTimeMillis();
	}
	
	public static boolean createSummon(Player player, Block block) {
		if (getPendingSummon(player) != null) {
			return false;
		} else if (summonTargetInUse(block)) {
			return false;
		} else {
			pending.add(new Summon(player, block));
			return true;
		}
	}
	
	public static Summon getPendingSummon(Player player) {
		for (int i = 0; i < pending.size(); i++) {
			Summon s = pending.get(i);
			if (s.player.getName().equalsIgnoreCase(player.getName())) {
				if (s.created + allowedDelay > System.currentTimeMillis()) {
					return s;
				} else {
					pending.remove(i);
					i--;
				}
			}
		}
		return null;
	}
	
	public void activate() {
		player.teleportTo((double)x, (double)y, (double)z, player.getRotation(), player.getPitch());
		pending.remove(this);
	}
	
	public static boolean summonTargetInUse(Block block) {
		for (int i = 0; i < pending.size(); i++) {
			Summon s = pending.get(i);
			if (s.x == block.getX() && s.y == block.getY() && s.z == block.getZ()) {
				if (s.created + allowedDelay > System.currentTimeMillis()) {
					return true;
				} else {
					pending.remove(i);
					return false;
				}
			}
		}
		return false;
	}
}
