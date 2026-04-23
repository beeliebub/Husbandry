package me.beeliebub.husbandry.animal;

import me.beeliebub.husbandry.trait.Trait;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Animals;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Reads and writes Husbandry data (traits + gender) on an animal's
 * {@link PersistentDataContainer}.
 *
 * <h3>PDC layout</h3>
 * <ul>
 *   <li>{@code husbandry:tag} (byte 1) - marker that this animal has been initialized.</li>
 *   <li>{@code husbandry:gender} (string) - "MALE" or "FEMALE".</li>
 *   <li>{@code husbandry:traits} (string) - comma-separated trait enum names.</li>
 * </ul>
 */
public final class AnimalData {

    private final NamespacedKey tagKey;
    private final NamespacedKey genderKey;
    private final NamespacedKey traitsKey;

    public AnimalData(Plugin plugin) {
        this.tagKey = new NamespacedKey(plugin, "tag");
        this.genderKey = new NamespacedKey(plugin, "gender");
        this.traitsKey = new NamespacedKey(plugin, "traits");
    }

    /** Returns true if the animal already has the Husbandry tag. */
    public boolean isInitialized(Animals animal) {
        return animal.getPersistentDataContainer().has(tagKey, PersistentDataType.BYTE);
    }

    /** Writes the Husbandry tag, gender, and traits to the animal's PDC. */
    public void initialize(Animals animal, Gender gender, Set<Trait> traits) {
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        pdc.set(tagKey, PersistentDataType.BYTE, (byte) 1);
        pdc.set(genderKey, PersistentDataType.STRING, gender.name());
        pdc.set(traitsKey, PersistentDataType.STRING, encodeTraits(traits));
    }

    /** Reads the gender from PDC. Returns null if not present. */
    public @Nullable Gender getGender(Animals animal) {
        String value = animal.getPersistentDataContainer().get(genderKey, PersistentDataType.STRING);
        if (value == null) return null;
        try {
            return Gender.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Overwrites the gender on an animal's PDC. */
    public void setGender(Animals animal, Gender gender) {
        animal.getPersistentDataContainer().set(genderKey, PersistentDataType.STRING, gender.name());
    }

    /** Reads the trait set from PDC. Returns an empty set if not present. */
    public Set<Trait> getTraits(Animals animal) {
        String value = animal.getPersistentDataContainer().get(traitsKey, PersistentDataType.STRING);
        return decodeTraits(value);
    }

    /** Overwrites the trait set on an animal's PDC. */
    public void setTraits(Animals animal, Set<Trait> traits) {
        animal.getPersistentDataContainer().set(traitsKey, PersistentDataType.STRING, encodeTraits(traits));
    }

    // ── Serialization helpers ───────────────────────────────────────

    private static String encodeTraits(Set<Trait> traits) {
        if (traits.isEmpty()) return "";
        List<String> names = new ArrayList<>(traits.size());
        for (Trait t : traits) {
            names.add(t.name());
        }
        return String.join(",", names);
    }

    private static Set<Trait> decodeTraits(@Nullable String raw) {
        Set<Trait> result = EnumSet.noneOf(Trait.class);
        if (raw == null || raw.isBlank()) return result;
        for (String name : raw.split(",")) {
            Trait t = Trait.fromName(name.trim());
            if (t != null) {
                result.add(t);
            }
        }
        return result;
    }
}