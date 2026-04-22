package me.beeliebub.husbandry.trait;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Every trait an animal can possess. Each enum constant carries its own rarity,
 * inheritance behaviour, display name, entity type restrictions, attribute modifiers,
 * and potion effects.
 *
 * <p>Traits with an empty {@code applicableTypes} set apply to all animals.
 * Traits with specific types only apply to (and are only rolled for) those entity types.</p>
 */
public enum Trait {

    // ── General traits (all animals) ────────────────────────────────
    SLUGGISH(TraitRarity.BASIC, InheritanceType.DOMINANT, "Sluggish"),
    HASTY(TraitRarity.BASIC, InheritanceType.RECESSIVE, "Hasty"),
    BEEFY(TraitRarity.BASIC, InheritanceType.RECESSIVE, "Beefy"),
    BRITTLE(TraitRarity.BASIC, InheritanceType.DOMINANT, "Brittle"),
    HEAVY(TraitRarity.BASIC, InheritanceType.RECESSIVE, "Heavy"),
    FERTILE(TraitRarity.BASIC, InheritanceType.RECESSIVE, "Fertile"),
    BARREN(TraitRarity.BASIC, InheritanceType.DOMINANT, "Barren"),
    HYDROPHOBIC(TraitRarity.BASIC, InheritanceType.DOMINANT, "Hydrophobic"),
    FIREPROOF(TraitRarity.RARE, InheritanceType.SPECIAL, "Fireproof"),
    WATERBREATHING(TraitRarity.RARE, InheritanceType.SPECIAL, "Waterbreathing"),
    SWIFT_SWIMMER(TraitRarity.RARE, InheritanceType.SPECIAL, "Swift Swimmer"),
    HYDROPHILIC(TraitRarity.RARE, InheritanceType.SPECIAL, "Hydrophilic"),
    BOUNTIFUL(TraitRarity.RARE, InheritanceType.SPECIAL, "Bountiful"),
    INVINCIBLE(TraitRarity.LEGENDARY, InheritanceType.SPECIAL, "Invincible"),

    // ── Wolf-specific ───────────────────────────────────────────────
    COWARDLY(TraitRarity.BASIC, InheritanceType.DOMINANT, "Cowardly", EntityType.WOLF),
    FIERCE(TraitRarity.BASIC, InheritanceType.RECESSIVE, "Fierce", EntityType.WOLF),

    // ── Bee-specific ────────────────────────────────────────────────
    HONEY_HINDRANCE(TraitRarity.BASIC, InheritanceType.DOMINANT, "Honey Hindrance", EntityType.BEE),
    HONEY_HELPER(TraitRarity.RARE, InheritanceType.SPECIAL, "Honey Helper", EntityType.BEE),
    POLLINATOR(TraitRarity.LEGENDARY, InheritanceType.SPECIAL, "Pollinator", EntityType.BEE),

    // ── Horse-specific (horses, donkeys, mules) ─────────────────────
    SHORT_JUMPER(TraitRarity.BASIC, InheritanceType.DOMINANT, "Short Jumper",
            EntityType.HORSE, EntityType.DONKEY, EntityType.MULE),
    HIGH_JUMPER(TraitRarity.BASIC, InheritanceType.RECESSIVE, "High Jumper",
            EntityType.HORSE, EntityType.DONKEY, EntityType.MULE),

    // ── Sheep-specific ──────────────────────────────────────────────
    WOOLY(TraitRarity.RARE, InheritanceType.SPECIAL, "Wooly", EntityType.SHEEP),

    // ── Chicken-specific ────────────────────────────────────────────
    EMPTY(TraitRarity.BASIC, InheritanceType.DOMINANT, "Empty", EntityType.CHICKEN),
    EGGY(TraitRarity.RARE, InheritanceType.SPECIAL, "Eggy", EntityType.CHICKEN);

    // ── Static registries ───────────────────────────────────────────
    private static final Map<Trait, List<ModifierEntry>> MODIFIER_MAP = new EnumMap<>(Trait.class);
    private static final Map<Trait, List<PotionEntry>> POTION_MAP = new EnumMap<>(Trait.class);

