package de.teamlapen.vampirism;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Map.Entry;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import de.teamlapen.vampirism.util.BALANCE;
import de.teamlapen.vampirism.util.DefaultBoolean;
import de.teamlapen.vampirism.util.DefaultDouble;
import de.teamlapen.vampirism.util.DefaultInt;
import de.teamlapen.vampirism.util.Logger;
import de.teamlapen.vampirism.util.REFERENCE;

public class Configs {

	public static void init(File configFile, boolean inDev) {
		config = new Configuration(configFile);
		loadConfiguration();
		if(inDev&&shouldResetInDev()){
			reset();
			setResetInDev(true);
		}
		Logger.i("Config", "Loaded configuration");
	}
	
	/**
	 * Loads/refreshes the configuration and adds comments if there aren't any
	 * {@link #init(File) init} has to be called once before using this
	 */
	private static void loadConfiguration() {
		// Categories
		ConfigCategory cat_balance = config.getCategory(CATEGORY_BALANCE);
		cat_balance.setComment("You can adjust these values to change the balancing of this mod");
		ConfigCategory cat_village = config.getCategory(CATEGORY_VILLAGE);
		cat_village.setComment("Here you can configure the village generation");
		ConfigCategory cat_balance_player_mod = config.getCategory(CATEGORY_BALANCE_PLAYER_MOD);
		cat_balance_player_mod.setComment("You can adjust these values to change the vampire player modifiers");
		ConfigCategory cat_balance_player_skills = config.getCategory(CATEGORY_BALANCE_PLAYER_SKILLS);
		cat_balance_player_skills.setComment("You can adjust these values to change the vampire player skills");
		ConfigCategory cat_balance_leveling = config.getCategory(CATEGORY_BALANCE_LEVELING);
		cat_balance_leveling.setComment("You can adjust these values to change the level up requirements");
		ConfigCategory cat_balance_mobprop = config.getCategory(CATEGORY_BALANCE_MOBPROP);
		cat_balance_mobprop.setComment("You can adjust the properties of the added mobs");
		ConfigCategory cat_balance_vvprop = config.getCategory(CATEGORY_BALANCE_VVPROP);
		cat_balance_vvprop.setComment("You can adjust the configuration of village managment (Agressive hunters, etc.)");
		ConfigCategory cat_general=config.getCategory(CATEGORY_GENERAL);
		cat_general.setComment("General settings");
		ConfigCategory cat_disabled=config.getCategory(CATEGORY_DISABLE);
		cat_disabled.setComment("You can disable some features here, but it is not recommend and might cause problems (e.g. you can't get certain items");

		//General
		String conf_version=config.get(CATEGORY_GENERAL, "config_mod_version", REFERENCE.VERSION).getString();
		player_blood_watcher = config.get(CATEGORY_GENERAL, "player_data_watcher_id", 21,"ID for datawatcher. HAS TO BE THE SAME ON CLIENT AND SERVER").getInt();
		getVampireBiomeId();
		
		// Village
		village_gen_enabled = config.get(cat_village.getQualifiedName(), "enabled", true,
				"Should the custom generator be injected? (Enables/Disables the village mod)").getBoolean();
		village_density = config.get(cat_village.getQualifiedName(), "density", 15,
				"Minecraft will try to generate 1 village per NxN chunk area. Vanilla: 32").getInt();
		village_minDist = config.get(cat_village.getQualifiedName(), "minimumDistance", 4,
				"Village centers will be at least N chunks apart. Must be smaller than density. Vanilla: 8").getInt();
		village_size = config.get(cat_village.getQualifiedName(), "size", 0, "A higher size increases the overall spawn weight of buildings.")
				.getInt();

		if (village_minDist < 0) {
			Logger.e("VillageDensity", "Invalid config: Minimal distance must be non-negative.");
			village_gen_enabled = false;
		}
		if (village_minDist >= village_density) {
			Logger.e("VillageDensity", "Invalid config: Minimal distance must be smaller than density.");
			village_gen_enabled = false;
		}
		if (village_size < 0) {
			village_gen_enabled = false;
			Logger.e("VillageDensity", "Invalid config: Size must be non-negative.");
		}

		// Disable
		
		disable_vampire_biome = config.getBoolean("disable_vampire_biome", cat_disabled.getQualifiedName(), false, "Disable the generation of the vampire biome");
		
		// Balance
		loadFields(cat_balance, BALANCE.class);
		loadFields(cat_balance_player_mod, BALANCE.VP_MODIFIERS.class);
		loadFields(cat_balance_player_skills,BALANCE.VP_SKILLS.class);
		loadFields(cat_balance_leveling, BALANCE.LEVELING.class);
		loadFields(cat_balance_mobprop, BALANCE.MOBPROP.class);
		loadFields(cat_balance_vvprop,BALANCE.VV_PROP.class);
		
		//Create option if not existent
		shouldResetInDev();
		
		if (config.hasChanged()) {
			config.save();
		}
		if(!conf_version.equals(REFERENCE.VERSION)){
			Logger.i("Config", "Detected Modupdate");
			handleModUpdated(conf_version);	
		}
		
		
	}
	/**
	 * This methods makes variables from a class available in the config file.
	 * To use this, the given class can only contain static non-final variables,
	 * which should have a '@Default*' annotation containing the default value.
	 * Currently only boolean,int and double are supported
	 * 
	 * @param cat
	 *            Config Category to put the properties inside
	 * @param cls
	 *            Class to go through
	 * @author Maxanier
	 */
	private static void loadFields(ConfigCategory cat, Class cls) {
		for (Field f : cls.getDeclaredFields()) {
			String name = f.getName();
			Class type = f.getType();
			try {
				if (type == int.class) {
					// Possible exception should not be caught so you can't forget a default value					
					DefaultInt a = f.getAnnotation(DefaultInt.class); 
					f.set(null, config.get(cat.getQualifiedName(), a.name(), a.value(), 
							a.comment(), a.minValue(), a.maxValue()).getInt());
				} else if (type == double.class) {
					// Possible exception should not be caught so you can't forget a default value					
					DefaultDouble a = f.getAnnotation(DefaultDouble.class); 
					f.set(null, config.get(cat.getQualifiedName(), a.name(), a.value(), 
							a.comment(), a.minValue(), a.maxValue()).getDouble());
				} else if (type == boolean.class) {
					DefaultBoolean a = f.getAnnotation(DefaultBoolean.class); 
					f.set(null, config.get(cat.getQualifiedName(), a.name(), a.value(), a.comment()).getBoolean());
				}
			} catch (NullPointerException e1) {
				Logger.e("Configs", "Author probably forgot to specify a default value for " + name + " in " + cls.getCanonicalName(), e1);
				throw new Error("Please check you default values");
			} catch (Exception e) {
				Logger.e("Configs", "Cant set " + cls.getName() + " values", e);
				throw new Error("Please check your vampirism config file");
			}
		}
	}
	
	
	/**
	 * Called when the mod was updated, before the config values are reset
	 * @param oldVersion
	 */
	private static void handleModUpdated(String oldVersion){
		
	}
	public static final String CATEGORY_GENERAL = Configuration.CATEGORY_GENERAL;
	public static final String CATEGORY_VILLAGE = "village_settings";
	public static final String CATEGORY_BALANCE = "balance";
	public static final String CATEGORY_DISABLE = "disabled";

