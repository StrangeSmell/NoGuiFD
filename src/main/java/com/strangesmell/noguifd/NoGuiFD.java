package com.strangesmell.noguifd;

import com.mojang.datafixers.DSL;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.client.ClientSetup;

import static vectorwing.farmersdelight.common.registry.ModCreativeTabs.TAB_FARMERS_DELIGHT;

@Mod(NoGuiFD.MODID)
public class NoGuiFD
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "noguifd";

    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<Block> NGCookingPot = BLOCKS.register("no_gui_cooking_pot",NGCookingPotBlock::new);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> NGCookingPotItem = ITEMS.register("no_gui_cooking_pot_item",()->new NGCookingPotItem(NGCookingPot.get(),new Item.Properties().stacksTo(1)));


    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final RegistryObject<BlockEntityType<NGCookingPotBlockEntity>> NGCookingPotEntity = BLOCK_ENTITIES.register("no_gui_cooking_pot_entity", () -> BlockEntityType.Builder.of(NGCookingPotBlockEntity::new, NGCookingPot.get()).build(DSL.remainderType()));

    public static final DeferredRegister<MenuType<?>> NG_MENU = DeferredRegister.create(ForgeRegistries.MENU_TYPES, NoGuiFD.MODID);
    public static final RegistryObject<MenuType<NGCookingPotMenu>> COOKING_POT = NG_MENU.register("cooking_pot", () -> IForgeMenuType.create(NGCookingPotMenu::new));

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> NO_GUI_TAB = CREATIVE_MODE_TABS.register("no_gui_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> NGCookingPotItem.get().getDefaultInstance())
            .title( Component.translatable("NoGuiFD"))
            .displayItems((parameters, output) -> {
                output.accept(NGCookingPotItem.get());
            }).build());

    public NoGuiFD()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        NG_MENU.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::clientSetup);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
                () -> MenuScreens.register(COOKING_POT.get(),NGCookingPotScreen::new)
        );
    }

}
