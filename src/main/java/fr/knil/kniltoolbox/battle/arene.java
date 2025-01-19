package fr.knil.kniltoolbox.battle;

import java.util.ArrayList;
import java.util.List;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.minecraft.server.network.ServerPlayerEntity;

public class arene {
	
	ElementalType arenaType;
	ServerPlayerEntity champion;
	List<Pokemon> pokemons = new ArrayList<>();
	
	
	public arene(ElementalType type, ServerPlayerEntity player) {
		// TODO Auto-generated constructor stub
		arenaType = type;
		champion = player;
		
	}
	
	
}
