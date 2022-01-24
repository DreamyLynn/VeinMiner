package wtf.choco.veinminer.api;

import com.google.common.base.Predicates;

import java.util.function.Predicate;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.veinminer.VeinMinerPlugin;
import wtf.choco.veinminer.network.VeinMinerPlayer;
import wtf.choco.veinminer.util.VMConstants;

/**
 * Represents the different methods of activating VeinMiner.
 */
public enum ActivationStrategy {

    /**
     * Never activated. Disabled.
     */
    NONE(Predicates.alwaysFalse()),

    /**
     * Activated by the client with the client-sided mod to allow players to use their
     * own key binds.
     * <p>
     * This strategy should not be set on {@link VeinMinerPlayer VeinMinerPlayers} that
     * do not have the client mod installed.
     */
    CLIENT(player -> VeinMinerPlugin.getInstance().getPlayerManager().get(player).isVeinMinerActive()),

    /**
     * Activated when a Player is holding sneak.
     */
    SNEAK(Player::isSneaking),

    /**
     * Activated when a Player is standing up (i.e. not sneaking).
     */
    STAND(Predicates.not(Player::isSneaking)),

    /**
     * Always activated.
     */
    ALWAYS(Predicates.alwaysTrue());

    private final Predicate<@NotNull Player> condition;

    private ActivationStrategy(@NotNull Predicate<Player> condition) {
        this.condition = condition;
    }

    /**
     * Check whether a Player is capable of vein mining according to this activation.
     *
     * @param player the player to check
     *
     * @return true if valid to vein mine, false otherwise
     */
    public boolean isActive(@NotNull Player player) {
        return player != null && player.isValid() && this.condition.test(player);
    }

    /**
     * Get a MineActivation based on its name.
     *
     * @param name the name for which to search. Case insensitive
     *
     * @return the resulting activation. null if none found
     */
    @Nullable
    public static ActivationStrategy getByName(@NotNull String name) {
        for (ActivationStrategy activation : values()) {
            if (activation.name().equalsIgnoreCase(name)) {
                return activation;
            }
        }

        return null;
    }

    /**
     * Get the default {@link ActivationStrategy} as specific in the config.
     *
     * @return the default activation strategy
     */
    @NotNull
    public static ActivationStrategy getDefaultActivationStrategy() {
        VeinMinerPlugin plugin = VeinMinerPlugin.getInstance();

        String strategyName = plugin.getConfig().getString(VMConstants.CONFIG_DEFAULT_ACTIVATION_STRATEGY);
        if (strategyName == null) {
            return SNEAK;
        }

        ActivationStrategy strategy = getByName(strategyName);
        return strategy != null ? strategy : SNEAK;
    }

}
