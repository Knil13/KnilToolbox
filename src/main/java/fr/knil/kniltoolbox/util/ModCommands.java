package fr.knil.kniltoolbox.util;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Nature;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fr.knil.kniltoolbox.battle.PokeBattle;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;


public class ModCommands {

	public static Vector<PokemonBattle> challengeBattles = new Vector<>();
	
	private static final File DATA_DIRECTORY = new File("KnilToolbox_files"); // Répertoire des fichiers
    private static final File GIFT_FILE = new File(DATA_DIRECTORY, "gift_file.json"); // Fichier des gifts 
    private static final File PLAYER_GIFT_FILE = new File(DATA_DIRECTORY, "player_gift_file.json"); // Fichier des gifts 
    private static final File BATTLEPOSITION_FILE = new File(DATA_DIRECTORY, "BattlePosition.json"); // Fichier de la position des points de tp de bataille
    private static final File PARTIES_SAVED_FILE = new File(DATA_DIRECTORY, "PartiesSaved.json"); // Fichier des teams sauvegardées 
        
    private static final Gson GSON = new Gson();
    private static final Map<String, Pokemon> gift_data = new HashMap<>();
    private final static Map<String, List<UUID>> players_gifted = new HashMap<>();
    private static final Map<String, MutablePosition> BattlePosition = new HashMap<>();
    private static final Map<UUID, Map<String, List<PokemonTeamSlot>>> partiesSaved = new HashMap<>();
    
 // Classe pour représenter un slot d'équipe
    static class PokemonTeamSlot {       
        String species;
        UUID uuid;

        PokemonTeamSlot(String species, UUID uuid) {
            this.species = species;
            this.uuid = uuid;
        }
    }
    

