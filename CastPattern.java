import java.util.ArrayList;

public class CastPattern {
	
	public int MOVE_DIST = 8;

	private ArrayList<Boolean> up;
	private ArrayList<Boolean> down;
	private ArrayList<Boolean> left;
	private ArrayList<Boolean> right;
	
	double lastRot = 0, lastPit = 0;
	
	public CastPattern() {		
		up = new ArrayList<Boolean>();
		down = new ArrayList<Boolean>();
		left = new ArrayList<Boolean>();
		right = new ArrayList<Boolean>();
	}
	
	public CastPattern(String s) {
		this();
		
		String [] movements = s.split(",");
		
		for (int i = 0; i < movements.length; i++) {
			up.add(false);
			down.add(false);
			left.add(false);
			right.add(false);
			if (movements[i].contains("U")) {
				up.set(i, true);
			} else if (movements[i].contains("D")) {
				down.set(i, true);
			}
			if (movements[i].contains("L")) {
				left.set(i, true);
			} else if (movements[i].contains("R")) {
				right.set(i, true);
			}
		}
	}
	
	public void addMovement(double rot, double pit) {
		if (up.size() == 0 && lastRot == 0 && lastPit == 0) {
			lastRot = rot;
			lastPit = pit;
		} else {
			if (Math.abs(rot - lastRot) > MOVE_DIST) {
				if (rot > lastRot) {
					left.add(false);
					right.add(true);
				} else {
					left.add(true);
					right.add(false);
				}
			} else {
				left.add(false);
				right.add(false);
			}
			if (Math.abs(pit - lastPit) > MOVE_DIST) {
				if (pit > lastPit) {
					up.add(false);
					down.add(true);
				} else {
					up.add(true);
					down.add(false);
				}
			} else {
				up.add(false);
				down.add(false);
			}
			lastRot = rot;
			lastPit = pit;
		}
	}
	
	public boolean matches(CastPattern cp) {
		if (this.up.size() != cp.up.size()) {
			return false;
		}
		
		for (int i = 0; i < this.up.size(); i++) {
			if (this.up.get(i) != cp.up.get(i) || this.down.get(i) != cp.down.get(i) || this.left.get(i) != cp.left.get(i) || this.right.get(i) != cp.right.get(i)) {
				return false;
			}
		}
		return true;
	}


}