	public static final String CATEGORY_BALANCE_PLAYER_MOD = "balance_player_mod";
	public static final String CATEGORY_BALANCE_PLAYER_SKILLS="balance_player_skills";
	public static final String CATEGORY_BALANCE_LEVELING = "balance_leveling";
	public static final String CATEGORY_BALANCE_MOBPROP = "balance_mob_properties";
	public static final String CATEGORY_BALANCE_VVPROP = "balance_vv_properties";
	public static boolean village_gen_enabled;

	public static int village_density;

	public static int village_minDist;

	public static int village_size;
	
	public static int player_blood_watcher;
	
	public static boolean disable_vampire_biome;

	public static Configuration config;

	@SubscribeEvent
	public void onConfigurationChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
		if (e.modID.equalsIgnoreCase(REFERENCE.MODID)) {
			// Resync configs
			Logger.i("Configs", "Configuration has changed");
			Configs.loadConfiguration();
		}
	}
	
	public static void reset(){
		Logger.i("Configs", "Resetting config file");
		try {
			PrintWriter writer = new PrintWriter(config.getConfigFile());
			writer.print("");
			writer.close();
			init(config.getConfigFile(),false);
		} catch (Exception e) {
			Logger.e("Configs", "Failed to reset config file");
		}


	}
	
	private static boolean shouldResetInDev(){
		return config.get(CATEGORY_GENERAL, "reset_config_in_dev_env", true).getBoolean();
	}
	
	private static void setResetInDev(boolean v){
		config.get(CATEGORY_GENERAL, "reset_config_in_dev_env", v).set(v);
		config.save();
	}
	
	public static int getVampireBiomeId(){
		return config.getInt("vampirism_biome_id", CATEGORY_GENERAL, -1,-1, 1000, "If you set this to -1 the mod will try to find a free biome id");
	}
	
	public static void setVampireBiomeId(int i){
		config.get(CATEGORY_GENERAL, "vampirism_biome_id", -1).set(i);
		config.save();
	}

}
