package fr.knil.kniltoolbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import fr.knil.kniltoolbox.util.ModCommands;
import net.fabricmc.api.ModInitializer;


public class Kniltoolbox implements ModInitializer {
	public static final String MOD_ID = "kniltoolbox";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);	
	
    @Override
    public void onInitialize() {
    	LOGGER.info("Lancement de Knil ToolBox");
		ModCommands.registerCommands();	
		
		
        
    }    
}
