package untamedwilds;

import net.minecraft.block.DispenserBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import untamedwilds.block.BlockCage;
import untamedwilds.compat.CompatBridge;
import untamedwilds.config.ConfigBase;
import untamedwilds.init.ModBlock;
import untamedwilds.init.ModEntity;
import untamedwilds.init.ModItems;
import untamedwilds.init.ModVillagerTrades;
import untamedwilds.util.ModEntityRightClickEvent;
import untamedwilds.world.UntamedWildsGenerator;

@Mod(value = UntamedWilds.MOD_ID)
public class UntamedWilds {

    // TODO: Migration AI, rare events executed by hungry mobs where they will choose a direction and keep moving there
    // TODO: Make use of Tags to make animal's diets data-driven?
    // TODO: Define list of diggables, maybe extend it to it's own weighted list and include Truffles and funsies
    // TODO: setupTamedAI() function is promising to trim unnecessary tasks from Tameable Mobs
    // TODO: Store the children's UUID in their mother's NBT, to allow checking for Children without constant AABB checking
    // BUG: Minecraft will complain about duplicate UUIDs when using Mob Spawn items in Creative mode (since they are not used up)
    // BUG: Creative/Sneaking players standing closest to an angry sleeper will cause other nearby players to be ignored

    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "untamedwilds";
    public static final boolean DEBUG = false;

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public UntamedWilds() {
        final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigBase.server_config);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigBase.client_config);
        eventBus.addListener(this::setupCommon);
        eventBus.addListener(this::setupClient);
        ConfigBase.loadConfig(ConfigBase.server_config, FMLPaths.CONFIGDIR.get().resolve("untamedwilds-server.toml").toString());
        ConfigBase.loadConfig(ConfigBase.client_config, FMLPaths.CONFIGDIR.get().resolve("untamedwilds-client.toml").toString());
        ModBlock.BLOCKS.register(eventBus);
        ModBlock.TILE_ENTITY_TYPES.register(eventBus);
        ModItems.registerSpawnItems();
        ModItems.ITEMS.register(eventBus);
        UntamedWildsGenerator.FEATURES.register(eventBus);
        CompatBridge.RegisterCompat();
        MinecraftForge.EVENT_BUS.register(ModVillagerTrades.class); // Custom Villager Trades
        MinecraftForge.EVENT_BUS.register(UntamedWildsGenerator.class); // Custom Biome Features
        MinecraftForge.EVENT_BUS.register(ModEntityRightClickEvent.class); // TODO: WIP solution because Wolves are really stupid, and there seems to be no way to 'this.' an entity through mixin
        UntamedWildsGenerator.readBioDiversityLevels();
    }

    private void setupCommon(final FMLCommonSetupEvent event) {
        DispenserBlock.registerDispenseBehavior(ModBlock.TRAP_CAGE.get().asItem(), new BlockCage.DispenserBehaviorTrapCage());
    }

    private void setupClient(final FMLClientSetupEvent event) {
        ModEntity.registerRendering();
        ModBlock.registerRendering();
    }
}