	public static void registerCommands() {			
		loadGiftedPlayers();
		loadBattlePosition();
		loadPartyFromJson();
		
		
		 CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> { 
	         // commande /pokelist
	            dispatcher.register(literal("pokelist").executes(ModCommands::pokelist));
	            
	         // commande /pokegiverandom
	            dispatcher.register(literal("pokegiverandom").executes(ModCommands::pokegiverandom));
	            
	         
	                  
	         // commande /BattleVsWild
	            dispatcher.register(literal("BattleVsWild").executes(ModCommands::BattleVsWild));
	            
		     // commande /BattlePvP
		        dispatcher.register(literal("BattlePvP").then(argument("player", string())
            	        .executes(context -> BattlePvP(context, getString(context, "player"))))
            	);	
		        
	         // commande /pokegift <key>
	            dispatcher.register(literal("pokegift")
	            	    .then(argument("key", string())
	            	        .executes(context -> pokegift(context, getString(context, "key"))))
	            	);	      
	            
	         // commande /BattlePosition <key>
	            dispatcher.register(literal("BattlePosition")
	            	    .then(argument("side", string())
	            	        .executes(context -> setBattlePosition(context, getString(context, "side"))))
	            	);	
	            
	         // commande /saveparty <key>
	            dispatcher.register(literal("saveparty")
	            	    .then(argument("name", string())
	            	        .executes(context -> saveParty(context, getString(context, "name"))))
	            	);
	         // commande /loadparty <key>
	            dispatcher.register(literal("loadparty")
	            	    .then(argument("name", string())
	            	        .executes(context -> loadParty(context, getString(context, "name"))))
	            	);
	            
	         // commande /delparty <key>
	            dispatcher.register(literal("delparty")
	            	    .then(argument("name", string())
	            	        .executes(context -> delParty(context, getString(context, "name"))))
	            	);
	            
	         // commande /partyList
	            dispatcher.register(literal("test").executes(ModCommands::test));
	             
	            
	         // commande /partyList
	            dispatcher.register(literal("partyList").executes(ModCommands::partyList));
		 });
	}	
	
	private static int test(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayer();
		
		
		
		return 1;
	}
	
	private static int saveParty(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
	    ServerPlayerEntity player = context.getSource().getPlayer();
	    UUID playerId = player.getUuid();
	    PlayerPartyStore Party = Cobblemon.INSTANCE.getStorage().getParty(player);

	    // Créer la liste de Pokemon
	    List<PokemonTeamSlot> playerParty = new ArrayList<>();
	    for (int i=0;i<Party.size();i++)
	    {
	        Pokemon poke = Party.get(i);
	        if (poke != null) {
	    		playerParty.add(new PokemonTeamSlot(poke.getSpecies().toString(),poke.getUuid()));
	        }
	        else
	        {
	        	playerParty.add(new PokemonTeamSlot("aucun",null));
	        }
	    }
	    

	    // Vérifier si une map existe déjà pour ce joueur
	    Map<String, List<PokemonTeamSlot>> playerData = partiesSaved.getOrDefault(playerId, new HashMap<>());

	    // Remplacer ou ajouter l'équipe sauvegardée avec ce nom
	    playerData.put(name, playerParty);
	    partiesSaved.put(playerId, playerData);

	    // Sauvegarder dans le fichier JSON
	    savePartyInJson();

	    // Retourner un message au joueur
	    player.sendMessage(Text.literal("Team " + name + " sauvegardée !"), false);
	    return 1;
	}

	private static void savePartyInJson() {
	    if (PARTIES_SAVED_FILE.exists()) {
	    	Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Pretty print pour mieux lire le JSON
		    try (FileWriter writer = new FileWriter(PARTIES_SAVED_FILE)) {
		        gson.toJson(partiesSaved, writer);
		        System.out.println("Données sauvegardées dans " + PARTIES_SAVED_FILE);
		    } catch (IOException e) {
		        System.err.println("Erreur lors de la sauvegarde : " + e.getMessage());
		    }
	    } else {
	        System.out.println("Aucun fichier d'équipes trouvé. Création d'un nouveau fichier.");
	    }
	
}
	

	private static int loadParty(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayer();
        UUID playerId = player.getUuid();
		PlayerPartyStore Party = Cobblemon.INSTANCE.getStorage().getParty(player);
		PCStore PC = Cobblemon.INSTANCE.getStorage().getPC(player);
		
		if(name != null) {		
			List <Pokemon> PlayerParty = new ArrayList<Pokemon>();
			Party.forEach(poke -> PlayerParty.add(poke));		
			//partiesSaved.get(playerId).get(name).forEach(uuid -> System.out.println(uuid));		
		
			if (partiesSaved.containsKey(playerId)) {
				if (partiesSaved.get(playerId).containsKey(name)) {		
				
					for(int i = 0; i < partiesSaved.get(playerId).get(name).size(); i++) {
						UUID pokeUuid = partiesSaved.get(playerId).get(name).get(i).uuid;
						String specieString = partiesSaved.get(playerId).get(name).get(i).species;						
									
						if (specieString != "aucun") {						
							
							if(isInParty(Party, pokeUuid)) { //checké si le pokemon est dans la team
								//echanger les 2 pokemon
								int pos = getPositionInParty(Party, pokeUuid);
								System.out.println(Party.get(i).getSpecies()+"(slot "+i+") <=> "+specieString + "(slot "+pos+")");
								if (i != pos) Party.swap(i, pos);
							}
							else if (isInPC(PC, pokeUuid))//checké si le pokemon est dans le PC
							{
								PCPosition pos = GetPositionInPC(player, pokeUuid);
								switchIntoPC(player, i, pos);
							}
							else {
								player.sendMessage(Text.literal("Vous n'avez plus le pokemon "+specieString), false);
								Pokemon poke = Party.get(i);
								if (poke != null) {
									System.out.println("chargement d'un poké inexistant. Pokemon "+poke.getSpecies()+" envoyé dans le pc");								
									Party.remove(poke);
									PC.add(poke);
								}
								else System.out.println("chargement de slot vide sur un slot vide. rien ne se passe");
							}
						}
						else {
							Pokemon poke = Party.get(i);
							if (poke != null) {
								System.out.println("chargement de slot vide. Pokemon "+poke.getSpecies()+"envoyé dans le pc");								
								Party.remove(poke);
								PC.add(poke);
							}
							else System.out.println("chargement de slot vide sur un slot vide. rien ne se passe");
							
						}					
						
					}
					player.sendMessage(Text.literal("team "+name+" chargée !"), false);
				}
				else player.sendMessage(Text.literal("Vous n'avez pas de team " + name + " sauvegardée."), false);
			}
			else player.sendMessage(Text.literal("Vous n'avez pas de team sauvegardée."), false);	
		}
		else player.sendMessage(Text.literal("Vous n'avez pas entrez de nom de team"), false);
		
		return 1;
	}
	
	private static int getPositionInParty(PlayerPartyStore party, UUID uuid) {
	    
		for (int i=0;i<party.size();i++) {	
			Pokemon poke = party.get(i);
			
	        if (poke != null && poke.getUuid().equals(uuid)) {
	            return i; // Retourner immédiatement si l'UUID est trouvé
	        }
	    }
	    return 0;
	}
	
	private static boolean isInParty(PlayerPartyStore party, UUID uuid) {
	    for (Pokemon poke : party) {
	        if (poke.getUuid().equals(uuid)) {
	            return true; // Retourner immédiatement si l'UUID est trouvé
	        }
	    }
	    return false; // Retourner false si aucun match
	}
	
	private static boolean isInPC(PCStore PC, UUID uuid) {
		//check dans le PC					
		for(int i = 0; i < PC.getBoxes().size(); i++) {	
			for(int j = 0; j < 30; j++) {
				//System.out.println("check box "+i+" slot "+j);
				if (PC.get(new PCPosition(i, j)) != null) {		
					//System.out.println("pokemon in box "+i+" slot "+j);
					//System.out.println(PC.get(new PCPosition(i, j)).getUuid()+" = "+uuid+" ,");
					if (PC.get(new PCPosition(i, j)).getUuid().equals(uuid)) {						
						
						System.out.println("pokemon trouvé dans le pc in box "+i+" slot "+j);
						return true;
					}
				}
			}		
		}				
		return false;
	}
	
	private static PCPosition GetPositionInPC(ServerPlayerEntity player, UUID uuid) {
		
		//check dans le PC
		PCStore PC = Cobblemon.INSTANCE.getStorage().getPC(player);
		int box=0;
		int slot=0;
		boolean find = false;
		
		for(int i = 0; i < PC.getBoxes().size(); i++) {
			for(int j = 0; j < 30; j++) {
				if (PC.get(new PCPosition(i, j)) != null) {
					if (PC.get(new PCPosition(i, j)).getUuid().equals(uuid)){
						box = i;
						slot = j;
						find = true;
					}
				}
			}		
		}		
		if(find) return new PCPosition(box, slot);
		else return null;				
	}
	
	
	private static void switchIntoPC(ServerPlayerEntity player, int slot, PCPosition pos) {
		PlayerPartyStore Party = Cobblemon.INSTANCE.getStorage().getParty(player);
		PCStore PC = Cobblemon.INSTANCE.getStorage().getPC(player);
		
		if(Party.get(slot) != null) { //s'il y a un pokemon sur le slot
			
			Pokemon pokemonSlot = Party.get(slot);		
			Party.set(slot, PC.get(pos));
			PC.set(pos, pokemonSlot);
			System.out.println(Party.get(slot).getSpecies() + "(team slot "+ slot +") <=> "+ PC.get(pos).getSpecies()+" (PC slot "+pos.getSlot()+")");				
		}
		else { //si le slot est vide
			Pokemon poke = PC.get(pos);
			PC.remove(poke);
			Party.set(slot, poke);
			System.out.println(poke.getSpecies() + " => Team (slot " + slot +")");
		}		
	}
	
	private static void loadPartyFromJson() {
		    if (PARTIES_SAVED_FILE.exists()) {
		        Gson gson = new Gson();
		        try (FileReader reader = new FileReader(PARTIES_SAVED_FILE)) {
		            Type type = new TypeToken<Map<UUID, Map<String, List<PokemonTeamSlot>>>>() {}.getType();
		            Map<UUID, Map<String, List<PokemonTeamSlot>>> loadedData = gson.fromJson(reader, type);

		            if (loadedData != null) {
		                partiesSaved.putAll(loadedData);
		                System.out.println("Les équipes ont été chargées !");
		            }
		        } catch (IOException e) {
		            System.err.println("Erreur lors du chargement des équipes : " + e.getMessage());
		        }
		    } else {
		        System.out.println("Aucun fichier d'équipes trouvé. Création d'un nouveau fichier.");
		    }
		
	}
	
	private static int delParty(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayer();
        UUID playerId = player.getUuid();
		
		if(name != null) {		
			if (partiesSaved.containsKey(playerId)) {
				if (partiesSaved.get(playerId).containsKey(name)) {
					partiesSaved.get(playerId).remove(name);	
					savePartyInJson();
				}
				else player.sendMessage(Text.literal("Vous n'avez pas de team " + name + " sauvegardée."), false);
			}
			else player.sendMessage(Text.literal("Vous n'avez pas de team sauvegardée."), false);	
		}
		else player.sendMessage(Text.literal("Vous n'avez pas entrez de nom de team"), false);
		
		return 1;
	}
	
	private static int partyList(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayer();
        UUID playerId = player.getUuid();
		
		if (partiesSaved.containsKey(playerId)) {
			Map<String, List<PokemonTeamSlot>> playerTeams = partiesSaved.get(playerId);
			
			playerTeams.forEach((teamName, teamSlots) -> {
				StringBuilder teamDisplay = new StringBuilder(teamName + " : ");
	            
	            // Ajouter les noms des Pokémon de l'équipe
	            for (PokemonTeamSlot slot : teamSlots) {
	                if (slot != null && slot.species != "aucun") {
	                    teamDisplay.append(slot.species).append(", ");
	                }
	            }
	            
	            // Supprimer la dernière virgule et l'espace, puis afficher
	            if (teamDisplay.length() > 2) {
	                teamDisplay.setLength(teamDisplay.length() - 2);
	            }
	            player.sendMessage(Text.literal(teamDisplay.toString()), false);
			});
			
		}
		else player.sendMessage(Text.literal("Vous n'avez pas de team sauvegardée."), false);			
		
		return 1;
	}
	
	private static int setBattlePosition(CommandContext<ServerCommandSource> context, String side) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayer();
		BlockPos currentPos = player.getBlockPos();
    
		MutablePosition tp = new MutablePosition(currentPos.getX(), currentPos.getY(), currentPos.getZ(), player.getYaw(), player.getPitch());

		BattlePosition.put(side, tp);
    
		// Sauvegarder les changements dans le fichier
		saveBattlePosition();     
		
		player.sendMessage(Text.literal("point de tp " + side + "sauvegardé !"), false);
    
		return 1;
	}
	
	private static void saveBattlePosition() {
    	// Sauvegarder dans le fichier
        try (FileWriter writer = new FileWriter(BATTLEPOSITION_FILE)) {
            Map<String, Map<String, Object>> rawData = new HashMap<>();
            BattlePosition.forEach((key, pos) -> {
                Map<String, Object> coords = new HashMap<>();
                coords.put("x", pos.getX());
                coords.put("y", pos.getY());
                coords.put("z", pos.getZ());
                coords.put("yaw", pos.getYaw());
                coords.put("pitch", pos.getPitch()); 
                rawData.put(key, coords);
            });
            GSON.toJson(rawData, writer);
        } catch (IOException e) {
            System.err.println("Failed to save BattlePoint : " + e.getMessage());
        }
    }
	
	private static void loadBattlePosition() {
	    if (BATTLEPOSITION_FILE.exists()) {
	        try (FileReader reader = new FileReader(BATTLEPOSITION_FILE)) {
	            Type type = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
	            Map<String, Map<String, Object>> rawData = GSON.fromJson(reader, type);

	            // Convertir les données en BlockPos
	            rawData.forEach((name, coords) -> {
	                if (coords.containsKey("x") && coords.containsKey("y") && coords.containsKey("z")) {
	                    // Conversion sécurisée des coordonnées
	                    int x = ((Number) coords.get("x")).intValue();
	                    int y = ((Number) coords.get("y")).intValue();
	                    int z = ((Number) coords.get("z")).intValue();
	                    float yaw = coords.containsKey("yaw") ? ((Number) coords.get("yaw")).floatValue() : 0.0f;
	                    float pitch = coords.containsKey("pitch") ? ((Number) coords.get("pitch")).floatValue() : 0.0f;

	                    // Ajout à la collection
	                    BattlePosition.put(name, new MutablePosition(x, y, z, yaw, pitch));
	                }
	            });
	        } catch (IOException e) {
	            System.err.println("Failed to load battle positions: " + e.getMessage());
	        }
	    }
	}

	
	private static int BattlePvP(CommandContext<ServerCommandSource> context, String playerChallenged) throws CommandSyntaxException {
		
		ServerPlayerEntity player1 = context.getSource().getPlayer();
		ServerPlayerEntity player2 = context.getSource().getServer().getPlayerManager().getPlayer(playerChallenged);
		
		PlayerPartyStore player1Party = Cobblemon.INSTANCE.getStorage().getParty(player1);				
		PlayerPartyStore player2Party = Cobblemon.INSTANCE.getStorage().getParty(player2);				
		

		ServerWorld worldServer = context.getSource().getWorld();
		
		if(player1Party.occupied() != 0 & player2Party.occupied() != 0) {			
			if(BattlePosition.containsKey("challenger") & BattlePosition.containsKey("challenged")) {
				PokeBattle PB = new PokeBattle();
				PB.BattlePvP(worldServer, player1, player2, BattlePosition.get("challenger"), BattlePosition.get("challenged"));
			}
			else
			{
				player1.sendMessage(Text.literal("un ou plusieurs point de tp n'est pas initialisé"), false);
			}
		}
		else player1.sendMessage(Text.literal("l'un des joueur n'a pas de pokemon"), false);
		
		return 1;
	}
	
	
	private static int BattleVsWild(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayer();
		PlayerPartyStore playerParty = Cobblemon.INSTANCE.getStorage().getParty(player);				
		
		if(playerParty.occupied() != 0) {	// si la team du joueur n'est pas vide
			
		//creation du poké à faire spawn
		Pokemon poke = new Pokemon();
		poke = CreatePokemon(PokemonSpecies.INSTANCE.getByName("blissey"));	//creer un leuphorie
		poke.setLevel(5);	// met son niveau à 5	S
		
		//utilisation de PokeBattle pour utilisé sa fonction qui genere le combat (voir dans fr.knil.kniltoolbox.battle.PokeBattle)
		PokeBattle PB = new PokeBattle();
		PB.BattleVSWildPokemon(context.getSource().getWorld(), player, poke);		
		}
		else player.sendMessage(Text.literal("Tu n'as pas de pokemon"), false);
		
		return 1;
	}
	

	// fonction qui liste les pokemons de l'equipe du joueur
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
	
	// fonction qui add un pokemon selon un code rentré prealablement.
	private static int pokegift(CommandContext<ServerCommandSource> context, String key) {
		loadgift();	// charge le fichier dans la var gift_data
		
		//Recuperation du joueur
		ServerPlayerEntity player = context.getSource().getPlayer();
		
		if(!containsUUID(key,player.getUuid())) {		
		
		//recuperation de la team du joueur
		PlayerPartyStore playerParty = Cobblemon.INSTANCE.getStorage().getParty(player);
		
		
		if (gift_data.containsKey(key)) {	//verification que le code rentré correspond à un pokemon
			//creation du pokémon à give
			Pokemon poke = gift_data.get(key);	//charge le pokemon correspondant au code
				
			//ajout du pokemon à la team  
			playerParty.add(poke);	// NB : quand la team est pleine, il va directement dans le pc. 
			player.sendMessage(Text.literal("§aCadeau : un " + poke.getSpecies().getName()), false);	
			addPlayerGifted(key,player.getUuid());
			saveGiftedPlayers();
		}
		else 
			player.sendMessage(Text.literal("Le code n'existe pas"));
		}
		else player.sendMessage(Text.literal("Tu as deja utiliser ce code."));
		return 1;
	}	
	
	//petite fonction que genere le pokemon avec son UUID
	private static Pokemon CreatePokemon(Species spec)  {
		
		Pokemon poke = new Pokemon();
			
		poke.setSpecies(spec);	//configuration de l'espece	
		poke.setUuid(UUID.randomUUID()); //configuration de l'UUID		
		
		return poke;
	}
	
	//fonction qui permet de give un pokemon aleatoire
	private static int pokegiverandom(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		//Recuperation du joueur dans une var
		ServerPlayerEntity player = context.getSource().getPlayer();
		
		//Recuperation de la team du joueur
		PlayerPartyStore playerParty = Cobblemon.INSTANCE.getStorage().getParty(player);	
			
		//Creation du pokémon à give
		Species espece = PokemonSpecies.INSTANCE.random();
		Pokemon poke = CreatePokemon(espece);
		String isShiny = "";
		
		//10% de chance que le pokemon soit shiny
		int randomInt = (int) (Math.random() * 101);
    	//Si le nombre est superieur à 50, le pokemon devient shiny.
    	if(randomInt>90) {
    		//changement du pokémon en shiny
    		poke.setShiny(true);
    		isShiny = " §e shiny !!";
    	}    	
		
		//ajout du pokemon à la team : 
		playerParty.add(poke); 
		player.sendMessage(Text.literal("§aCadeau : un " + poke.getSpecies().getName() + isShiny), false);		

		return 1;
	}
	
	//chargement du fichier GIFT_FILE dans la var gift_data
	@SuppressWarnings("unchecked")
	private static void loadgift() {
		gift_data.clear();
		if (GIFT_FILE.exists()) {	//verification que le fichier existe
            try (FileReader reader = new FileReader(GIFT_FILE)) {	
                Type type = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
                Map<String, Map<String, Object>> rawData = GSON.fromJson(reader, type); //charger le fichier dans une var

                rawData.forEach((key, pokedata) -> { //parcourir la var rawdata clé par clé
                	Pokemon pokegift = new Pokemon();	//la variable qui sera chargé dans gift_data
                	Species spec = new Species();
                	
                	//Verifier que l'espece du pokemon est chargé. Si non, il sera aleatoire.
                	if (pokedata.containsKey("species")) {                		
                		spec = PokemonSpecies.INSTANCE.getByName((String) pokedata.get("species"));
               	 	}
                	else {
                		spec = PokemonSpecies.INSTANCE.random();
                	}
                	
                	//creation du pokémon avec son espece et un UUID
                	pokegift = CreatePokemon(spec);  
                	
                	if (pokedata.containsKey("shiny")) {	//si il y a un champ "shiny", assigner la valeur correspondante au poké               		
                		 pokegift.setShiny(Boolean.parseBoolean((String) pokedata.get("shiny")));                		 
                	 }
                	 if (pokedata.containsKey("pokeball")) {	//si il y a un champ "pokeball", assigner la valeur correspondante au poké comme ball de capture
                		 pokegift.setCaughtBall(getPokeball((String) pokedata.get("pokeball")));
                	 }          	 
                	 if (pokedata.containsKey("level")) {	//si il y a un champ "level", assigner la valeur correspondante au poké comme niveau
                		 double AsDouble = (double) pokedata.get("level");
                		 pokegift.setLevel((int) AsDouble);
                	 }
                	 if (pokedata.containsKey("IV_HP")) {	//si il y a un champ "IV_HP", assigné la valeur correspondante au poké comme IV au PV
                		 double AsDouble = (double) pokedata.get("IV_HP");
                		 pokegift.setIV(Stats.HP, (int) AsDouble);
                	 }
                	 if (pokedata.containsKey("IV_ATK")) {	//si il y a un champ "IV_ATK", assigné la valeur correspondante au poké comme IV à l'attaque
                		 double AsDouble = (double) pokedata.get("IV_ATK");
                		 pokegift.setIV(Stats.ATTACK, (int) AsDouble);
                	 }
                	 if (pokedata.containsKey("IV_DEF")) {	//si il y a un champ "IV_DEF", assigné la valeur correspondante au poké comme IV à la defense
                		 double AsDouble = (double) pokedata.get("IV_DEF");
                		 pokegift.setIV(Stats.DEFENCE, (int) AsDouble);
                	 }
                	 if (pokedata.containsKey("IV_ATKS")) {	//si il y a un champ "IV_ATKS", assigné la valeur correspondante au poké comme IV à l'attaque spé
                		 double AsDouble = (double) pokedata.get("IV_ATKS");
                		 pokegift.setIV(Stats.SPECIAL_ATTACK, (int) AsDouble);
                	 }
                	 if (pokedata.containsKey("IV_DEFS")) {	//si il y a un champ "IV_DEFS", assigné la valeur correspondante au poké comme IV à la defense spé
                		 double AsDouble = (double) pokedata.get("IV_DEFS");
                		 pokegift.setIV(Stats.SPECIAL_DEFENCE, (int) AsDouble);
                	 }
                	 if (pokedata.containsKey("IV_SPD")) {	//si il y a un champ "IV_SPD", assigné la valeur correspondante au poké comme IV à la vitesse
                		 double AsDouble = (double) pokedata.get("IV_SPD");
                		 pokegift.setIV(Stats.SPEED, (int) AsDouble);
                	 }
                	 if (pokedata.containsKey("EV_HP")) {	//si il y a un champ "EV_HP", assigné la valeur correspondante au poké comme EV au PV
                		 double AsDouble = (double) pokedata.get("EV_HP");
                		 pokegift.setEV(Stats.HP, (int) AsDouble);
                	 }
                	 if (pokedata.containsKey("EV_ATK")) { 	//si il y a un champ "EV_ATK", assigné la valeur correspondante au poké comme EV à l'attaque
                		 double AsDouble = (double) pokedata.get("EV_ATK");
                		 pokegift.setEV(Stats.ATTACK, (int) AsDouble);
                	 }
                	 if (pokedata.containsKey("EV_DEF")) {	//si il y a un champ "EV_DEF", assigné la valeur correspondante au poké comme EV à la defense
                		 double AsDouble = (double) pokedata.get("EV_DEF");
                		 pokegift.setEV(Stats.DEFENCE, (int) AsDouble);
                	 }
                	 if (pokedata.containsKey("EV_ATKS")) {	//si il y a un champ "EV_ATKS", assigné la valeur correspondante au poké comme EV à l'attaque spé
                		 double AsDouble = (double) pokedata.get("EV_ATKS");
                		 pokegift.setEV(Stats.SPECIAL_ATTACK, (int) AsDouble);
                	 }
                	 if (pokedata.containsKey("EV_DEFS")) {	//si il y a un champ "EV_DEFS", assigné la valeur correspondante au poké comme EV à la defense spé
                		 double AsDouble = (double) pokedata.get("EV_DEFS");
                		 pokegift.setEV(Stats.SPECIAL_DEFENCE, (int) AsDouble);
                	 }
                	 if (pokedata.containsKey("EV_SPD")) {	//si il y a un champ "EV_SPD", assigné la valeur correspondante au poké comme EV à la vitesse
                		 double AsDouble = (double) pokedata.get("EV_SPD");
                		 pokegift.setEV(Stats.SPEED, (int) AsDouble);
                	 }
                	 if (pokedata.containsKey("Nature")) {	//si il y a un champ "Nature", assigné la valeur correspondante au poké comme nature du pokémon
                		 pokegift.setNature(getNature((String) pokedata.get("Nature")));
                	 }
                	 if (pokedata.containsKey("Surnom")) {	//si il y a un champ "Surnom", assigné la valeur correspondante au poké comme surnom du pokémon
                		 pokegift.setNickname(Text.literal((String) pokedata.get("Surnom")));
                	 }
                	 if (pokedata.containsKey("Gender")) {	//si il y a un champ "Gender", assigné la valeur correspondante au poké comme genre du pokémon
                		 String gender = (String) pokedata.get("Gender");
                		 switch (gender.toUpperCase()) {
                		 	case "MALE" -> pokegift.setGender(Gender.MALE);
                		 	case "FEMALE" -> pokegift.setGender(Gender.FEMALE);
                		 	case "GENDERLESS" -> pokegift.setGender(Gender.GENDERLESS);
                		 	
                		 	default -> 
             		 		throw new IllegalArgumentException("Unexpected value: " + gender);
                		 }
                	 }
                	 if (pokedata.containsKey("OT")) {	//si il y a un champ "OT", assigné la valeur correspondante au poké comme dresseur d'origine
                		 pokegift.setOriginalTrainer((String) pokedata.get("OT"));
                	 }                	 
                	 if (pokedata.containsKey("Moveset")) {	//pour plus tard, essayer de set le moveset du poké
                		 MoveSet MS = new MoveSet();
                		 
                		 List <String> movesetStr = (List<String>) pokedata.get("Moveset");
                                         		 
                		 //mettre la liste de move dans le moveset
                		 for (String moveStr : movesetStr) {
                			 MS.add(Moves.INSTANCE.getByName(moveStr).create());                			                 			 
                         } 
                		 
                		 //rajouter le moveset au pokemon
                		 pokegift.getMoveSet().copyFrom(MS);
                		 
                	 }
                	 if (pokedata.containsKey("HeldItem")) {	//pour plus tard, essayer de set l'objet tenu du poké
                		 pokegift.swapHeldItem(CobblemonItems.ABILITY_PATCH.getDefaultStack(), false);
                		 
                	 }
                	
                	// Ajouter la clé et le pokemon dans gift_data
                	gift_data.put(key, pokegift);
                });       
                
            } catch (IOException e) {
                System.err.println("Failed to load spawn points: " + e.getMessage());
            }
        }
    }

	public static ItemStack getItem(String item) {		
		ItemStack itemStack;
		switch (item.toUpperCase()) {
		case "ABILITY_PATCH" -> itemStack = CobblemonItems.ABILITY_PATCH.getDefaultStack();
		
		
		default -> 
	 	throw new IllegalArgumentException("Unexpected value: " + item);
	}
	
	return itemStack;
	}
	
	//obtenir l'objet de type Pokeball à partir d'une chaine
	public static PokeBall getPokeball(String ball) {		
		PokeBall pokeball;		
		
		switch (ball.toUpperCase()) {
			case "POKE_BALL" -> pokeball = PokeBalls.INSTANCE.getPOKE_BALL();
			case "SLATE_BALL" -> pokeball = PokeBalls.INSTANCE.getSLATE_BALL();
			case "AZURE_BALL" -> pokeball = PokeBalls.INSTANCE.getAZURE_BALL();
			case "VERDANT_BALL" -> pokeball = PokeBalls.INSTANCE.getVERDANT_BALL();
			case "ROSEATE_BALL" -> pokeball = PokeBalls.INSTANCE.getROSEATE_BALL();
			case "CITRINE_BALL" -> pokeball = PokeBalls.INSTANCE.getCITRINE_BALL();
			case "GREAT_BALL" -> pokeball = PokeBalls.INSTANCE.getGREAT_BALL();
			case "ULTRA_BALL" -> pokeball = PokeBalls.INSTANCE.getULTRA_BALL();
			case "MASTER_BALL" -> pokeball = PokeBalls.INSTANCE.getMASTER_BALL();
			case "SAFARI_BALL" -> pokeball = PokeBalls.INSTANCE.getSAFARI_BALL();
			case "FAST_BALL" -> pokeball = PokeBalls.INSTANCE.getFAST_BALL();
			case "LEVEL_BALL" -> pokeball = PokeBalls.INSTANCE.getLEVEL_BALL();
			case "LURE_BALL" -> pokeball = PokeBalls.INSTANCE.getLURE_BALL();
			case "HEAVY_BALL" -> pokeball = PokeBalls.INSTANCE.getHEAVY_BALL();
			case "LOVE_BALL" -> pokeball = PokeBalls.INSTANCE.getLOVE_BALL();
			case "FRIEND_BALL" -> pokeball = PokeBalls.INSTANCE.getFRIEND_BALL();
			case "MOON_BALL" -> pokeball = PokeBalls.INSTANCE.getMOON_BALL();
			case "SPORT_BALL" -> pokeball = PokeBalls.INSTANCE.getSPORT_BALL();
			case "NET_BALL" -> pokeball = PokeBalls.INSTANCE.getNET_BALL();
			case "DIVE_BALL" -> pokeball = PokeBalls.INSTANCE.getDIVE_BALL();
			case "NEST_BALL" -> pokeball = PokeBalls.INSTANCE.getNEST_BALL();
			case "REPEAT_BALL" -> pokeball = PokeBalls.INSTANCE.getREPEAT_BALL();
			case "TIMER_BALL" -> pokeball = PokeBalls.INSTANCE.getTIMER_BALL();
			case "LUXURY_BALL" -> pokeball = PokeBalls.INSTANCE.getLUXURY_BALL();
			case "PREMIER_BALL" -> pokeball = PokeBalls.INSTANCE.getPREMIER_BALL();
			case "DUSK_BALL" -> pokeball = PokeBalls.INSTANCE.getDUSK_BALL();
			case "HEAL_BALL" -> pokeball = PokeBalls.INSTANCE.getHEAL_BALL();
			case "QUICK_BALL" -> pokeball = PokeBalls.INSTANCE.getQUICK_BALL();
			case "CHERISH_BALL" -> pokeball = PokeBalls.INSTANCE.getCHERISH_BALL();
			case "PARK_BALL" -> pokeball = PokeBalls.INSTANCE.getPARK_BALL();
			case "DREAM_BALL" -> pokeball = PokeBalls.INSTANCE.getDREAM_BALL();
			case "BEAST_BALL" -> pokeball = PokeBalls.INSTANCE.getBEAST_BALL();
			case "ANCIENT_POKE_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_POKE_BALL();
			case "ANCIENT_CITRINE_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_CITRINE_BALL();
			case "ANCIENT_VERDANT_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_VERDANT_BALL();
			case "ANCIENT_AZURE_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_AZURE_BALL();
			case "ANCIENT_ROSEATE_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_ROSEATE_BALL();
			case "ANCIENT_SLATE_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_SLATE_BALL();
			case "ANCIENT_IVORY_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_IVORY_BALL();
			case "ANCIENT_GREAT_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_GREAT_BALL();
			case "ANCIENT_ULTRA_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_ULTRA_BALL();
			case "ANCIENT_HEAVY_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_HEAVY_BALL();
			case "ANCIENT_LEADEN_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_LEADEN_BALL();
			case "ANCIENT_GIGATON_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_GIGATON_BALL();
			case "ANCIENT_FEATHER_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_FEATHER_BALL();
			case "ANCIENT_WING_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_WING_BALL();
			case "ANCIENT_JET_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_JET_BALL();
			case "ANCIENT_ORIGIN_BALL" -> pokeball = PokeBalls.INSTANCE.getANCIENT_ORIGIN_BALL();
		 	
		 	default -> 
		 	throw new IllegalArgumentException("Unexpected value: " + ball);
		}
		
		return pokeball;
	}

	//obtenir l'objet de type Nature à partir d'une chaine
	public static Nature getNature(String natureString) {		
		Nature nature;
		
		switch (natureString.toUpperCase()) {
			case "HARDY" -> nature = Natures.INSTANCE.getHARDY();
			case "LONELY" -> nature = Natures.INSTANCE.getLONELY();
			case "BRAVE" -> nature = Natures.INSTANCE.getBRAVE();
			case "ADAMANT" -> nature = Natures.INSTANCE.getADAMANT();
			case "NAUGHTY" -> nature = Natures.INSTANCE.getNAUGHTY();
			case "BOLD" -> nature = Natures.INSTANCE.getBOLD();
			case "DOCILE" -> nature = Natures.INSTANCE.getDOCILE();
			case "RELAXED" -> nature = Natures.INSTANCE.getRELAXED();
			case "IMPISH" -> nature = Natures.INSTANCE.getIMPISH();
			case "LAX" -> nature = Natures.INSTANCE.getLAX();
			case "TIMID" -> nature = Natures.INSTANCE.getTIMID();
			case "HASTY" -> nature = Natures.INSTANCE.getHASTY();
			case "SERIOUS" -> nature = Natures.INSTANCE.getSERIOUS();
			case "JOLLY" -> nature = Natures.INSTANCE.getJOLLY();
			case "NAIVE" -> nature = Natures.INSTANCE.getNAIVE();
			case "MODEST" -> nature = Natures.INSTANCE.getMODEST();
			case "MILD" -> nature = Natures.INSTANCE.getMILD();
			case "QUIET" -> nature = Natures.INSTANCE.getQUIET();
			case "BASHFUL" -> nature = Natures.INSTANCE.getBASHFUL();
			case "RASH" -> nature = Natures.INSTANCE.getRASH();
			case "CALM" -> nature = Natures.INSTANCE.getCALM();
			case "GENTLE" -> nature = Natures.INSTANCE.getGENTLE();
			case "SASSY" -> nature = Natures.INSTANCE.getSASSY();
			case "CAREFUL" -> nature = Natures.INSTANCE.getCAREFUL();
			case "QUIRKY" -> nature = Natures.INSTANCE.getQUIRKY();
		 	
		 	default -> 
		 	throw new IllegalArgumentException("Unexpected value: " + natureString);
		}
		
		return nature;
	}
	
	// Charger les données depuis le fichier JSON
	public static void loadGiftedPlayers() {
        if (PLAYER_GIFT_FILE.exists()) {
            try (FileReader reader = new FileReader(PLAYER_GIFT_FILE)) {            	
            	
                @SuppressWarnings("unchecked")
				Map<String, List<String>> rawData = GSON.fromJson(reader, Map.class);
                if (rawData != null) {
                    rawData.forEach((keyword, uuidStrings) -> {
                        List<UUID> uuids = new ArrayList<>();
                        for (String uuidString : uuidStrings) {
                            uuids.add(UUID.fromString(uuidString));
                        }
                        players_gifted.put(keyword, uuids);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void saveGiftedPlayers() {
        try (FileWriter writer = new FileWriter(PLAYER_GIFT_FILE)) {
            Map<String, List<String>> rawData = new HashMap<>();
            players_gifted.forEach((keyword, uuids) -> {
                List<String> uuidStrings = new ArrayList<>();
                for (UUID uuid : uuids) {
                    uuidStrings.add(uuid.toString());
                }
                rawData.put(keyword, uuidStrings);
            });
            GSON.toJson(rawData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
 // Ajouter un UUID à un mot-clé
    public static void addPlayerGifted(String keyword, UUID uuid) {
    	players_gifted.computeIfAbsent(keyword, k -> new ArrayList<>()).add(uuid);
    }
    
    public static boolean containsUUID(String keyword, UUID uuid) {
        List<UUID> uuids = players_gifted.get(keyword);
        return uuids != null && uuids.contains(uuid);
    }
	
}

