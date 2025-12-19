package com.axalotl.async.neoforge.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashSet;
import java.util.List;

import static com.axalotl.async.common.config.AsyncConfig.*;

public class AsyncConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.ConfigValue<Boolean> disabled;
    private static final ModConfigSpec.ConfigValue<Integer> paraMax;
    private static final ModConfigSpec.ConfigValue<List<String>> synchronizedEntities;
    private static final ModConfigSpec.ConfigValue<Boolean> enableAsyncSpawn;
    private static final ModConfigSpec.ConfigValue<Boolean> enableAsyncRandomTicks;
    private static final ModConfigSpec.ConfigValue<Integer> chunkIOParaMax;
    private static final ModConfigSpec.ConfigValue<Integer> chunkGenParaMax;

    static {
        BUILDER.push("Async Config");

        disabled = BUILDER.comment("Enables parallel processing of entity.")
                .define("disabled", isDisabled());

        paraMax = BUILDER.comment("Maximum number of threads to use for parallel processing. Set to -1 to use default value.")
                .define("paraMax", getParaMax());

        synchronizedEntities = BUILDER.comment("List of entity class for sync processing.")
                .define("synchronizedEntities", new java.util.ArrayList<>(com.axalotl.async.common.config.AsyncConfig.synchronizedEntities.stream().map(ResourceLocation::toString).toList()));

        enableAsyncSpawn = BUILDER.comment("Enables parallel processing of entity spawns.")
                .define("enableAsyncSpawn", isEnableAsyncSpawn());

        enableAsyncRandomTicks = BUILDER.comment("Experimental! Enables async processing of random ticks.")
                .define("enableAsyncRandomTicks", isEnableAsyncRandomTicks());

        chunkIOParaMax = BUILDER.comment("Maximum number of threads to use for chunk IO. Set to -1 to use default value.")
                .define("chunkIOParaMax", getChunkIOParaMax());

        chunkGenParaMax = BUILDER.comment("Maximum number of threads to use for chunk generation. Set to -1 to use default value.")
                .define("chunkGenParaMax", getChunkGenParaMax());

        BUILDER.pop();
        SPEC = BUILDER.build();
        LOGGER.info("Configuration successfully loaded.");
    }

    public static void loadConfig() {
        setDisabled(disabled.get());
        setParaMax(paraMax.get());
        setChunkIOParaMax(chunkIOParaMax.get());
        setChunkGenParaMax(chunkGenParaMax.get());
        setEnableAsyncSpawn(enableAsyncSpawn.get());
        setEnableAsyncRandomTicks(enableAsyncRandomTicks.get());
        List<String> ids = synchronizedEntities.get();
        HashSet<ResourceLocation> set = new HashSet<>();

        for (String id : ids) {
            ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl != null) {
                set.add(rl);
            } else {
                LOGGER.warn("Invalid resource location in synchronizedEntities config: {}", id);
            }
        }

        com.axalotl.async.common.config.AsyncConfig.synchronizedEntities = set;
    }

    public static void saveConfig() {
        disabled.set(isDisabled());
        paraMax.set(getParaMax());
        chunkIOParaMax.set(getChunkIOParaMax());
        chunkGenParaMax.set(getChunkGenParaMax());
        enableAsyncSpawn.set(isEnableAsyncSpawn());
        enableAsyncRandomTicks.set(isEnableAsyncRandomTicks());
        synchronizedEntities.set(new java.util.ArrayList<>(com.axalotl.async.common.config.AsyncConfig.synchronizedEntities.stream().map(ResourceLocation::toString).toList()));
        SPEC.save();
        LOGGER.info("Configuration successfully saved.");
    }
}
