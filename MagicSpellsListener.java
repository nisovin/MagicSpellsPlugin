import java.util.HashSet;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Scanner;
import java.io.IOException;
import java.util.logging.*;

public class MagicSpellsListener extends PluginListener {
	private static final Logger log = Logger.getLogger("Minecraft");

	// spell data
	private static HashSet<Spell> spells;
	private static Hashtable<String,Spell> spellNames;
	private static Hashtable<String,String> spellDescs;
	
	private static Hashtable<CastPattern,String> castPatterns;
	private static Hashtable<String,CastPattern> activeCastPatterns;
	
	private static Hashtable<String,HashSet<String>> learnedSpells;
	
	// rune data
	private static HashSet<Rune> runes;
	
	// listener calls
	private static HashSet<Spell> blockPlaceCalls;
	private static HashSet<Spell> blockRightClickCalls;
	private static HashSet<Spell> blockDestroyCalls;
	private static HashSet<Spell> armSwingCalls;
	private static HashSet<Spell> playerMoveCalls;
	private static HashSet<Spell> disconnectCalls;
	private static HashSet<Spell> healthChangeCalls;
	private static HashSet<Spell> damageCalls;
	private static HashSet<Spell> flowCalls;
	
	// properties
	public static String DATA_FOLDER;
	private static boolean USE_C_CAST_SHORTCUT;
	private static String STR_CAST_USAGE;
	public static String STR_CAST_FAIL;
	public static boolean LEARN_REQUIRES_PERM;
	private boolean CAST_PATTERNS_ENABLED;
	private int CAST_PATTERNS_WAND;
	private static int RUNE_ACTIVATOR;
	private static int RUNE_TEACHER;
	private static boolean RUNES_ENABLED;
	private static String STR_RUNE_LEARNED;

