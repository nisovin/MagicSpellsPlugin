import java.util.HashMap;
import java.util.HashSet;

public abstract class Rune {
	
	public static HashMap<Integer,String> blockCodes;
	public static HashMap<Integer,String> blockNames;

	private int [][] rune;

	Rune() {
	}
	
	Rune(MagicSpellsListener listener, PropertiesFile properties) {	
	}
	
	abstract void activate(Player player, Block center);
	
	abstract String getName();
	
	abstract String getDesc();
	
	void setRune(int [][] rune) {
		this.rune = rune;
	}
	
	void setRune(String r) {
		rune = new int [5][5];
		String [] rows = r.split(";");
		for (int i = 0; i < rows.length; i++) {
			String [] cols = rows[i].split(",");
			for (int j = 0; j < cols.length; j++) {
				rune[i][j] = Integer.parseInt(cols[j]);
			}
		}
	}
	
	int [][] getRune() {
		return rune;
	}
	
	void describeRune(Player player) {
		player.sendMessage(Spell.TEXT_COLOR + "Rune: " + getName());
		
		HashSet<Integer> usedCodes = new HashSet<Integer>();
		for (int i = 0; i < 5; i++) {
			String s = "";
			for (int j = 0; j < 5; j++) {
				usedCodes.add(rune[i][j]);
				s += blockCodes.get(rune[i][j]);
				if (j != 4) s += ".";
			}
			player.sendMessage(Spell.TEXT_COLOR + s);
		}
		String s = "";
		int c = 0;
		for (Integer i : usedCodes) {
			s += blockCodes.get(i) + " = " + blockNames.get(i) + ", ";
			c++;
			if (c == 3) {
				s = s.substring(0,s.length()-2);
				player.sendMessage(Spell.TEXT_COLOR + s);	
				c = 0;
				s = "";
			}
		}
		if (c > 0) {
			s = s.substring(0,s.length()-2);
			player.sendMessage(Spell.TEXT_COLOR + s);
		}
	}
	
	abstract boolean needsLearned();
	
	void destroyRune(Block center) {
		Server s = etc.getServer();
		int i = 0, j = 0;
		for (int x = center.getX() - 2; x <= center.getX() + 2; x++) {
			i = 0;
			for (int z = center.getZ() - 2; z <= center.getZ() + 2; z++) {
				if (rune[i][j] != -1 && rune[i][j] == s.getBlockIdAt(x,center.getY(),z)) {
					s.setBlockAt(0,x,center.getY(),z);
				}
				i++;
			}
			j++;
		}
	}
}
