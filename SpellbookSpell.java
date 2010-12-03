import java.util.HashSet;
import java.util.Hashtable;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Scanner;
import java.io.IOException;
import java.util.logging.*;

public class SpellbookSpell extends Spell {
	private static final Logger log = Logger.getLogger("Minecraft");

	private MagicSpellsListener listener;
	
	private Hashtable<String,String> spellBooks;
	private Hashtable<String,Integer> usesRemaining;
	
	// spell cost
	private int REDSTONE_COST;
	private int OTHER_COST;
	private String OTHER_COST_NAME;
	private int OTHER_COST_AMT;
	
	// options
	private String SPELL_BOOKS_FILE;
	private int SPELL_BOOK_USES;
	private boolean DESTROY_BOOKCASE_ON_EMPTY;
	private String DISALLOWED_SPELLBOOKS;
	
	// strings
	private String STR_SPELLBOOK_CREATED;
	private String STR_SPELLBOOK_ERR_HAS_SPELL;
	private String STR_SPELLBOOK_ERR_NO_TARGET;
	private String STR_SPELLBOOK_ERR_INVALID_SPELL;
	private String STR_SPELLBOOK_ERR_DISALLOWED;
	private String STR_SPELLBOOK_LEARN_SUCCESS;
	private String STR_SPELLBOOK_ALREADY_LEARNED;
	private String STR_SPELLBOOK_CANT_CAST;
	private String STR_SPELLBOOK_CANT_LEARN;
	
	// reagents
	private int [][] reagents;

	public SpellbookSpell(MagicSpellsListener listener, PropertiesFile properties) {
		this.listener = listener;
		
		listener.registerSpellName(properties.getString("spellbook-spellname","spellbook"),this,properties.getString("spellbook-desc","Creates a spellbook that teaches a spell."));
		listener.requireBlockRightClickCall(this);
		
		// get properties
		REDSTONE_COST = properties.getInt("spellbook-redstone-cost",0);
		OTHER_COST = properties.getInt("spellbook-other-cost-type",0);
		OTHER_COST_NAME = properties.getString("spellbook-other-cost-name","");
		OTHER_COST_AMT = properties.getInt("spellbook-other-cost-amt",0);
		
		SPELL_BOOKS_FILE = properties.getString("spellbook-file","spell-books.txt");
		SPELL_BOOK_USES = properties.getInt("spellbook-num-uses",0);
		DESTROY_BOOKCASE_ON_EMPTY = properties.getBoolean("spellbook-destroy-bookcase-on-empty",true);
		DISALLOWED_SPELLBOOKS = properties.getString("spellbook-disallowed-spells","spellbook,teach");
		
		STR_SPELLBOOK_CREATED = properties.getString("spellbook-created-str","You have created a spellbook for the '[spell]' spell!");
		STR_SPELLBOOK_ERR_HAS_SPELL = properties.getString("spellbook-err-has-spell-str","That bookcase already teaches a spell.");
		STR_SPELLBOOK_ERR_NO_TARGET = properties.getString("spellbook-err-no-target-str","You must target a bookshelf to use that spell.");
		STR_SPELLBOOK_ERR_INVALID_SPELL = properties.getString("spellbook-err-invalid-spell-str","You must provide a valid spell name to create a spellbook.");
		STR_SPELLBOOK_ERR_DISALLOWED = properties.getString("spellbook-err-disallowed-str","You cannot make a spellbook with that spell.");
		STR_SPELLBOOK_LEARN_SUCCESS = properties.getString("spellbook-learn-str","You have learned the '[spell]' spell!");
		STR_SPELLBOOK_ALREADY_LEARNED = properties.getString("spellbook-already-learned-str","You already know the '[spell]' spell.");
		STR_SPELLBOOK_CANT_CAST = properties.getString("spellbook-cant-cast-str","You have found a spellbook, but don't know magic!");
		STR_SPELLBOOK_CANT_LEARN = properties.getString("spellbook-cant-learn-str","You cannot learn the '[spell]' spell.");
		
		// setup reagents
		reagents = new int [][] {{REDSTONE_DUST,REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};	
		
		loadSpellBooks();	
	}
	
	public boolean cast(Player player, String [] command) {
		if (command.length != 3) {
			// incorrect usage
			player.sendMessage(Spell.TEXT_COLOR + STR_SPELLBOOK_ERR_INVALID_SPELL);
			return false;
		} else if (!listener.canCastSpell(player,command[2])) {
			// can't put a spell you don't know into a spellbook
			player.sendMessage(Spell.TEXT_COLOR + STR_SPELLBOOK_ERR_INVALID_SPELL);
			return false;
		} else if ((","+DISALLOWED_SPELLBOOKS+",").contains(","+command[2]+",")) {
			// can't put that spell in a spellbook
			player.sendMessage(Spell.TEXT_COLOR + STR_SPELLBOOK_ERR_DISALLOWED);
			return false;
		} else if (!removeReagents(player,reagents)) {
			// no reagents
			player.sendMessage(Spell.TEXT_COLOR + STR_NO_REAGENTS);
			return false;
		} else {
			// make the spellbook
			HitBlox hit = new HitBlox(player);
			Block b = hit.getTargetBlock();
			if (b.getType() == 47) {
				String coords = b.getX() + "," + b.getY() + "," + b.getZ();
				if (!spellBooks.contains(coords)) {
					spellBooks.put(coords,command[2]);
					if (SPELL_BOOK_USES > 0) {
						usesRemaining.put(coords,SPELL_BOOK_USES);
					}
					saveSpellBooks();
					player.sendMessage(Spell.TEXT_COLOR + STR_SPELLBOOK_CREATED.replace("[spell]",command[2]));
					return true;
				} else {
					player.sendMessage(Spell.TEXT_COLOR + STR_SPELLBOOK_ERR_HAS_SPELL);
					return false;
				}
			} else {
				// not targeting a spellbook
				player.sendMessage(Spell.TEXT_COLOR + STR_SPELLBOOK_ERR_NO_TARGET);
				return false;
			}
		}
	}
	
	public String getCostDesc(String s) {
		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"");
	}
	
