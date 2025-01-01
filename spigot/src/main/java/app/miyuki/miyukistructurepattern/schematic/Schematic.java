package app.miyuki.miyukistructurepattern.schematic;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Data
public class Schematic {

    private final List<OffsetItem> offsetItems;

    private final SchematicVector center;

    @Data
    public static class OffsetItem {

        private final SchematicVector offset;

        private final ItemStack item;

    }
}