package fr.knil.kniltoolbox.battle;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.battles.BattleBuilder;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import kotlin.Unit;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PokeBattle {
	public PokeBattle() {
		// TODO Auto-generated constructor stub
	}
	
	public void BattleVSWildPokemon(World world, ServerPlayerEntity player, Pokemon poke) {	
		//	generation de l'entité et positionnement
		PokemonEntity PE = new PokemonEntity(world,poke,CobblemonEntities.POKEMON);
		PE.setPosition(player.getX()+2, player.getY(), player.getZ());	// à 2 bloc du joueur
		world.spawnEntity(PE);	//spawn de l'entité dans le monde	
		
		//generation du combat
		BattleBuilder.INSTANCE.pve(player, 
				PE)
				.ifSuccessful(battle -> {
            return Unit.INSTANCE;
        });	
		
	}
	
	public void BattleVSWildPokemon(World world, ServerPlayerEntity player, Pokemon poke, Vec3d pos) {
//		generation de l'entité et positionnement
		PokemonEntity PE = new PokemonEntity(world,poke,CobblemonEntities.POKEMON);
		PE.setPosition(pos);	//choix de la position de spawn
		world.spawnEntity(PE);	//spawn de l'entité dans le monde	
		
		//generation du combat
		BattleBuilder.INSTANCE.pve(player, 
				PE)
				.ifSuccessful(battle -> {
            return Unit.INSTANCE;
        });	
		
	}
	
	
}
