import java.util.Random;
import java.util.HashSet;

public class FastGrowSpell extends Spell {

	private HashSet<String> growers;
	private Random rand;
	
	// cost
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	private int TREE_REDSTONE_COST;
	
	private int SIZE_MIN;
	private int SIZE_VARIATION;
	
	// strings
	private String STR_CAST_ON;
	private String STR_CAST_ON_OTHERS;
	private String STR_CAST_OFF;
	private String STR_CAST_OFF_OTHERS;
	private String STR_PLANT;
	private String STR_PLANT_OTHERS;
	private String STR_PLANT_ERR;
	
	private int [][] reagents;
	private int [][] treeReagents;

	public FastGrowSpell(MagicSpellsListener listener, PropertiesFile properties) {
		super(listener,properties);
		
		// register spell
		String n = properties.getString("fastgrow-spellname","fastgrow");
		listener.registerSpellName(n,this,properties.getString("fastgrow-desc","Causes planted saplings to grow instantly."));
		listener.registerCastPattern(n,properties.getString("fastgrow-cast-pattern","U,L,R"));
		listener.requireBlockPlaceCall(this);
		
		// get properties
		REDSTONE_COST = properties.getInt("fastgrow-redstone-cost",10);
		OTHER_COST = properties.getInt("fastgrow-other-cost-type",6);
		OTHER_COST_NAME = properties.getString("fastgrow-other-cost-name","saplings");
		OTHER_COST_AMT = properties.getInt("fastgrow-other-cost-amt",3);
		TREE_REDSTONE_COST = properties.getInt("fastgrow-per-tree-redstone-cost",3);
		
		SIZE_MIN = properties.getInt("fastgrow-tree-size-min",5);
		SIZE_VARIATION = properties.getInt("fastgrow-tree-size-variation",12);
		if (SIZE_MIN < 1) SIZE_MIN = 1;
		
		STR_CAST_ON = properties.getString("fastgrow-cast-on-str","Your hands glow faintly green, and you gain nature powers.");
		STR_CAST_ON_OTHERS = properties.getString("fastgrow-cast-on-others-str","[caster]'s hands start to glow faintly green!");
		STR_CAST_OFF = properties.getString("fastgrow-cast-off-str","Your hands lose their green glow.");
		STR_CAST_OFF_OTHERS = properties.getString("fastgrow-cast-off-others-str","[caster]'s hands lose their green hue.");
		STR_PLANT = properties.getString("fastgrow-plant-str","The sapling grows into a tree instantly!");
		STR_PLANT_OTHERS = properties.getString("fastgrow-plant-others-str","[caster]'s sapling grows into a tree instantly!");
		STR_PLANT_ERR = properties.getString("fastgrow-plant-err-str","Your sapling fails to grow into a tree.");
		
		// prepare data
		growers = new HashSet<String>();
		rand = new Random();
		
		// prepare reagents
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
		treeReagents = new int [][] {{REDSTONE_DUST,TREE_REDSTONE_COST}};
	}

	public boolean cast(Player player, String [] command) {
		if (!growers.contains(player.getName().toLowerCase())) {
			if (removeReagents(player,reagents)) {
				growers.add(player.getName().toLowerCase());
				sendMessage(player, STR_CAST_ON);
				sendMessageToPlayersInRange(player,STR_CAST_ON_OTHERS.replace("[caster]",player.getName()));
				return true;
			} else {
				sendMessage(player, STR_NO_REAGENTS);
				return false;
			}
		} else {
			turnOff(player);
			return true;
		}
	}
	
	private void turnOff(Player player) {
		growers.remove(player.getName().toLowerCase());
		sendMessage(player, STR_CAST_OFF);
		sendMessageToPlayersInRange(player,STR_CAST_OFF_OTHERS.replace("[caster]",player.getName()));	
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", " + OTHER_COST_AMT + " " + OTHER_COST_NAME:"") + (TREE_REDSTONE_COST>0?"@plus " + TREE_REDSTONE_COST + " " + REDSTONE_NAME + " per tree planted":"");
	}
	
	public boolean onBlockPlace(Player player, Block blockPlaced, Block blockClicked, Item itemInHand) {
		if (growers.contains(player.getName().toLowerCase()) && blockPlaced.getType() == 6) {
			if (removeReagents(player,treeReagents)) {
				//boolean success = growTree(blockPlaced.getX(), blockPlaced.getY(), blockPlaced.getZ());
				blockPlaced.setType(0);
				blockPlaced.update();
				TreeGrower tree = new TreeGrower(SIZE_MIN, SIZE_VARIATION);
				boolean success = tree.grow(rand, blockPlaced.getX(), blockPlaced.getY(), blockPlaced.getZ());
				//bj tree = new hd();
				//boolean success = tree.a(etc.getServer().getMCServer().e, rand, blockPlaced.getX(), blockPlaced.getY(), blockPlaced.getZ());

				if (success) {
					sendMessage(player, STR_PLANT);
					sendMessageToPlayersInRange(player,STR_PLANT_OTHERS.replace("[caster]",player.getName()));
				} else {
					sendMessage(player, STR_PLANT_ERR);
					blockPlaced.setType(6);
					blockPlaced.update();
				}
			} else {
				sendMessage(player, STR_NO_REAGENTS);
				turnOff(player);
			}
		}
		return false;
	}

}
