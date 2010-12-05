public class MagicSpells extends Plugin {
	private static final MagicSpellsListener listener = new MagicSpellsListener();
	
	public void initialize() {
		PluginLoader loader = etc.getLoader();
		loader.addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.MEDIUM);
		loader.addListener(PluginLoader.Hook.BLOCK_PLACE, listener, this, PluginListener.Priority.MEDIUM);
		loader.addListener(PluginLoader.Hook.BLOCK_RIGHTCLICKED, listener, this, PluginListener.Priority.MEDIUM);
		loader.addListener(PluginLoader.Hook.BLOCK_DESTROYED, listener, this, PluginListener.Priority.MEDIUM);
		loader.addListener(PluginLoader.Hook.ARM_SWING, listener, this, PluginListener.Priority.MEDIUM);
		loader.addListener(PluginLoader.Hook.PLAYER_MOVE, listener, this, PluginListener.Priority.MEDIUM);
		loader.addListener(PluginLoader.Hook.LOGIN, listener, this, PluginListener.Priority.MEDIUM);
		loader.addListener(PluginLoader.Hook.DISCONNECT, listener, this, PluginListener.Priority.MEDIUM);
		loader.addListener(PluginLoader.Hook.HEALTH_CHANGE, listener, this, PluginListener.Priority.MEDIUM);
		loader.addListener(PluginLoader.Hook.DAMAGE, listener, this, PluginListener.Priority.MEDIUM);
		loader.addListener(PluginLoader.Hook.FLOW, listener, this, PluginListener.Priority.MEDIUM);
		loader.addListener(PluginLoader.Hook.ITEM_USE, listener, this, PluginListener.Priority.MEDIUM);
		loader.addCustomListener(new SpellLearnListener(listener));
	}
	
	public void enable() {
		etc inst = etc.getInstance();
		inst.addCommand("/cast","[spell] <options> - Casts a spell");
		listener.enable();
	}
	
	public void disable() {
		etc inst = etc.getInstance();
		inst.removeCommand("/cast");
		listener.disable();
	}
}
