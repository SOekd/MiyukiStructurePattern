package app.miyuki.miyukistructurepattern.util.item.material;

import com.cryptomorin.xseries.XMaterial;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class MaterialUtils {

    private final Map<String, @Nullable ItemStack> itemCache = new HashMap<>();

    @Nullable
    public ItemStack findMaterialByNameAndCreateItem(@NotNull String name) {
        if (itemCache.containsKey(name)) {
            val cachedItem = itemCache.get(name);
            if (cachedItem == null)
                return null;
            return cachedItem.clone();
        }

        var item = XMaterial.matchXMaterial(name)
                .map(XMaterial::parseItem)
                .orElse(null);

        if (item == null) {
            try {
                item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(name)));
            } catch (Exception ignored) {
            }
        }


        itemCache.put(name, item);
        return item;
    }

}
