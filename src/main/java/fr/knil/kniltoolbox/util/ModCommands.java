package fr.knil.kniltoolbox.util;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonEntities;

import kotlin.Unit;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.helditem.HeldItemManager;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.BattleBuilder;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.BattleTypes;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.CobblemonItem;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Nature;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.pokemon.helditem.BaseCobblemonHeldItemManager;
import com.cobblemon.mod.common.pokemon.helditem.CobblemonHeldItemManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

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
    
    private static final Gson GSON = new Gson();
    private static final Map<String, Pokemon> gift_data = new HashMap<>();
    private final static Map<String, List<UUID>> players_gifted = new HashMap<>();


	public static void registerCommands() {		
		
		loadGiftedPlayers();
		
		 CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> { 
	         // commande /pokelist
	            dispatcher.register(literal("pokelist").executes(ModCommands::pokelist));
	            
	         // commande /pokelist
	            dispatcher.register(literal("pokegiverandom").executes(ModCommands::pokegiverandom));
	            
	         // commande /pokelist
	            dispatcher.register(literal("test").executes(ModCommands::test));
	                  
	         // commande /pokelist
	            dispatcher.register(literal("BattleVsWild").executes(ModCommands::BattleVsWild));
	            
	         // commande /pokegift <key>
	            dispatcher.register(literal("pokegift")
	            	    .then(argument("key", string())
	            	        .executes(context -> pokegift(context, getString(context, "key"))))
	            	);	              
		 });
	}		
	
	
	private static int test(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		
		return 1;
	}
	
	
	private static int BattleVsWild(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayer();			   
		Pokemon poke = new Pokemon();
		PlayerPartyStore playerParty = Cobblemon.INSTANCE.getStorage().getParty(player);
		World world = context.getSource().getWorld();
		
		if(playerParty.occupied() != 0) {
		
		BattleRegistry br = Cobblemon.INSTANCE.getBattleRegistry();			
			
		poke.setSpecies(PokemonSpecies.INSTANCE.getByName("blissey"));	//configuration de l'espece	
		poke.setLevel(10);
		poke.setShiny(true);
		poke.setUuid(UUID.randomUUID()); //configuration de l'UUID	
		PokemonEntity PE = new PokemonEntity(world,poke,CobblemonEntities.POKEMON);
		PE.setPosition(player.getX()+2, player.getY(), player.getZ());
		world.spawnEntity(PE);		
		
		BattleBuilder.INSTANCE.pve(player, 
				PE,
				playerParty.get(0).getUuid())
				.ifSuccessful(battle -> {
            challengeBattles.add(battle); // Keep a list of challenge battles to keep track of cloned pokemon
            return Unit.INSTANCE;
        });	
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

