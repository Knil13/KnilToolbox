package fr.knil.kniltoolbox.util;

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
	        	
			// commande /test
	            dispatcher.register(literal("givepoke").executes(ModCommands::givepoke));
		 });
	}
		 
	private static int givepoke(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayer();
		
		//donner un pokemon au joueur	
		
		player.sendMessage(Text.literal("Give pokemon"), false);

		return 1;
	}	
}
