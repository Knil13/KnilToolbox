package fr.knil.kniltoolbox;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Kniltoolbox implements ModInitializer {

    @Override
    public void onInitialize() {
        // Register cobblemon Pokemon captured events
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, this::onCaptured);


    }

    private Unit onCaptured(PokemonCapturedEvent pokemonCapturedEvent) {
        // Test recupération de l'event
        System.out.println("test" + pokemonCapturedEvent.getPokemon());
        // Test recupération du joueur
        ServerPlayerEntity player = pokemonCapturedEvent.getPlayer();
        // affichage du joueur dans la console
        System.out.println("test" + player);
        // affichage d'un message au joueur
        player.sendMessage(Text.of("Tu a récupéré un super pokémon de fou"), false);
        pokemonCapturedEvent.getPokeBallEntity().getType();
        // affichage du type de pokéball utilisé ( a tester ) 
        player.sendMessage(Text.of("Avec une"+ pokemonCapturedEvent.getPokeBallEntity().getType()), false);

        return Unit.INSTANCE;

    }
}
