package fr.knil.kniltoolbox.util;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.types.tera.TeraTypes;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


import static net.minecraft.server.command.CommandManager.literal;

import java.util.UUID;


public class ModCommands {

	

	public static void registerCommands() {
		 CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {            
	        	
			// commande /givepoke
	            dispatcher.register(literal("pokegive").executes(ModCommands::pokegive));
	            
	         // commande /test
	            dispatcher.register(literal("pokelist").executes(ModCommands::pokelist));
		 });
	}
		 
	
	private static int pokelist(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		//initialisation des variables
		ServerPlayerEntity player = context.getSource().getPlayer();	//recuperation du joueur
		PlayerPartyStore playerParty = Cobblemon.INSTANCE.getStorage().getParty(player); //recuperation de l'equipe du joueur	
		Pokemon poke;
		String pokeName;
		Boolean shiny;
		String isShiny = "";			
		
		//boucle for parcourant la liste des pokémon du joueur, slot par slot
		for(int i = 0; i <= 5; i++) {			
			poke = playerParty.get(i);	//recuperation du pokemon du slot i
			if (poke != null) {
				pokeName = poke.getSpecies().getName(); // recuperation du nom de son espece.
				shiny = poke.getShiny();	// recuperation du fait qu'il soit shiny ou non
				if (shiny) isShiny = " §e(shiny)";	// s'il est shiny, alors le rajouter dans la chaine
				else isShiny = "";					
				player.sendMessage(Text.literal("§bSlot " + (i+1) + " : " + pokeName + isShiny), false);	//afficher au joueur le pokemon du slot (avec une indication s'il est shiny ou non)
			}
			else player.sendMessage(Text.literal("§3Slot " + (i+1) + " : vide"), false); // afficher que le slot est vide
		}
		
		return 1;
	}
	
	
	private static int pokegive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		//Recuperation du joueur dans une var
		ServerPlayerEntity player = context.getSource().getPlayer();
		
		//recuperation de la team du joueur
		PlayerPartyStore playerParty = Cobblemon.INSTANCE.getStorage().getParty(player);	
			
		//creation du pokémon à give
		Pokemon poke = new Pokemon();		
		poke.setCaughtBall(PokeBalls.INSTANCE.getLOVE_BALL());	//configuration de la ball
		poke.setSpecies(PokemonSpecies.INSTANCE.getByName("pikachu"));	//configuration de l'espece		
		poke.setLevel(5); //configuration du niveau
		poke.setUuid(UUID.randomUUID()); //configuration de l'UUID		
		//configuration des IVs
		poke.setIV(Stats.ATTACK, 31);
		poke.setIV(Stats.DEFENCE, 31);
		poke.setIV(Stats.HP, 31);
		poke.setIV(Stats.SPECIAL_ATTACK, 31);
		poke.setIV(Stats.SPECIAL_DEFENCE, 31);
		poke.setIV(Stats.SPEED, 31);
		//configuration des EV
		poke.setEV(Stats.ATTACK, 0);
		poke.setEV(Stats.DEFENCE, 0);
		poke.setEV(Stats.HP, 0);
		poke.setEV(Stats.SPECIAL_ATTACK, 0);
		poke.setEV(Stats.SPECIAL_DEFENCE, 0);
		poke.setEV(Stats.SPEED, 0);	
		
		poke.setShiny(true);	//configuration du caractere shiny
		poke.setNature(Natures.INSTANCE.getTIMID());	//configuration de la nature	
		poke.setNickname(Text.literal("GiftMon"));		//configuration du surnom
		poke.setGender(Gender.MALE);	//configuration du genre
		poke.setOriginalTrainer(player.getUuid());	//configuration du dresseur d'origine
		poke.setTeraType(TeraTypes.getDRAGON());	//configuration du teraType
		
		//ajout du pokemon à la team : 
		playerParty.add(poke); //Attention, je ne verifie pas encore si la team est pleine ou non.
		player.sendMessage(Text.literal("§aCadeau : un " + poke.getSpecies().getName() + " §eshiny !!"), false);		

		return 1;
	}	
}
