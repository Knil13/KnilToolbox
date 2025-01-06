package fr.knil.kniltoolbox.util;


import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


import static net.minecraft.server.command.CommandManager.literal;


public class ModCommands {

	

	public static void registerCommands() {
		 CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {            
	        	
			// commande /givepoke
	            dispatcher.register(literal("givepoke").executes(ModCommands::givepoke));
	            
	         // commande /test
	            dispatcher.register(literal("testpoke").executes(ModCommands::test));
		 });
	}
		 
	private static int test(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
				
		ServerPlayerEntity player = context.getSource().getPlayer();		
		
		
		PlayerPartyStore PPS = new PlayerPartyStore(player.getUuid());
		Pokemon poke;
		String pokeName;
		
		player.sendMessage(Text.literal("test1"), false);
		poke = PPS.get(1);
		
		player.sendMessage(Text.literal("test2"), false);
		
		int pokelevel = poke.getLevel();
		
		player.sendMessage(Text.literal("test3"), false);
		
		/*pokeName = poke.getSpecies().getName();
		player.sendMessage(Text.literal(pokeName), false);*/
		
		/*for(int i = 0; i < 5; i++) {
			
			player.sendMessage(Text.literal("pokemon " + i + " : "), false);
			poke = PPS.get(i);	
			pokeName = poke.getSpecies().getName();
			player.sendMessage(Text.literal(pokeName), false);
		}*/
		
		
		return 1;
	}
	
	
	private static int givepoke(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayer();	
				
		Pokemon poke = new Pokemon();
		Species espece = PokemonSpecies.INSTANCE.getByName("Charmander");		
		
		poke.setSpecies(espece);;		
		
		
		poke.setLevel(5);
		
		poke.setIV(Stats.ATTACK, 31);
		poke.setIV(Stats.DEFENCE, 31);
		poke.setIV(Stats.HP, 31);
		poke.setIV(Stats.SPECIAL_ATTACK, 31);
		poke.setIV(Stats.SPECIAL_DEFENCE, 31);
		poke.setIV(Stats.SPEED, 31);
		
		poke.setEV(Stats.ATTACK, 0);
		poke.setEV(Stats.DEFENCE, 0);
		poke.setEV(Stats.HP, 0);
		poke.setEV(Stats.SPECIAL_ATTACK, 0);
		poke.setEV(Stats.SPECIAL_DEFENCE, 0);
		poke.setEV(Stats.SPEED, 0);
		
		poke.setShiny(true);
		poke.setNature(Natures.INSTANCE.getTIMID());
		
		poke.setNickname(Text.literal("GiftMon"));
		;
		
		PlayerPartyStore PPS = new PlayerPartyStore(player.getUuid());
		PPS.add(poke);		
		
		player.sendMessage(Text.literal("Tu as un cadeau :)"), false);

		return 1;
	}	
}
