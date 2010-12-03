import java.util.ArrayList;

public class MineshaftRune extends Rune {
		
	private String name;
	private String desc;
	private boolean needsLearned;
		
	ArrayList<Integer> natural = new ArrayList<Integer>();
		
	public MineshaftRune(MagicSpellsListener listener, PropertiesFile properties) {
		name = properties.getString("runes-mineshaft-name","mineshaft");
		desc = properties.getString("runes-mineshaft-desc","Creates a mineshaft.");
		needsLearned = properties.getBoolean("runes-mineshaft-needs-learned",true);
		String rune = properties.getString("runes-mineshaft-rune","43,-1,-1,-1,43;-1,4,44,4,-1;-1,44,1,44,-1;-1,4,44,4,-1;43,-1,-1,-1,43");
		setRune(rune);
		
		String passThrough = properties.getString("runes-mineshaft-pass-through-types","0,1,2,3,8,9,10,11,12,13,14,15,16,56,73,74,87,88,89");
		String [] types = passThrough.split(",");
		for (int i = 0; i < types.length; i++) {
			natural.add(Integer.parseInt(types[i]));
		}		
	}
	
	public void activate(Player player, Block center) {
		player.sendMessage("yay!");
		
		Server s = etc.getServer();
		int x = center.getX();
		int y = center.getY();
		int z = center.getZ();
		
		destroyRune(center);
		
		while (--y > 0) {
			// validate blocks
			boolean ok = true;
			for (int i = x-1; i <= x+1; i++) {
				for (int j = z-1; j <= z+1; j++) {
					if (!natural.contains(s.getBlockIdAt(i,y,j))) {
						ok = false;
					}
				}
			}
			
			
			if (ok) {
				// destroy blocks
				for (int i = x-1; i <= x+1; i++) {
					for (int j = z-1; j <= z+1; j++) {
						s.setBlockAt(0,i,y,j);
					}
				}
				
				// create stair
				s.setBlockAt(1,x,y,z);
				if (y % 4 == 3) {
					s.setBlockAt(43,x-1,y,z-1);
					s.setBlockAt(44,x,y,z-1);
				} else if (y % 4 == 2) {
					s.setBlockAt(43,x+1,y,z-1);
					s.setBlockAt(44,x+1,y,z);
				} else if (y % 4 == 1) {
					s.setBlockAt(43,x+1,y,z+1);
					s.setBlockAt(44,x,y,z+1);
				} else {
					s.setBlockAt(43,x-1,y,z+1);
					s.setBlockAt(44,x-1,y,z);
				}
			} else {
				break;
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
