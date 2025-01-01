package app.miyuki.miyukistructurepattern.util.item;

import app.miyuki.miyukistructurepattern.util.chat.ColorHelper;
import app.miyuki.miyukistructurepattern.util.item.material.MaterialUtils;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.google.common.base.Enums;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.map.MapView;
import org.bukkit.material.MaterialData;
import org.bukkit.material.SpawnEgg;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.cryptomorin.xseries.XItemStack.parseColor;
import static com.cryptomorin.xseries.reflection.XReflection.supports;

@UtilityClass
public class NodeToItemAdapter {

    @Nullable
    public ItemStack adapt(@NotNull ConfigurationNode node) {
        return adapt(node, UnaryOperator.identity());
    }

    @NotNull
    public ItemStack adaptOrElse(@NotNull ConfigurationNode node, @NotNull ItemStack defaultItem) {
        val item = adapt(node);
        return item == null ? defaultItem : item;
    }

    @SuppressWarnings("removal")
    @SneakyThrows(SerializationException.class)
    @Nullable
    public ItemStack adapt(@NotNull ConfigurationNode node, @NotNull UnaryOperator<String> formatter) {

        val materialName = formatter.apply(node.node("material").getString("AIR"));
        var item = MaterialUtils.findMaterialByNameAndCreateItem(materialName);

        if (item == null) {

            item = ItemBuilder.of(XMaterial.PLAYER_HEAD.parseItem())
                    .skull(materialName)
                    .build();
        }

        if (item.getType() == Material.AIR)
            return null;

        int amount = node.node("amount").getInt();
        if (amount > 1)
            item.setAmount(amount);

        ItemMeta meta;
        ItemMeta tempMeta = item.getItemMeta();
        if (tempMeta == null) {
            // When AIR is null. Useful for when you just want to use the meta to save data and
            // set the type later. A simple CraftMetaItem.
            meta = Bukkit.getItemFactory().getItemMeta(XMaterial.STONE.parseMaterial());
        } else {
            meta = tempMeta;
        }


        // Durability - Damage
        if (supports(13)) {
            if (meta instanceof Damageable) {
                int damage = node.node("damage").getInt();
                if (damage > 0) ((Damageable) meta).setDamage(damage);
            }
        } else {
            int damage = node.node("damage").getInt();
            if (damage > 0) item.setDurability((short) damage);
        }

        // Special Items
        if (meta instanceof BannerMeta) {
            BannerMeta banner = (BannerMeta) meta;
            val patterns = node.node("patterns");
            if (!patterns.virtual()) {
                for (Map.Entry<Object, ? extends ConfigurationNode> patternEntry : patterns.childrenMap().entrySet()) {
                    PatternType type = PatternType.getByIdentifier(patternEntry.getKey().toString());
                    if (type == null) {
                        type = Enums.getIfPresent(PatternType.class, patternEntry.getKey().toString().toUpperCase(Locale.ENGLISH)).or(PatternType.BASE);
                    }
                    DyeColor color = Enums.getIfPresent(DyeColor.class, patternEntry.getValue().getString("")).or(DyeColor.WHITE);
                    banner.addPattern(new Pattern(color, type));
                }
            }

        } else if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leather = (LeatherArmorMeta) meta;
            val colorStr = node.node("color");
            if (!colorStr.virtual()) {
                leather.setColor(parseColor(colorStr.getString("")));
            }
        } else if (meta instanceof PotionMeta) {
            if (supports(9)) {
                PotionMeta potion = (PotionMeta) meta;

                for (String effects : node.node("effects").getList(String.class, new ArrayList<>())) {
                    XPotion.Effect effect = XPotion.parseEffect(effects);
                    if (effect.hasChance())
                        potion.addCustomEffect(effect.getEffect(), true);
                }

                val baseEffectNode = node.node("base-effect");
                if (!baseEffectNode.virtual()) {
                    val split = split(baseEffectNode.getString(""), ',');
                    PotionType type = Enums.getIfPresent(PotionType.class, split.get(0).trim().toUpperCase(Locale.ENGLISH)).or(PotionType.UNCRAFTABLE);
                    boolean extended = split.size() != 1 && Boolean.parseBoolean(split.get(1).trim());
                    boolean upgraded = split.size() > 2 && Boolean.parseBoolean(split.get(2).trim());
                    PotionData potionData = new PotionData(type, extended, upgraded);
                    potion.setBasePotionData(potionData);
                }

                val color = node.node("color");
                if (!color.virtual()) {
                    potion.setColor(parseColor(color.getString("")));
                }
            } else {

                val levelNode = node.node("level");
                if (!levelNode.virtual()) {
                    val level = levelNode.getInt();
                    val baseEffect = node.node("base-effect");
                    if (!baseEffect.virtual()) {
                        List<String> split = split(baseEffect.getString(""), ',');
                        PotionType type = Enums.getIfPresent(PotionType.class, split.get(0).trim().toUpperCase(Locale.ENGLISH)).or(PotionType.SLOWNESS);
                        boolean extended = split.size() != 1 && Boolean.parseBoolean(split.get(1).trim());
                        boolean splash = split.size() > 2 && Boolean.parseBoolean(split.get(2).trim());

                        item = (new Potion(type, level, splash, extended)).toItemStack(1);
                    }
                }
            }
        } else if (meta instanceof BlockStateMeta) {
            BlockStateMeta bsm = (BlockStateMeta) meta;
            BlockState state = safeBlockState(bsm);

            if (state instanceof CreatureSpawner) {
                // Do we still need this? XMaterial handles it, doesn't it?
                CreatureSpawner spawner = (CreatureSpawner) state;
                String spawnerStr = node.node("spawner").getString("");
                if (!Strings.isNullOrEmpty(spawnerStr)) {
                    spawner.setSpawnedType(Enums.getIfPresent(EntityType.class, spawnerStr.toUpperCase(Locale.ENGLISH)).orNull());
                    spawner.update(true);
                    bsm.setBlockState(spawner);
                }
            } else if (supports(11) && state instanceof ShulkerBox) {
                val shulkerNode = node.node("contents");
                if (!shulkerNode.virtual()) {
                    val box = (ShulkerBox) state;
                    for (Map.Entry<Object, ? extends ConfigurationNode> entry : shulkerNode.childrenMap().entrySet()) {
                        val boxItem = adapt(entry.getValue());
                        val slot = Integer.parseInt(entry.getKey().toString());
                        box.getInventory().setItem(slot, boxItem);
                    }
                    box.update(true);
                    bsm.setBlockState(box);
                }
            } else if (state instanceof Banner) {
                Banner banner = (Banner) state;
                val patterns = node.node("patterns");

                if (!supports(14)) {
                    banner.setBaseColor(DyeColor.WHITE);
                }

                if (!patterns.virtual()) {
                    for (Map.Entry<Object, ? extends ConfigurationNode> patternEntry : patterns.childrenMap().entrySet()) {
                        PatternType type = PatternType.getByIdentifier(patternEntry.getKey().toString());
                        if (type == null) {
                            type = Enums.getIfPresent(PatternType.class, patternEntry.getKey().toString().toUpperCase(Locale.ENGLISH)).or(PatternType.BASE);
                        }
                        DyeColor color = Enums.getIfPresent(DyeColor.class, patternEntry.getValue().getString("")).or(DyeColor.WHITE);
                        banner.addPattern(new Pattern(color, type));
                    }
                    banner.update(true);
                    bsm.setBlockState(banner);
                }
            }
        } else if (meta instanceof FireworkMeta) {
            FireworkMeta firework = (FireworkMeta) meta;
            firework.setPower(node.node("power").getInt());
            val fireworkNode = node.node("firework");

            if (!fireworkNode.virtual()) {
                val builder = FireworkEffect.builder();
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : fireworkNode.childrenMap().entrySet()) {
                    val fw = entry.getValue();
                    builder.flicker(fw.node("flicker").getBoolean());
                    builder.trail(fw.node("trail").getBoolean());
                    builder.with(Enums.getIfPresent(FireworkEffect.Type.class, fw.node("type").getString("").toUpperCase(Locale.ENGLISH)).or(FireworkEffect.Type.STAR));

                    val colorsSection = fw.node("colors");
                    if (!colorsSection.virtual()) {
                        val fwColors = colorsSection.node("base").getList(String.class, new ArrayList<>());
                        val colors = new ArrayList<Color>(fwColors.size());
                        for (String colorStr : fwColors) colors.add(parseColor(colorStr));
                        builder.withColor(colors);

                        val fadeColors = colorsSection.node("fade").getList(String.class, new ArrayList<>());
                        colors.clear();
                        for (String colorStr : fadeColors) colors.add(parseColor(colorStr));
                        builder.withFade(colors);
                    }

                    firework.addEffect(builder.build());
                }
            }


        } else if (meta instanceof BookMeta) {
            BookMeta book = (BookMeta) meta;
            val boolNode = node.node("book");
            if (!boolNode.virtual()) {
                book.setTitle(boolNode.node("title").getString(""));
                book.setAuthor(boolNode.node("author").getString(""));
                book.setPages(boolNode.node("pages").getList(String.class, new ArrayList<>()));

                if (supports(9)) {
                    val generationValue = boolNode.node("generation").getString("");
                    if (!generationValue.isEmpty()) {
                        BookMeta.Generation generation = Enums.getIfPresent(BookMeta.Generation.class, generationValue).orNull();
                        book.setGeneration(generation);
                    }
                }
            }
        } else if (meta instanceof MapMeta) {
            MapMeta map = (MapMeta) meta;
            val mapNode = node.node("map");

            if (!mapNode.virtual()) {
                map.setScaling(mapNode.node("scaling").getBoolean());
                if (supports(11)) {
                    map.setLocationName(mapNode.node("location").getString(""));
                    val color = mapNode.node("color");
                    if (!color.virtual()) {
                        map.setColor(parseColor(color.getString("")));
                    }
                }

                if (supports(14)) {
                    val view = mapNode.node("view");
                    if (!view.virtual()) {
                        val world = Bukkit.getWorld(view.node("world").getString(""));
                        if (world != null) {
                            val mapView = Bukkit.createMap(world);
                            mapView.setWorld(world);
                            mapView.setScale(Enums.getIfPresent(MapView.Scale.class, view.node("scale").getString("")).or(MapView.Scale.NORMAL));
                            mapView.setLocked(view.node("locked").getBoolean());
                            mapView.setTrackingPosition(view.node("tracking-position").getBoolean());
                            mapView.setUnlimitedTracking(view.node("unlimited-tracking").getBoolean());

                            val center = view.node("center");
                            if (!center.virtual()) {
                                mapView.setCenterX(center.node("x").getInt());
                                mapView.setCenterZ(center.node("z").getInt());
                            }

                            map.setMapView(mapView);
                        }
                    }
                }
            }
        } else {
            if (supports(20) && (meta instanceof ArmorMeta)) {
                ArmorMeta armorMeta = (ArmorMeta) meta;
                val trim = node.node("trim");
                if (!trim.virtual()) {
                    val trimMaterial = Registry.TRIM_MATERIAL.get(NamespacedKey.fromString(trim.node("material").getString("")));
                    val trimPattern = Registry.TRIM_PATTERN.get(NamespacedKey.fromString(trim.node("pattern").getString("")));
                    armorMeta.setTrim(new ArmorTrim(trimMaterial, trimPattern));
                }

            }

            if (supports(17) && (meta instanceof AxolotlBucketMeta)) {
                AxolotlBucketMeta bucket = (AxolotlBucketMeta) meta;
                val variantStr = node.node("color");
                if (!variantStr.virtual()) {
                    Axolotl.Variant variant = Enums.getIfPresent(Axolotl.Variant.class, variantStr.getString("")).or(Axolotl.Variant.BLUE);
                    bucket.setVariant(variant);
                }

            }

            if (supports(16) && (meta instanceof CompassMeta)) {
                CompassMeta compass = (CompassMeta) meta;

                compass.setLodestoneTracked(node.node("tracked").getBoolean());

                val lodestone = node.node("lodestone");
                if (!lodestone.virtual()) {
                    val world = Bukkit.getWorld(lodestone.node("world").getString(""));
                    val x = lodestone.node("x").getDouble();
                    val y = lodestone.node("y").getDouble();
                    val z = lodestone.node("z").getDouble();
                    compass.setLodestone(new Location(world, x, y, z));
                }
            }

            if (supports(15) && (meta instanceof SuspiciousStewMeta)) {
                SuspiciousStewMeta stew = (SuspiciousStewMeta) meta;
                val effects = node.node("effects").getList(String.class, new ArrayList<>());
                for (String effect : effects) {
                    XPotion.Effect potionEffect = XPotion.parseEffect(effect);
                    if (potionEffect.hasChance()) {
                        stew.addCustomEffect(potionEffect.getEffect(), true);
                    }
                }
            }

            if (supports(14)) {
                if (meta instanceof CrossbowMeta) {
                    CrossbowMeta crossbow = (CrossbowMeta) meta;
                    val projectiles = node.node("projectiles");
                    if (!projectiles.virtual()) {
                        for (Map.Entry<Object, ? extends ConfigurationNode> entry : projectiles.childrenMap().entrySet()) {
                            val projectileItem = adapt(entry.getValue());
                            crossbow.addChargedProjectile(projectileItem);
                        }
                    }
                } else if (meta instanceof TropicalFishBucketMeta) {
                    TropicalFishBucketMeta tropical = (TropicalFishBucketMeta) meta;
                    val color = Enums.getIfPresent(DyeColor.class, node.node("color").getString("")).or(DyeColor.WHITE);
                    val patternColor = Enums.getIfPresent(DyeColor.class, node.node("pattern-color").getString("")).or(DyeColor.WHITE);

                    val pattern = Enums.getIfPresent(TropicalFish.Pattern.class, node.node("pattern").getString("")).or(TropicalFish.Pattern.BETTY);

                    tropical.setBodyColor(color);
                    tropical.setPatternColor(patternColor);
                    tropical.setPattern(pattern);
                }
            }

            // Apparently Suspicious Stew was never added in 1.14
            if (!supports(13)) {
                // Spawn Eggs
                if (supports(11)) {
                    if (meta instanceof SpawnEggMeta) {
                        val creatureName = node.node("creature");
                        if (!creatureName.virtual()) {
                            val spawnEgg = (SpawnEggMeta) meta;
                            val creature = Enums.getIfPresent(EntityType.class, creatureName.getString("").toUpperCase(Locale.ENGLISH));

                            if (creature.isPresent())
                                spawnEgg.setSpawnedType(creature.get());
                        }
                    }
                } else {
                    MaterialData data = item.getData();
                    if (data instanceof SpawnEgg) {
                        val creatureName = node.node("creature");
                        if (!creatureName.virtual()) {
                            val spawnEgg = (SpawnEgg) data;
                            val creature = Enums.getIfPresent(EntityType.class, creatureName.getString("").toUpperCase(Locale.ENGLISH));

                            if (creature.isPresent())
                                spawnEgg.setSpawnedType(creature.get());
                            item.setData(data);
                        }
                    }
                }
            }
        }

        // Display Name
        val name = node.node("name");
        if (!name.virtual()) {
            val translated = formatter.apply(name.getString(""));
            meta.setDisplayName(ColorHelper.legacyColorize(translated));
        }

        // Unbreakable
        if (supports(11)) {
            val unbreakable = node.node("unbreakable");
            if (!unbreakable.virtual()) {
                meta.setUnbreakable(unbreakable.getBoolean());
            }
        }

        // Custom Model Data
        if (supports(14)) {
            val modelData = node.node("model-data");
            if (!modelData.virtual()) {
                meta.setCustomModelData(modelData.getInt());
            }
        }

        // Lore
        val loreNode = node.node("lore");
        if (!loreNode.virtual()) {

            val translatedLore = loreNode.getList(String.class, new ArrayList<>()).stream()
                    .map(formatter)
                    .map(ColorHelper::legacyColorize)
                    .collect(Collectors.toList());

            meta.setLore(translatedLore);
        }

        val enchantmentNode = node.node("enchantments");
        if (!enchantmentNode.virtual()) {
            val enchantments = enchantmentNode.childrenMap();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : enchantments.entrySet()) {
                val enchant = XEnchantment.matchXEnchantment(entry.getKey().toString());
                enchant.ifPresent(xEnchantment -> meta.addEnchant(xEnchantment.getEnchant(), entry.getValue().getInt(), true));
            }
        } else if (node.node("glow").getBoolean()) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        val storedEnchantNode = node.node("stored-enchants");
        if (!storedEnchantNode.virtual()) {
            val storedEnchants = storedEnchantNode.childrenMap();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : storedEnchants.entrySet()) {
                val enchant = XEnchantment.matchXEnchantment(entry.getKey().toString());
                enchant.ifPresent(xEnchantment -> ((EnchantmentStorageMeta) meta).addStoredEnchant(xEnchantment.getEnchant(), entry.getValue().getInt(), true));
            }
        }

        val flagsNode = node.node("flags");
        if (!flagsNode.virtual()) {
            val flags = flagsNode.getList(String.class, new ArrayList<>())
                    .stream()
                    .map(String::toUpperCase)
                    .map(Enums.stringConverter(ItemFlag.class));

            meta.addItemFlags(flags.toArray(ItemFlag[]::new));
        }

        if (supports(13)) {
            val attributesNode = node.node("attributes");
            if (!attributesNode.virtual()) {
                val attributes = attributesNode.childrenMap();
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : attributes.entrySet()) {
                    val attribute = Enums.getIfPresent(Attribute.class, entry.getKey().toString().toUpperCase(Locale.ENGLISH)).orNull();
                    if (attribute == null)
                        continue;

                    val attributeNode = entry.getValue();

                    val attribId = attributeNode.node("id").getString();
                    val id = attribId != null ? UUID.fromString(attribId) : UUID.randomUUID();

                    val slot = attributeNode.node("slot").getString("");
                    val equipmentSlot = slot != null ? Enums.getIfPresent(EquipmentSlot.class, slot).or(EquipmentSlot.HAND) : null;

                    val modifier = new AttributeModifier(
                            id,
                            attributeNode.node("name").getString(""),
                            attributeNode.node("amount").getDouble(),
                            Enums.getIfPresent(AttributeModifier.Operation.class, attributeNode.node("operation").getString("")).or(AttributeModifier.Operation.ADD_NUMBER),
                            equipmentSlot);

                    meta.addAttributeModifier(attribute, modifier);
                }
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    private static BlockState safeBlockState(BlockStateMeta meta) {
        try {
            return meta.getBlockState();
        } catch (IllegalStateException ex) {
            if (ex.getMessage().toLowerCase(Locale.ENGLISH).contains("missing blockstate")) {
                return null;
            } else {
                throw ex;
            }
        }
    }

    private static List<String> split(@NotNull String str, @SuppressWarnings("SameParameterValue") char separatorChar) {
        List<String> list = new ArrayList<>(5);
        boolean match = false, lastMatch = false;
        int len = str.length();
        int start = 0;

        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }

                // This is important, it should not be i++
                start = i + 1;
                continue;
            }

            lastMatch = false;
            match = true;
        }

        if (match || lastMatch) {
            list.add(str.substring(start, len));
        }
        return list;
    }

}
