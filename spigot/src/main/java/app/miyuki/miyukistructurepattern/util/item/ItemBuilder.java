package app.miyuki.miyukistructurepattern.util.item;

import app.miyuki.miyukistructurepattern.util.chat.ColorHelper;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.google.common.collect.Lists;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ItemBuilder {

    private static final MethodHandle CUSTOM_MODEL_DATA_METHOD;

    static {
        MethodHandle customModelDataHandler;
        try {
            customModelDataHandler = MethodHandles.lookup().findVirtual(
                    ItemMeta.class,
                    "setCustomModelData",
                    MethodType.methodType(void.class, Integer.class)
            );
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            // no custom model data method found
            customModelDataHandler = null;
        }
        CUSTOM_MODEL_DATA_METHOD = customModelDataHandler;
    }

    private ItemStack itemStack;
    private UnaryOperator<String> placeholders;

    public static ItemBuilder of(XMaterial material) {
        if (!material.isSupported())
            throw new IllegalArgumentException("Material " + material.name() + " is not supported.");
        return new ItemBuilder(Objects.requireNonNull(material.parseItem()));
    }

    public static ItemBuilder of(@NotNull Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    public static ItemBuilder of(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    private ItemBuilder(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemBuilder durability(short durability) {
        itemStack.setDurability(durability);
        return this;
    }

    public ItemBuilder name(@NotNull Component component) {
        val itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return this;


        itemMeta.setDisplayName(ColorHelper.legacyColorize(component.decoration(TextDecoration.ITALIC, false)));
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder lore(@NotNull List<Component> lore) {
        val itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return this;

        itemMeta.setLore(lore.stream().map(ColorHelper::legacyColorize).collect(Collectors.toList()));
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder modelData(int modelData) {
        if (CUSTOM_MODEL_DATA_METHOD == null)
            return this;
        val itemMeta = itemStack.getItemMeta();
        try {
            CUSTOM_MODEL_DATA_METHOD.invoke(itemMeta, modelData);
        } catch (Throwable exception) {
            throw new RuntimeException("Failed to set custom model data.", exception);
        }
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder removeLoreLine(int index) {
        val itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return this;

        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : Lists.newArrayList();
        lore.remove(index);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder addLoreLine(@NotNull Component line) {
        val itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return this;

        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : Lists.newArrayList();

        lore.add(ColorHelper.legacyColorize(line));
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder addLoreLine(int index, @NotNull Component line) {
        val itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return this;

        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : Lists.newArrayList();
        lore.set(index, ColorHelper.legacyColorize(line));
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder enchant(@NotNull Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        val itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(flags);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder skull(@NotNull String value) {
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();

        if (value.isEmpty()) {
            XSkull.of(meta).profile(Profileable.detect(value)).removeProfile();
        } else {
            XSkull.of(meta).profile(Profileable.detect(value)).lenient().apply();
        }

        itemStack.setItemMeta(meta);
        return this;
    }

    @NotNull
    public ItemStack build() {
        if (placeholders != null) {
            val itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                if (itemMeta.hasDisplayName()) {
                    itemMeta.setDisplayName(placeholders.apply(itemMeta.getDisplayName()));
                }
                if (itemMeta.hasLore()) {
                    itemMeta.setLore(
                            itemMeta.getLore().stream()
                                    .map(placeholders)
                                    .collect(Collectors.toList())
                    );
                }
                itemStack.setItemMeta(itemMeta);
            }
        }

        return itemStack;
    }

}