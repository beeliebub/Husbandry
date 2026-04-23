package me.beeliebub.husbandry.crop;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

/**
 * Handles seed/crop planting: stores crop traits from the planted item
 * into the chunk PDC at the block location.
 * <p>
 * Only {@code crop_traits} are stored. The {@code quality_crop} marker on items
 * is a crafting-only concept and does NOT affect planting or create quality stems.
 * Quality stems come solely from planting seeds that have the QUALITY trait in
 * their {@code crop_traits}.
 */
public class CropPlantListener implements Listener {

    private final CropData cropData;

    public CropPlantListener(CropData cropData) {
        this.cropData = cropData;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (!CropData.isPlantableItem(item.getType())) return;

        Set<CropTrait> traits = cropData.getTraits(item);
        if (traits.isEmpty()) return;

        cropData.setBlockTraits(event.getBlockPlaced(), traits);
    }
}