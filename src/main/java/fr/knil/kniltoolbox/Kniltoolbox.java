package fr.knil.kniltoolbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.pokemon.Pokemon;

import fr.knil.kniltoolbox.util.ModCommands;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


public class Kniltoolbox implements ModInitializer {
	public static final String MOD_ID = "kniltoolbox";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);	
	
    @Override
    public void onInitialize() {
    	LOGGER.info("Lancement de Knil ToolBox");
		ModCommands.registerCommands();	
		
		//abonnement à l'event de capture de poké
		CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, this::onCaptured);	
		
		
        
    }      
    
    private Unit onCaptured(PokemonCapturedEvent pokemonCapturedEvent) {
    	//variable stockant le joueur ayant capturer un poké
    	ServerPlayerEntity player = pokemonCapturedEvent.getPlayer();
    	
    	//Variable contenant le pokemon capturé
    	Pokemon poke = pokemonCapturedEvent.getPokemon();
    	
    	//generation d'un nombre aleatoire entre 0 et 100;
    	int randomInt = (int) (Math.random() * 101);
    	//Si le nombre est superieur à 50, le pokemon devient shiny.
    	if(randomInt>50) {
    		//changement du pokémon en shiny
    		poke.setShiny(true);
    		player.sendMessage(Text.of("Bravo ! ton pokemon est devenu shiny !"), false);
    	}

        return Unit.INSTANCE;

    }
}
