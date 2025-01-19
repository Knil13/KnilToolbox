package fr.knil.kniltoolbox.battle;

import java.util.Set;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.battles.BattleBuilder;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.BattleRules;
import com.cobblemon.mod.common.battles.BattleTypes;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import fr.knil.kniltoolbox.util.MutablePosition;
import kotlin.Unit;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PokeBattle {	
	
	public PokeBattle() {
				
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
	
	public void BattlePvP(ServerWorld world, ServerPlayerEntity player1, ServerPlayerEntity player2, MutablePosition positionChallenger, MutablePosition positionChallenged) {
		int level = 10;
		Set<String> rules = Set.of(BattleRules.OBTAINABLE, BattleRules.PAST, BattleRules.UNOBTAINABLE, BattleRules.TEAM_PREVIEW);
		BattleFormat bf = new BattleFormat("cobblemon", BattleTypes.INSTANCE.getSINGLES(), rules,9,level);
				
		
		//Both intra-dimensional and cross-dimensional teleportation can now be performed using Entity#teleportTo 
		//(previously known as moveToWorld). TeleportTarget now contains the destination world and position.
		//FabricDimensions was removed, because its only API, teleport, is effectively superseded with Entity#teleportTo.
		
		// teleporter les joueurs face à face
		player1.teleport(world, positionChallenger.getX(), positionChallenger.getY(), positionChallenger.getZ(), positionChallenger.getYaw(), positionChallenger.getPitch());	
		player2.teleport(world, positionChallenged.getX(), positionChallenged.getY(), positionChallenged.getZ(), positionChallenged.getYaw(), positionChallenged.getPitch());	
	
		
		BattleBuilder.INSTANCE
		.pvp1v1(player1, 
				player2, 
				Cobblemon.INSTANCE.getStorage().getParty(player1).get(0).getUuid(), 
				Cobblemon.INSTANCE.getStorage().getParty(player2).get(0).getUuid(), 
				bf, 
				true, 
				true).ifSuccessful(battle -> {
					
			return Unit.INSTANCE;
        });		
	}
}