	public void enable() {
		// load properties
		PropertiesFile properties = new PropertiesFile("magicspells.properties");
		DATA_FOLDER = properties.getString("general-data-folder","magicspells") + "/";
		USE_C_CAST_SHORTCUT = properties.getBoolean("general-use-c-cast-shortcut",true);
		STR_CAST_USAGE = properties.getString("general-cast-usage-str","Usage: /cast [spell] <options> (not all spells have options)@Use /cast list <page> to list spells you have learned.@Use /cast cost [spell] to check the reagent cost of a spell.");
		STR_CAST_FAIL = properties.getString("general-cast-fail-str","Your magic words have no effect.");
		LEARN_REQUIRES_PERM = properties.getBoolean("general-learning-requires-comm-perm",false);
		CAST_PATTERNS_ENABLED = properties.getBoolean("general-cast-patterns-enabled",true);
		CAST_PATTERNS_WAND = properties.getInt("general-cast-patterns-wand",280);
		RUNE_ACTIVATOR = properties.getInt("general-rune-activator",276);
		RUNE_TEACHER = properties.getInt("general-rune-teacher",277);
		RUNES_ENABLED = properties.getBoolean("general-rune-system-enabled",true);
		STR_RUNE_LEARNED = properties.getString("general-rune-learned-str","You have learned the [rune] rune!");
	
		// spell data
		spells = new HashSet<Spell>();
		spellNames = new Hashtable<String,Spell>();
		spellDescs = new Hashtable<String,String>();
		
		if (CAST_PATTERNS_ENABLED) {
			castPatterns = new Hashtable<CastPattern,String>();
			activeCastPatterns = new Hashtable<String,CastPattern>();
		}
		
		// hooks
		blockPlaceCalls = new HashSet<Spell>();
		blockRightClickCalls = new HashSet<Spell>();
		blockDestroyCalls = new HashSet<Spell>();
		armSwingCalls = new HashSet<Spell>();
		playerMoveCalls = new HashSet<Spell>();
		disconnectCalls = new HashSet<Spell>();
		healthChangeCalls = new HashSet<Spell>();
		damageCalls = new HashSet<Spell>();
		flowCalls = new HashSet<Spell>();
	
		// initialize the spells
		if (properties.getBoolean("list-enabled",true)) spells.add(new ListSpell(this,properties));
		if (properties.getBoolean("cost-enabled",true)) spells.add(new CostSpell(this,properties));
		if (properties.getBoolean("teach-enabled",true)) spells.add(new TeachSpell(this,properties));
		if (properties.getBoolean("spellbook-enabled",true)) spells.add(new SpellbookSpell(this,properties));
		
		if (properties.getBoolean("ascenddescend-enabled",true)) spells.add(new AscendAndDescendSpell(this,properties));
		if (properties.getBoolean("blink-enabled",true)) spells.add(new BlinkSpell(this,properties));
		if (properties.getBoolean("boom-enabled",true)) spells.add(new BoomSpell(this,properties));
		//if (properties.getBoolean("boost-enabled",true)) spells.add(new BoostSpell(this,properties));
		if (properties.getBoolean("bubble-enabled",true)) spells.add(new BubbleSpell(this,properties));
		if (properties.getBoolean("deathwalk-enabled",true)) spells.add(new DeathwalkSpell(this,properties));
		if (properties.getBoolean("extinguish-enabled",true)) spells.add(new ExtinguishSpell(this,properties));
		if (properties.getBoolean("fastgrow-enabled",true)) spells.add(new FastGrowSpell(this,properties));
		if (properties.getBoolean("firetools-enabled",true)) spells.add(new FireToolsSpell(this,properties));
		//if (properties.getBoolean("frostnova-enabled",true)) spells.add(new FrostNovaSpell(this,properties));
		if (properties.getBoolean("frostwalk-enabled",true)) spells.add(new FrostwalkSpell(this,properties));
		if (properties.getBoolean("gills-enabled",true)) spells.add(new GillsSpell(this,properties));
		if (properties.getBoolean("heal-enabled",true)) spells.add(new HealSpell(this,properties));
		if (properties.getBoolean("invincible-enabled",true)) spells.add(new InvincibleSpell(this,properties));
		if (properties.getBoolean("lifewalk-enabled",true)) spells.add(new LifewalkSpell(this,properties));
		if (properties.getBoolean("lightwalk-enabled",true)) spells.add(new LightwalkSpell(this,properties));
		if (properties.getBoolean("markrecall-enabled",true)) spells.add(new MarkAndRecallSpell(this,properties));
		if (properties.getBoolean("purge-enabled",true)) spells.add(new PurgeSpell(this,properties));
		if (properties.getBoolean("safefall-enabled",true)) spells.add(new SafefallSpell(this,properties));
		if (properties.getBoolean("stonevision-enabled",true)) spells.add(new StoneVisionSpell(this,properties));
		if (properties.getBoolean("teleport-enabled",true)) spells.add(new TeleportSpell(this,properties));
		if (properties.getBoolean("transmute-enabled",true)) spells.add(new TransmuteSpell(this,properties));
		if (properties.getBoolean("treetrim-enabled",true)) spells.add(new TreeTrimSpell(this,properties));
		//if (properties.getBoolean("vanish-enabled",true)) spells.add(new VanishSpell(this,properties));
		if (properties.getBoolean("wand-enabled",true)) spells.add(new WandSpell(this,properties));
		if (properties.getBoolean("wings-enabled",true)) spells.add(new WingsSpell(this,properties));
		if (properties.getBoolean("yoink-enabled",true)) spells.add(new YoinkSpell(this,properties));
		
		// rune initialization
		if (RUNES_ENABLED) {
			// setup block types
			String [] types = properties.getString("general-rune-blocktypes","-1,--,Anything;0,AR,Air;1,ST,Stone;2,GR,Grass;3,DT,Dirt;4,CB,Cobblestone;5,WD,Wood;12,SD,Sand;13,GV,Gravel;17,LG,Log;18,LV,Leaves;20,GL,Glass;35,CL,Cloth;41,GB,Gold Block;42,IB,Iron Block;43,DS,Doublestep;44,HS,Halfstep;45,BK,Brick;40,OB,Obsidian;50,TR,Torch;57,DB,Diamond Block;76,RT,Redstone Torch;80,SW,Snow Block;82,CY,Clay;87,NS,Netherstone;88,SS,Slow Sand;89,LS,Lightstone").split(";");
			Rune.blockCodes = new HashMap<Integer,String>();
			Rune.blockNames = new HashMap<Integer,String>();
			for (String type : types) {
				String [] data = type.split(",");
				int i = Integer.parseInt(data[0]);
				Rune.blockCodes.put(i,data[1]);
				Rune.blockNames.put(i,data[2]);
			}
		
			// setup runes
			runes = new HashSet<Rune>();
			if (properties.getBoolean("runes-mineshaft-enabled",true)) runes.add(new MineshaftRune(this,properties));
			if (properties.getBoolean("runes-ascend-enabled",true)) runes.add(new AscendRune(this,properties));
			if (properties.getBoolean("runes-descend-enabled",true)) runes.add(new DescendRune(this,properties));
			if (properties.getBoolean("runes-summon-request-enabled",true)) runes.add(new SummonRequestRune(this,properties));
			if (properties.getBoolean("runes-summon-accept-enabled",true)) runes.add(new SummonAcceptRune(this,properties));
		}
		
		// load learned data from files
		learnedSpells = new Hashtable<String,HashSet<String>>();
		for (Player p : etc.getServer().getPlayerList()) {
			loadLearnedSpells(p);
		}
		
		log.info("MagicSpells loaded successfully!");
	}
	
