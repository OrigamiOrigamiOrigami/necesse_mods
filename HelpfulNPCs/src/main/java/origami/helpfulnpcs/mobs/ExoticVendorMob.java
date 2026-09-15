package origami.helpfulnpcs.mobs;

import necesse.engine.MusicOptions;
import necesse.engine.AbstractMusicList;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.BiomeRegistry;
import necesse.engine.registries.MusicRegistry;
import necesse.engine.seasons.GameSeasons;
import necesse.engine.sound.GameMusic;
import necesse.engine.util.GameBlackboard;
import necesse.engine.util.GameRandom;
import necesse.engine.util.TicketSystemList;
import necesse.entity.mobs.friendly.human.humanShop.SellingShopItem;
import necesse.inventory.InventoryItem;
import necesse.inventory.lootTable.presets.PaintingSelectionTable;
import necesse.level.maps.Level;
import necesse.level.maps.biomes.Biome;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Recruitable Exotic Merchant with the same shop shelf rules as vanilla.
 * Settler shop seed includes the world day, so random stock / meal / costume set
 * refresh daily. Uses settler id {@code hnexoticvendor} so vanilla
 * {@code exoticmerchant} visitors keep spawning normally.
 */
public class ExoticVendorMob extends HelpfulVendorMob {

    public ExoticVendorMob() {
        super("hnexoticvendor", 500);
        this.attackCooldown = 500;
        this.attackAnimTime = 500;
        setSwimSpeed(1.0f);
        equipmentInventory.setItem(6, new InventoryItem("ironsword"));
        registerExoticShop();
    }

    @Override
    protected Iterable<origami.helpfulnpcs.shop.ShopEntry> shopEntries() {
        // Shop is registered directly in constructor to match vanilla Exotic Merchant.
        return java.util.Collections.emptyList();
    }