	public void onBlockRightClicked(Player player, Block block, Item itemInHand) {
		if (block.getType() == 47) {
			String coords = block.getX() + "," + block.getY() + "," + block.getZ();
			if (spellBooks.containsKey(coords)) {
				String s = spellBooks.get(coords);
				// check if player can even use magic
				if (player.canUseCommand("/cast")) {
					// player can use spells - check if player already knows the spell
					if (!listener.canCastSpell(player,s)) {
						// player doesn't know the spell - check if they can learn it
						if (!listener.LEARN_REQUIRES_PERM || player.canUseCommand("/cast"+s)) {
							// can learn - add it to their spells
							if (listener.getLearnedSpells().containsKey(player.getName().toLowerCase())) {
								listener.getLearnedSpells().get(player.getName().toLowerCase()).add(s);
							} else {
								HashSet<String> h = new HashSet<String>();
								h.add(s);
								listener.getLearnedSpells().put(player.getName().toLowerCase(),h);							
							}
							listener.saveLearnedSpells(player,s);
							player.sendMessage(Spell.TEXT_COLOR + STR_SPELLBOOK_LEARN_SUCCESS.replace("[spell]",s));
						
							// remove a spellbook use
							if (SPELL_BOOK_USES > 0) {
								int usesLeft = usesRemaining.get(coords) - 1;
								if (usesLeft <= 0) {
									// no more uses - remove spellbook
									spellBooks.remove(coords);
									usesRemaining.remove(coords);
									saveSpellBooks();
									if (DESTROY_BOOKCASE_ON_EMPTY) {
										block.setType(0);
										block.update();
									}
								} else {
									usesRemaining.put(coords,usesLeft);
								}
							}
						} else {
							player.sendMessage(Spell.TEXT_COLOR + STR_SPELLBOOK_CANT_LEARN.replace("[spell]",s));
						}
					} else {
						// player already knows that spell
						player.sendMessage(Spell.TEXT_COLOR + STR_SPELLBOOK_ALREADY_LEARNED.replace("[spell]",s));
					}
				} else {
					// player can't use magic
					player.sendMessage(Spell.TEXT_COLOR + STR_SPELLBOOK_CANT_CAST);
				}
			}
		}
	}
	
	private void loadSpellBooks() {
		spellBooks = new Hashtable<String,String>();
		if (SPELL_BOOK_USES > 0) {
			usesRemaining = new Hashtable<String,Integer>();
		}
		
		File file = null;
		
		try {
			file = new File(MagicSpellsListener.DATA_FOLDER + SPELL_BOOKS_FILE);
			if (file.exists()) {
				Scanner scanner = new Scanner(file);
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					if (!line.startsWith("#") && !line.equals("")) {
						String [] data = line.split(":");
						spellBooks.put(data[0],data[1]);
						if (SPELL_BOOK_USES > 0) {
							usesRemaining.put(data[0],SPELL_BOOK_USES);
						}
					}
				
				}
				scanner.close();
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Exception reading " + MagicSpellsListener.DATA_FOLDER + SPELL_BOOKS_FILE);
		}
		
	}
	
	private synchronized void saveSpellBooks() {
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(MagicSpellsListener.DATA_FOLDER + SPELL_BOOKS_FILE,false));		
			for (String s : spellBooks.keySet()) {
				writer.append(s + ":" + spellBooks.get(s));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			log.log(Level.SEVERE,"Failed to write " + MagicSpellsListener.DATA_FOLDER + SPELL_BOOKS_FILE);
			try {
				if (writer != null) writer.close();
			} catch (IOException ee) {
			}
		}
	
	}

}