	public void disable() {
		// clean up
		for (Spell s : spells) {
			s.disable();
		}
		
		spells = null;
		spellNames = null;
		spellDescs = null;
		
		castPatterns = null;
		activeCastPatterns = null;
		
		learnedSpells = null;
		
		blockPlaceCalls = null;
		blockRightClickCalls = null;
		blockDestroyCalls = null;
		armSwingCalls = null;
		playerMoveCalls = null;
		disconnectCalls = null;
		healthChangeCalls = null;
		damageCalls = null;
		flowCalls = null;
		
		runes = null;
	}
	
	public void onLogin(Player player) {
		loadLearnedSpells(player);
	}
	
	public boolean onCommand(Player player, String [] command) {
		// reload data
		if (command[0].equalsIgnoreCase("/reload")) {
			disable();
			enable();
			return false;			
			
		// cast command
		} else if ((command[0].equalsIgnoreCase("/cast") || (USE_C_CAST_SHORTCUT && command[0].equalsIgnoreCase("/c"))) && player.canUseCommand("/cast")) {
			if (command.length == 1) {
				// no params - show help
				String [] castUsage = STR_CAST_USAGE.split("@");
				for (int i=0; i<castUsage.length; i++) {
					player.sendMessage(Spell.TEXT_COLOR + castUsage[i]);
				}
			} else if (spellNames.containsKey(command[1].toLowerCase())) {
				// spell exists
				Spell spell = spellNames.get(command[1]);
				if (canCastSpell(player,command[1])) {
					// spell is learned - cast it
					spell.cast(player, command);
				} else {
					// player doesn't know spell
					player.sendMessage(Spell.TEXT_COLOR + STR_CAST_FAIL);
				}
			} else {
				// no such spell name
				player.sendMessage(Spell.TEXT_COLOR + STR_CAST_FAIL);
			}
		
			return true;
		
		// rune command
		} else if (command[0].equalsIgnoreCase("/rune") && player.canUseCommand("/rune")) {
			if (command.length == 1) {
				// list known runes
				player.sendMessage(Spell.TEXT_COLOR + "Known runes:");
				boolean found = false;
				for (Rune rune : runes) {
					if (canUseRune(player, rune)) {
						player.sendMessage(Spell.TEXT_COLOR + rune.getName() + ": " + rune.getDesc());
						found = true;
					}
				}
				if (!found) {
					player.sendMessage(Spell.TEXT_COLOR + "None.");
				}
			} else if (command.length == 2) {
				// show rune info
				Rune rune = null;
				for (Rune r : runes) {
					if (r.getName().equalsIgnoreCase(command[1]) && canUseRune(player, r)) {
						rune = r;
						break;
					}
				}
				if (rune != null) {
					rune.describeRune(player);
				}
			}
			
			return true;
			
		} else {
			return false;
		}
	}
	
