import java.util.HashSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Scanner;
import java.io.IOException;
import java.util.logging.*;

public class TransmuteSpell extends Spell {
	private static final Logger log = Logger.getLogger("Minecraft");

	private HashSet<Xmute> xmutes;
	private Comparator<Item> c;
	
	private String XMUTE_FILE;
	private String CREATE_REQ_GROUP;
	
	private String STR_SUCCESS;
	private String STR_FAIL;

	public TransmuteSpell(MagicSpellsListener listener, PropertiesFile properties) {		
		listener.registerSpellName(properties.getString("transmute-spellname","transmute"),this,properties.getString("transmute-desc","Transmute items to new items."));
		
		xmutes = new HashSet<Xmute>();
		c = new ItemComparer<Item>();
		
		// get properties
		XMUTE_FILE = properties.getString("transmute-filename","transmutes.txt");
		CREATE_REQ_GROUP = properties.getString("transmute-create-req-rank","wizards");
		
		STR_SUCCESS = properties.getString("transmute-success-str","You have performed a transmutation!");
		STR_FAIL = properties.getString("transmute-fail-str","You have failed to perform a transmutation.");
		
		loadTransmutes();
		
	}
	
	public boolean cast(Player player, String [] command) {
		if (command.length == 2) {
			// create provided reagent list
			ArrayList<Item> reagents = new ArrayList<Item>();
			Inventory inv = player.getCraftingTable();
			for (int i = 0; i < 4; i++) {
				Item item = inv.getItemFromSlot(i);
				if (item != null && item.getItemId() > 0 && item.getAmount() > 0) {
					reagents.add(item);
				}
			}
			Collections.sort(reagents, c);
			
			// check if empty
			if (reagents.size() == 0) {
				sendMessage(player, STR_FAIL);
				return false;
			}
		
			// find xmute
			Xmute xmute = null;
			for (Xmute x : xmutes) {
				if (x.matches(reagents)) {
					xmute = x;
					break;
				}
			}
		
			// process xmute
			if (xmute != null) {
				xmute.process(player);
				sendMessage(player, STR_SUCCESS);
				return true;
			} else {
				sendMessage(player, STR_FAIL);
				return false;
			}
		} else if (command.length >= 3 && command[2].equalsIgnoreCase("list")) {
			// list all xmutes
			sendMessage(player, "Not yet implemented.");
			return true;
			
		} else if (command.length > 4 && command[2].equalsIgnoreCase("create") && player.isInGroup(CREATE_REQ_GROUP)) {
			// creating new xmute -- first get info
			String name = command[3];
			String desc = "";
			for (int i = 4; i < command.length; i++) {
				desc += command[i] + " ";
			}
			desc = desc.trim();
			
			// get reagents
			ArrayList<Item> reagents = new ArrayList<Item>();
			Inventory inv = player.getCraftingTable();
			for (int i = 0; i < 4; i++) {
				Item item = inv.getItemFromSlot(i);
				if (item != null && item.getItemId() > 0 && item.getAmount() > 0) {
					reagents.add(item);
				}
			}
			
			// get results
			ArrayList<Item> results = new ArrayList<Item>();
			inv = player.getInventory();
			for (int i = 0; i < 4; i++) {
				Item item = inv.getItemFromSlot(i);
				if (item != null && item.getItemId() > 0 && item.getAmount() > 0) {
					results.add(item);
				}
			}
			
			// create transmute
			if (reagents.size() > 0 && results.size() > 0) {
				Xmute x = new Xmute(name, desc, reagents, results);
				xmutes.add(x);
				saveTransmutes();
				sendMessage(player, "Transmute created: " + x.toString());
			} else {
				sendMessage(player, "Fail.");
			}
			
			return true;
		}
		
		return false;
	}
	
	public String getCostDesc(String s) {
		return "free";
	}
	
	private class Xmute {
		private String name;
		private String desc;
		private ArrayList<Item> reagents;
		private ArrayList<Item> results;
		
		public Xmute(String data) {
			reagents = new ArrayList<Item>();
			results = new ArrayList<Item>();
			
			String [] xmuteData = data.split(":");
			
			// get info
			name = xmuteData[0];
			desc = xmuteData[1];
			
			// get reagents
			String [] items = xmuteData[2].split(",");
			for (String i : items) {
				String [] itemData = i.split(" ");
				reagents.add(new Item(Integer.parseInt(itemData[0]),Integer.parseInt(itemData[1])));
			}
			
			// get results
			items = xmuteData[3].split(",");
			for (String i : items) {
				String [] itemData = i.split(" ");
				results.add(new Item(Integer.parseInt(itemData[0]),Integer.parseInt(itemData[1])));
			}			
		}
		
		public Xmute(String name, String desc, ArrayList<Item> reagents, ArrayList<Item> results) {
			this.name = name;
			this.desc = desc;
			this.reagents = reagents;
			this.results = results;
		}
		
		public boolean matches(ArrayList<Item> r) {
			if (r.size() != reagents.size()) {
				return false;
			}
			for (int i = 0; i < reagents.size(); i++) {
				if (reagents.get(i).getItemId() != r.get(i).getItemId() || reagents.get(i).getAmount() > r.get(i).getAmount()) {
					return false;
				}
			}
			return true;
		}
		
		public void process(Player player) {
			Inventory table = player.getCraftingTable();
			Inventory inv = player.getInventory();
			for (Item i : reagents) {
				table.removeItem(i);
			}
			for (Item i : results) {
				inv.giveItem(i.getItemId(),i.getAmount());
			}
			table.updateInventory();
			inv.updateInventory();
		}
		
		public String toString() {
			String s = name + ":" + desc.replace(":","") + ":";
			for (Item i : reagents) {
				s += i.getItemId() + " " + i.getAmount() + ",";
			}
			s = s.substring(0,s.length()-1) + ":";
			for (Item i : results) {
				s += i.getItemId() + " " + i.getAmount() + ",";
			}
			s = s.substring(0,s.length()-1);
			return s;
		}
	}
	
	private class ItemComparer<T> implements Comparator<T> {
		public int compare(Object o1, Object o2) {
			if (!(o1 instanceof Item) || !(o2 instanceof Item)) {
				return 0;
			} else { 
				int diff = ((Item)o1).getItemId() - ((Item)o2).getItemId();
				if (diff != 0) {
					return diff;
				} else {
					return ((Item)o1).getAmount() - ((Item)o2).getAmount();
				}
			}		
		}
	}
	
	private void loadTransmutes() {
		File file = null;
		
		try {
			file = new File(MagicSpellsListener.DATA_FOLDER + XMUTE_FILE);
			if (file.exists()) {
				Scanner scanner = new Scanner(file);
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					if (!line.startsWith("#") && !line.equals("")) {
						xmutes.add(new Xmute(line));
					}
				
				}
				scanner.close();
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Exception reading " + MagicSpellsListener.DATA_FOLDER + XMUTE_FILE);
		}
		
	}
	
	private synchronized void saveTransmutes() {
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(MagicSpellsListener.DATA_FOLDER + XMUTE_FILE,false));		
			for (Xmute x : xmutes) {
				writer.append(x.toString());
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			log.log(Level.SEVERE,"Failed to write " + MagicSpellsListener.DATA_FOLDER + XMUTE_FILE);
			try {
				if (writer != null) writer.close();
			} catch (IOException ee) {
			}
		}
	
	}

}