    static {
        // Attribute modifiers (percentage-based use ADD_SCALAR)
        addModifier(SLUGGISH, Attribute.MOVEMENT_SPEED, -0.5, AttributeModifier.Operation.ADD_SCALAR);
        addModifier(HASTY, Attribute.MOVEMENT_SPEED, 0.5, AttributeModifier.Operation.ADD_SCALAR);
        addModifier(BEEFY, Attribute.MAX_HEALTH, 0.5, AttributeModifier.Operation.ADD_SCALAR);
        addModifier(BRITTLE, Attribute.MAX_HEALTH, -0.5, AttributeModifier.Operation.ADD_SCALAR);
        addModifier(HEAVY, Attribute.KNOCKBACK_RESISTANCE, 0.5, AttributeModifier.Operation.ADD_NUMBER);
        addModifier(COWARDLY, Attribute.ATTACK_DAMAGE, -0.5, AttributeModifier.Operation.ADD_SCALAR);
        addModifier(FIERCE, Attribute.ATTACK_DAMAGE, 0.5, AttributeModifier.Operation.ADD_SCALAR);
        addModifier(SHORT_JUMPER, Attribute.JUMP_STRENGTH, -0.5, AttributeModifier.Operation.ADD_SCALAR);
        addModifier(HIGH_JUMPER, Attribute.JUMP_STRENGTH, 0.5, AttributeModifier.Operation.ADD_SCALAR);

        // Potion effects (applied permanently with no particles)
        addPotion(FIREPROOF, PotionEffectType.FIRE_RESISTANCE, 0);
        addPotion(SWIFT_SWIMMER, PotionEffectType.DOLPHINS_GRACE, 0);
    }

    // ── Instance fields ─────────────────────────────────────────────
    private final TraitRarity rarity;
    private final InheritanceType inheritanceType;
    private final String displayName;
    private final Set<EntityType> applicableTypes;

    Trait(TraitRarity rarity, InheritanceType inheritanceType, String displayName, EntityType... types) {
        this.rarity = rarity;
        this.inheritanceType = inheritanceType;
        this.displayName = displayName;
        this.applicableTypes = types.length == 0 ? Set.of() : EnumSet.copyOf(Arrays.asList(types));
    }

    // ── Getters ─────────────────────────────────────────────────────

    public TraitRarity getRarity() {
        return rarity;
    }

    public InheritanceType getInheritanceType() {
        return inheritanceType;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Returns the entity types this trait is restricted to, or empty if it applies to all animals. */
    public Set<EntityType> getApplicableTypes() {
        return applicableTypes;
    }

    /** Returns true if this trait can be assigned to the given entity type. */
    public boolean isApplicableTo(EntityType type) {
        return applicableTypes.isEmpty() || applicableTypes.contains(type);
    }

    /** Returns an unmodifiable list of attribute modifiers this trait applies. */
    public List<ModifierEntry> getModifiers() {
        return Collections.unmodifiableList(MODIFIER_MAP.getOrDefault(this, List.of()));
    }

    /** Returns an unmodifiable list of potion effects this trait applies. */
    public List<PotionEntry> getPotionEffects() {
        return Collections.unmodifiableList(POTION_MAP.getOrDefault(this, List.of()));
    }

    // ── Static helpers ──────────────────────────────────────────────

    private static void addModifier(Trait trait, Attribute attribute, double amount,
                                    AttributeModifier.Operation operation) {
        MODIFIER_MAP.computeIfAbsent(trait, k -> new ArrayList<>())
                .add(new ModifierEntry(attribute, amount, operation));
    }

    private static void addPotion(Trait trait, PotionEffectType type, int amplifier) {
        POTION_MAP.computeIfAbsent(trait, k -> new ArrayList<>())
                .add(new PotionEntry(type, amplifier));
    }

    /** Returns all traits matching the given rarity that are applicable to the entity type. */
    public static List<Trait> byRarityFor(TraitRarity rarity, EntityType entityType) {
        List<Trait> result = new ArrayList<>();
        for (Trait t : values()) {
            if (t.rarity == rarity && t.isApplicableTo(entityType)) {
                result.add(t);
            }
        }
        return result;
    }

    /** Looks up a trait by name (case-insensitive). Returns null if not found. */
    public static @Nullable Trait fromName(String name) {
        for (Trait t : values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }

    // ── Inner records ───────────────────────────────────────────────

    public record ModifierEntry(Attribute attribute, double amount,
                                AttributeModifier.Operation operation) {
    }

    public record PotionEntry(PotionEffectType type, int amplifier) {
    }
}