    private void registerExoticShop() {
        SellingShopItem.ShopItemRequirement christmas = (random, client, shop, blackboard) -> GameSeasons.isChristmas();

        shop.addSellingItem("theeldersjinglejamvinyl", new SellingShopItem())
                .setRandomPrice(250, 350)
                .addRequirement(christmas);
        shop.addSellingItem("christmastree", new SellingShopItem())
                .setRandomPrice(600, 800)
                .addRequirement(christmas);
        shop.addSellingItem("christmaswreath", new SellingShopItem())
                .setRandomPrice(120, 150)
                .addRequirement(christmas);
        shop.addSellingItem("snowlauncher", new SellingShopItem())
                .setRandomPrice(600, 700)
                .addRequirement(christmas);
        shop.addSellingItem("greenwrappingpaper", new SellingShopItem())
                .setRandomPrice(20, 40)
                .addRequirement(christmas);
        shop.addSellingItem("bluewrappingpaper", new SellingShopItem())
                .setRandomPrice(20, 40)
                .addRequirement(christmas);
        shop.addSellingItem("redwrappingpaper", new SellingShopItem())
                .setRandomPrice(20, 40)
                .addRequirement(christmas);
        shop.addSellingItem("yellowwrappingpaper", new SellingShopItem())
                .setRandomPrice(20, 40)
                .addRequirement(christmas);

        shop.addSellingItem("rope", new SellingShopItem(10, 4))
                .setRandomPrice(150, 200);

        final ArrayList<String> meals = new ArrayList<String>(Arrays.asList(
                "cheeseburger", "tropicalstew", "minersstew", "chickencutletdish", "sushirolls",
                "parisiansteak", "dessertpancakes", "deepfriedchicken", "porktenderloin", "pumpkinpie"
        ));
        shop.addSellingItem("mealoftheday", new SellingShopItem(20, 4))
                .setItem((GameRandom random, ServerClient client, necesse.entity.mobs.friendly.human.humanShop.HumanShop shopMob, GameBlackboard blackboard) ->
                        new InventoryItem(random.getOneOf(meals)))
                .setRandomPrice(40, 60);

        shop.addSellingItem("piratemap", new SellingShopItem())
                .setRandomPrice(180, 220)
                .addRequirement((random, client, shopMob, blackboard) ->
                        !client.characterStats().biomes_visited.isBiomeVisited(BiomeRegistry.getBiome("piratevillage")));

        shop.addSellingItem("brainonastick", new SellingShopItem())
                .setRandomPrice(800, 1200);

        shop.addSellingItem("largerarepainting", new SellingShopItem())
                .setItem((GameRandom random, ServerClient client, necesse.entity.mobs.friendly.human.humanShop.HumanShop shopMob, GameBlackboard blackboard) ->
                        new InventoryItem(PaintingSelectionTable.getRandomLargeRarePaintingIDBasedOnWeight(random)))
                .setRandomPrice(300, 450);

        shop.addSellingItem("binoculars", new SellingShopItem())
                .setRandomPrice(200, 300)
                .addRandomAvailableRequirement(0.25f);
        shop.addSellingItem("boxingglovegun", new SellingShopItem())
                .setRandomPrice(200, 300)
                .addRandomAvailableRequirement(0.25f);
        shop.addSellingItem("recipebook", new SellingShopItem())
                .setRandomPrice(500, 800)
                .addRandomAvailableRequirement(0.25f);

        shop.addSellingItem("potionpouch", new SellingShopItem())
                .setRandomPrice(1000, 1200);
        shop.addSellingItem("prettyflower", new SellingShopItem())
                .setRandomPrice(600, 950);

        shop.addSellingItem("foolsgambit", new SellingShopItem())
                .setRandomPrice(1200, 1600)
                .addKilledMobRequirement("piratecaptain");
        shop.addSellingItem("ninjasmark", new SellingShopItem())
                .setRandomPrice(1200, 1600)
                .addKilledMobRequirement("piratecaptain");

        TicketSystemList<String> cosmetics = new TicketSystemList<String>();
        cosmetics.addObject(100, "jumpingball");
        cosmetics.addObject(100, "hula");
        cosmetics.addObject(100, "swim");
        cosmetics.addObject(100, "snow");
        cosmetics.addObject(100, "sailor");
        cosmetics.addObject(50, "jester");
        cosmetics.addObject(50, "space");

        addCosmetic("jumpingball", 600, 800, cosmetics, "jumpingball");
        addCosmetic("hulahat", 200, 400, cosmetics, "hula");
        addCosmetic("hulaskirtwithtop", 200, 400, cosmetics, "hula");
        addCosmetic("hulaskirt", 200, 400, cosmetics, "hula");
        addCosmetic("sunglasses", 300, 600, cosmetics, "swim");
        addCosmetic("swimsuit", 200, 400, cosmetics, "swim");
        addCosmetic("swimtrunks", 200, 400, cosmetics, "swim");
        addCosmetic("snowhood", 200, 400, cosmetics, "snow");
        addCosmetic("snowcloak", 200, 400, cosmetics, "snow");
        addCosmetic("snowboots", 200, 400, cosmetics, "snow");
        addCosmetic("sailorhat", 200, 400, cosmetics, "sailor");
        addCosmetic("sailorshirt", 200, 400, cosmetics, "sailor");
        addCosmetic("sailorshoes", 200, 400, cosmetics, "sailor");
        addCosmetic("jesterhat", 200, 400, cosmetics, "jester");
        addCosmetic("jestershirt", 200, 400, cosmetics, "jester");
        addCosmetic("jesterboots", 200, 400, cosmetics, "jester");
        addCosmetic("spacehelmet", 200, 400, cosmetics, "space");
        addCosmetic("spacesuit", 200, 400, cosmetics, "space");
        addCosmetic("spaceboots", 200, 400, cosmetics, "space");

        shop.addSellingItem("turban", new SellingShopItem()).setRandomPrice(150, 250);
        shop.addSellingItem("exoticshirt", new SellingShopItem()).setRandomPrice(150, 250);
        shop.addSellingItem("exoticshoes", new SellingShopItem()).setRandomPrice(150, 250);

        SellingShopItem.ShopItemRequirement musicPlayerChance = (random, client, shopMob, blackboard) -> {
            if (!blackboard.containsKey("addMusicPlayer")) {
                blackboard.set("addMusicPlayer", Boolean.valueOf(random.getChance(0.25f)));
            }
            return blackboard.getBoolean("addMusicPlayer");
        };
        shop.addSellingItem("musicplayer", new SellingShopItem())
                .setRandomPrice(800, 1200)
                .addRequirement(musicPlayerChance);
        shop.addSellingItem("portablemusicplayer", new SellingShopItem())
                .setRandomPrice(800, 1200)
                .addRequirement(musicPlayerChance);
        shop.addSellingItem("adventurebeginsvinyl", new SellingShopItem())
                .setRandomPrice(75, 125)
                .addRequirement(musicPlayerChance);
        shop.addSellingItem("homevinyl", new SellingShopItem())
                .setRandomPrice(75, 125)
                .addRequirement(musicPlayerChance);

        for (final GameMusic music : MusicRegistry.getMusic()) {
            String vinylId = music.getStringID() + "vinyl";
            SellingShopItem existing = shop.sellingShop.getItem(vinylId);
            if (existing == null) {
                existing = shop.addSellingItem(vinylId, new SellingShopItem())
                        .setRandomPrice(75, 125)
                        .addRequirement(musicPlayerChance);
            }
            existing.addRequirement((random, client, shopMob, blackboard) -> isBiomeMusic(music, shopMob, client));
        }
    }

    private void addCosmetic(String itemId, int minPrice, int maxPrice,
                             final TicketSystemList<String> cosmetics, final String setId) {
        shop.addSellingItem(itemId, new SellingShopItem())
                .setRandomPrice(minPrice, maxPrice)
                .addRequirement((random, client, shopMob, blackboard) -> {
                    String decided = blackboard.getString("decidedCosmetic");
                    if (decided == null) {
                        decided = cosmetics.getRandomObject(random);
                        blackboard.set("decidedCosmetic", decided);
                    }
                    return decided.equals(setId);
                });
    }

    private static boolean isBiomeMusic(GameMusic music,
                                        necesse.entity.mobs.friendly.human.humanShop.HumanShop shopMob,
                                        ServerClient client) {
        Level level = shopMob.getLevel();
        if (level == null || client == null || client.playerMob == null) {
            return false;
        }
        Biome biome = level.getBiome(shopMob.getTileX(), shopMob.getTileY());
        AbstractMusicList list = biome.getLevelMusic(level, client.playerMob);
        for (MusicOptions options : list.getMusicInList()) {
            if (options.music == music) {
                return true;
            }
        }
        return false;
    }
}
