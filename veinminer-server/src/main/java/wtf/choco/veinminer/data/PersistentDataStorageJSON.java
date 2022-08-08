package wtf.choco.veinminer.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.jetbrains.annotations.NotNull;

import wtf.choco.veinminer.ActivationStrategy;
import wtf.choco.veinminer.VeinMinerPlayer;
import wtf.choco.veinminer.VeinMinerServer;
import wtf.choco.veinminer.tool.VeinMinerToolCategory;
import wtf.choco.veinminer.util.EnumUtil;

/**
 * An implementation of {@link PersistentDataStorage} for JSON files in a directory.
 */
public final class PersistentDataStorageJSON implements PersistentDataStorage {

    private final VeinMinerServer veinMiner;
    private final File directory;
    private final Gson gson;

    /**
     * Construct a new {@link PersistentDataStorageJSON}.
     *
     * @param veinMiner the vein miner server instance
     * @param directory the directory where all JSON files are held
     */
    public PersistentDataStorageJSON(@NotNull VeinMinerServer veinMiner, @NotNull File directory) {
        this.veinMiner = veinMiner;
        this.directory = directory;
        this.gson = new Gson();
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.JSON;
    }

    @NotNull
    @Override
    public CompletableFuture<Void> init() {
        this.directory.mkdirs();
        return CompletableFuture.completedFuture(null);
    }

    @NotNull
    @Override
    public CompletableFuture<VeinMinerPlayer> save(@NotNull VeinMinerPlayer player) {
        return CompletableFuture.supplyAsync(() -> savePlayer(player));
    }

    @NotNull
    @Override
    public CompletableFuture<List<VeinMinerPlayer>> save(@NotNull Collection<? extends VeinMinerPlayer> players) {
        if (players.isEmpty() || players.stream().allMatch(player -> !player.isDirty())) {
            return CompletableFuture.completedFuture(new ArrayList<>(players));
        }

        return CompletableFuture.supplyAsync(() -> {
            List<VeinMinerPlayer> result = new ArrayList<>(players.size());
            players.forEach(player -> result.add(savePlayer(player)));
            return result;
        });
    }

    @NotNull
    @Override
    public CompletableFuture<VeinMinerPlayer> load(@NotNull VeinMinerPlayer player) {
        return CompletableFuture.supplyAsync(() -> loadPlayer(player));
    }

    @NotNull
    @Override
    public CompletableFuture<List<VeinMinerPlayer>> load(@NotNull Collection<? extends VeinMinerPlayer> players) {
        if (players.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        return CompletableFuture.supplyAsync(() -> {
            List<VeinMinerPlayer> result = new ArrayList<>(players.size());
            players.forEach(player -> result.add(loadPlayer(player)));
            return result;
        });
    }

    private VeinMinerPlayer savePlayer(VeinMinerPlayer player) {
        try {
            File playerFile = new File(directory, player.getPlayerUUID().toString() + ".json");
            playerFile.createNewFile();

            JsonObject root = new JsonObject();
            root.addProperty("activation_strategy_id", player.getActivationStrategy().name());
            root.addProperty("vein_mining_pattern_id", player.getVeinMiningPattern().getKey().toString());

            JsonArray disabledCategoriesArray = new JsonArray();
            player.getDisabledCategories().forEach(category -> disabledCategoriesArray.add(category.getId()));
            root.add("disabled_categories", disabledCategoriesArray);

            Files.write(playerFile.toPath(), gson.toJson(root).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new CompletionException(e);
        }

        return player;
    }

    private VeinMinerPlayer loadPlayer(VeinMinerPlayer player) {
        File playerFile = new File(directory, player.getPlayerUUID().toString() + ".json");

        if (!playerFile.exists()) {
            return player;
        }

        try (BufferedReader reader = Files.newBufferedReader(playerFile.toPath(), StandardCharsets.UTF_8)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);

            if (root.has("activation_strategy_id")) {
                player.setActivationStrategy(EnumUtil.get(ActivationStrategy.class, root.get("activation_strategy_id").getAsString().toUpperCase()).orElse(veinMiner.getDefaultActivationStrategy()));
            }

            if (root.has("disabled_categories")) {
                player.setVeinMinerEnabled(true); // Ensure that all categories are loaded again

                root.getAsJsonArray("disabled_categories").forEach(element -> {
                    if (!element.isJsonPrimitive()) {
                        return;
                    }

                    VeinMinerToolCategory category = veinMiner.getToolCategoryRegistry().get(element.getAsString().toUpperCase());
                    if (category == null) {
                        return;
                    }

                    player.setVeinMinerEnabled(category, false);
                });
            }

            if (root.has("vein_mining_pattern_id")) {
                player.setVeinMiningPattern(veinMiner.getPatternRegistry().getOrDefault(root.get("vein_mining_pattern_id").getAsString(), veinMiner.getDefaultVeinMiningPattern()), false);
            }
        } catch (IOException | JsonSyntaxException e) {
            throw new CompletionException(e);
        }

        return player;
    }

}