	public boolean canCastSpell(Player player, String spellName) {
		return (/* spell doesn't need learned? */ spellNames.containsKey(spellName.toLowerCase()) && !spellNames.get(spellName.toLowerCase()).needsLearned()) || (/* player is admin? */ player.isAdmin() && spellDescs.containsKey(spellName.toLowerCase())) || (/* player has learned spell? */ learnedSpells.containsKey(player.getName().toLowerCase()) && learnedSpells.get(player.getName().toLowerCase()).contains(spellName.toLowerCase()) || ( /* can cast anyway from perm? */ !LEARN_REQUIRES_PERM && player.canUseCommand("/cast"+spellName)));
	}
	
	public static void cancelSpells(Player player, String [] spellsToCancel) {
		for (Spell s : spells) {
			for (int i = 0; i < spellsToCancel.length; i++) {
				if (s.getClass().getName().equals(spellsToCancel[i])) {
					s.cancel(player);
				}
			}
		}
	}
	
	public boolean onBlockPlace(Player player, Block placed, Block clicked, Item item) {
		// call spell hooks
		boolean result = false;
		for (Spell spell : blockPlaceCalls) {
			result = spell.onBlockPlace(player, placed, clicked, item);
			if (result) {
				break;
			}
		}
		return result;
	}
	
	public void onBlockRightClicked(Player player, Block block, Item itemInHand) {
		if (RUNES_ENABLED && (itemInHand.getItemId() == RUNE_ACTIVATOR || itemInHand.getItemId() == RUNE_TEACHER)) {
			// get placed blocks
			int [][] check = new int [5][5];
			int i = 0, j = 0;
			for (int x = block.getX() - 2; x <= block.getX() + 2; x++) {
				i = 0;
				for (int z = block.getZ() - 2; z <= block.getZ() + 2; z++) {
					check[i][j] = etc.getServer().getBlockIdAt(x,block.getY(),z);
					i++;
				}
				j++;
			}
			for (Rune r : runes) {
				if (canUseRune(player, r) && checkRune(r.getRune(), check)) {
					// rune matches
					if (itemInHand.getItemId() == RUNE_ACTIVATOR) {
						// activate the rune
						r.activate(player, block);
					} else {
						// teach the rune to players nearby
						if (r.needsLearned()) {
							for (Player p : etc.getServer().getPlayerList()) {
								if (!canUseRune(p,r)
										&& block.getX()-3 <= p.getX() && p.getX() <= block.getX()+3
										&& block.getY()-1 <= p.getY() && p.getY() <= block.getX()+1
										&& block.getZ()-3 <= p.getZ() && p.getZ() <= block.getZ()+3) {
									if (!learnedSpells.containsKey(player.getName().toLowerCase())) {
										learnedSpells.put(player.getName().toLowerCase(),new HashSet<String>());
									}
									learnedSpells.get(p.getName().toLowerCase()).add("rune#"+r.getName());
									saveLearnedSpells(p,"rune#"+r.getName());
									p.sendMessage(Spell.TEXT_COLOR + STR_RUNE_LEARNED.replace("[rune]",r.getName()));
								}
							}
						}
					}
					break;
				}
			}
		}
		
		// call spell hooks
		boolean result = false;
		for (Spell spell : blockRightClickCalls) {
			spell.onBlockRightClicked(player, block, itemInHand);
		}
	}
	
