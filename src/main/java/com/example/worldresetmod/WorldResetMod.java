
//--------------- build.gradle (forge 1.18.2) ---------------
// Quick Gradle build script skeleton – adjust group/version as needed
plugins {
    id 'java'
    id 'net.minecraftforge.gradle' version '5.1.74'
}

version = '1.0.0'
group = 'com.example.worldresetmod'
archivesBaseName = 'worldresetmod'

def mcVersion = '1.18.2'

def forgeVersion = '40.2.10' // latest recommended for 1.18.2

minecraft {
    mappings channel: 'official', version: mcVersion
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft "net.minecraftforge:forge:${mcVersion}-${forgeVersion}"
}

//--------------- src/main/java/com/example/worldresetmod/WorldResetMod.java ---------------
package com.example.worldresetmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Mod("worldresetmod")
public class WorldResetMod {
    public static final String MODID = "worldresetmod";
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> RESET_ORB = ITEMS.register("reset_orb",
            () -> new Item(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC)));

    public WorldResetMod() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (!event.getWorld().isClientSide && event.getItemStack().getItem() == RESET_ORB.get()) {
            MinecraftServer server = event.getWorld().getServer();
            if (server != null) {
                server.getPlayerList().broadcastMessage(new TextComponent("§c[!] Reset triggered! Server shutting down..."), false);
                try {
                    Files.writeString(Path.of("reset.flag"), "true");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                server.halt(true);
            }
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        File flagFile = new File("reset.flag");
        if (flagFile.exists()) {
            try {
                // delete old world
                File worldDir = server.getWorldPath(ServerLevel.OVERWORLD).toFile().getParentFile();
                for (File file : worldDir.listFiles()) {
                    deleteRecursively(file);
                }

                // delete flag
                flagFile.delete();

                // regenerate world
                server.getPlayerList().broadcastMessage(new TextComponent("§aWorld regenerated."), false);

                // copy datapack into new world folder
                File datapackZip = new File("./datapacks/pvp_timer_lava_fullreplace.zip");
                File datapackDest = new File(worldDir, "datapacks/pvp_timer_lava_fullreplace.zip");
                datapackDest.getParentFile().mkdirs();
                Files.copy(datapackZip.toPath(), datapackDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Set world border after startup
        ServerLevel level = server.overworld();
        WorldBorder border = level.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(7000);
    }

    private void deleteRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}