	public boolean checkRune(int [][] rune, int [][] check) {
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if (rune[i][j] != -1 && rune[i][j] != check[i][j]) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean canUseRune(Player player, Rune rune) {
		if (!rune.needsLearned() || player.isAdmin()) {	
			return true;
		} else if (learnedSpells.containsKey(player.getName().toLowerCase()) && learnedSpells.get(player.getName().toLowerCase()).contains("rune#"+rune.getName())) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean onBlockDestroy(Player player, Block block) {
		// call spell hooks
		boolean result = false;
		for (Spell spell : blockDestroyCalls) {
			result = spell.onBlockDestroy(player, block);
			if (result) {
				break;
			}
		}
		return result;
	}
	
	public boolean onItemUse(Player player, Block placed, Block clicked, Item item) {
		if (CAST_PATTERNS_ENABLED && item.getItemId() == CAST_PATTERNS_WAND) {
			if (!activeCastPatterns.containsKey(player.getName())) {
				activeCastPatterns.put(player.getName(), new CastPattern());
				player.sendMessage(Spell.TEXT_COLOR + "You begin to cast a spell...");
			} else {
				CastPattern pattern = activeCastPatterns.remove(player.getName());
				boolean casted = false;
				for (CastPattern p : castPatterns.keySet()) {
					if (p.matches(pattern)) {
						casted = true;
						String s = castPatterns.get(p);
						String [] command = new String [] {"/cast",s};
						spellNames.get(s).cast(player, command);
						break;
					}
				}
				if (!casted) {
					player.sendMessage(Spell.TEXT_COLOR + "Your spell fails.");
				}
			}
		}
		return false;
	}
	
	public void onArmSwing(Player player) {
		if (CAST_PATTERNS_ENABLED && player.getItemInHand() == CAST_PATTERNS_WAND && activeCastPatterns.containsKey(player.getName())) {
			activeCastPatterns.get(player.getName()).addMovement(player.getRotation(), player.getPitch());
		}
	
		// call spell hooks
		for (Spell spell : armSwingCalls) {
			spell.onArmSwing(player);
		}
	}
	
	public void onPlayerMove(Player player, Location from, Location to) {
		// call spell hooks
		for (Spell spell : playerMoveCalls) {
			spell.onPlayerMove(player, from, to);
		}
	}
	
	public void onDisconnect(Player player) {
		// call spell hooks
		for (Spell spell : disconnectCalls) {
			spell.onDisconnect(player);
		}
		
		// remove player from learned spells list
		learnedSpells.remove(player.getName().toLowerCase());
	}
	
	public boolean onHealthChange(Player player, int oldValue, int newValue) {
		// call spell hooks
		boolean result = false;
		for (Spell spell : healthChangeCalls) {
			result = spell.onHealthChange(player, oldValue, newValue);
			if (result) {
				break;
			}
		}
		return result;
	}
	
	public boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker, BaseEntity defender, int amount) {
		// call spell hooks
		boolean result = false;
		for (Spell spell : damageCalls) {
			result = spell.onDamage(type, attacker, defender, amount);
			if (result) {
				break;
			}
		}
		return result;
	}
	
	public boolean onFlow(Block from, Block to) {
		boolean result = false;
		for (Spell spell : flowCalls) {
			result = spell.onFlow(from, to);
			if (result) {
				break;
			}
		}
		return result;
	}
	
	// spells call this to specify the spell words they use
	public void registerSpellName(String spellName, Spell spell, String spellDescription) {
		spellNames.put(spellName.toLowerCase(), spell);
		spellDescs.put(spellName.toLowerCase(), spellDescription);
	}
	
	// spells call this to add a cast pattern for the spell
	public void registerCastPattern(String spell, String pattern) {
		if (CAST_PATTERNS_ENABLED) {
			castPatterns.put(new CastPattern(pattern), spell);
		}
	}
	
	// spells call this to specify they require block place checks
	public void requireBlockPlaceCall(Spell spell) {
		blockPlaceCalls.add(spell);
	}
	
	// spells call this to specify they require right click checks
	public void requireBlockRightClickCall(Spell spell) {
		blockRightClickCalls.add(spell);
	}
	
	// spells call this to specify they require block destroy checks
	public void requireBlockDestroyCall(Spell spell) {
		blockDestroyCalls.add(spell);
	}
	
	// spells call this to specify they require arm swing checks
	public void requireArmSwingCall(Spell spell) {
		armSwingCalls.add(spell);
	}
	
	// spells call this to specify they require player move checks
	public void requirePlayerMoveCall(Spell spell) {
		playerMoveCalls.add(spell);
	}
	
	// spells call this to specify they require player disconnect checks
	public void requireDisconnectCall(Spell spell) {
		disconnectCalls.add(spell);
	}
	
	// spells call this to specify they require health change checks
	public void requireHealthChangeCall(Spell spell) {
		healthChangeCalls.add(spell);
	}
	
	// spells call this to specify they require health change checks
	public void requireDamageCall(Spell spell) {
		damageCalls.add(spell);
	}
	
	public void requireFlowCall(Spell spell) {
		flowCalls.add(spell);
	}
	
	public HashSet<Spell> getSpells() {
		return spells;
	}
	
	public Hashtable<String,Spell> getSpellNames() {
		return spellNames;
	}
	
	public Hashtable<String,String> getSpellDescs() {
		return spellDescs;
	}
	
	public HashSet<String> getLearnedSpells(Player player) {
		if (learnedSpells.containsKey(player.getName().toLowerCase())) {
			return learnedSpells.get(player.getName().toLowerCase());
		} else {
			return null;
		}
	}
	
	public Hashtable<String,HashSet<String>> getLearnedSpells() {
		return learnedSpells;
	}
	
	public void loadLearnedSpells(Player player) {
		File file = null;
		
		try {
			// make data folder if it doesn't exist
			(new File(DATA_FOLDER)).mkdir();
			
			// get learned spells
			file = new File(DATA_FOLDER + player.getName().toLowerCase() + ".txt");
			if (file.exists()) {
				HashSet<String> l = new HashSet<String>();
				Scanner scanner = new Scanner(file);
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					if (line != "") {
						l.add(line);
					}
				}
				scanner.close();
				learnedSpells.put(player.getName().toLowerCase(), l);
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Exception reading " + DATA_FOLDER + player.getName().toLowerCase() + ".txt");
		}
	}
	
	public void saveLearnedSpells(Player player) {
		saveLearnedSpells(player.getName().toLowerCase(),"");
	}
	
	public void saveLearnedSpells(String name) {
		saveLearnedSpells(name, "");
	}
	
	public void saveLearnedSpells(Player player, String singleSpell) {
		saveLearnedSpells(player.getName().toLowerCase(),singleSpell);
	}
	
	public synchronized void saveLearnedSpells(String name, String singleSpell) {
		BufferedWriter writer = null;
		
		try {
			// make data folder if it doesn't exist
			(new File(DATA_FOLDER)).mkdir();
			
			// get learned spells
			HashSet<String> l = learnedSpells.get(name);
			
			if (l != null && l.size() > 0) {
				// save spells
				writer = new BufferedWriter(new FileWriter(DATA_FOLDER + name + ".txt", !singleSpell.equals("")));		
				if (singleSpell.equals("")) {
					for (String s : l) {
						writer.append(s);
						writer.newLine();
					}
				} else {
					writer.append(singleSpell);
					writer.newLine();
				}
				writer.close();
			}
		} catch (IOException e) {
			log.log(Level.SEVERE,"Failed to write " + DATA_FOLDER + name + ".txt");
			try {
				if (writer != null) writer.close();
			} catch (IOException ee) {
			}
		}
		
	}

}
