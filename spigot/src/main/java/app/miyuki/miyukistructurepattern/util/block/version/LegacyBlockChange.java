package app.miyuki.miyukistructurepattern.util.block.version;

import app.miyuki.miyukistructurepattern.structure.workload.StructureBlock;
import app.miyuki.miyukistructurepattern.util.block.BlockChunkUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;

public class LegacyBlockChange implements BlockChange {

    private static MethodHandle setDataHandle;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Method setDataMethod = Block.class.getMethod("setData", byte.class);
            setDataHandle = lookup.unreflect(setDataMethod);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            setDataHandle = null;
        }
    }

    @Override
    public void sendFakeBlockChange(@NotNull Player player, @NotNull List<StructureBlock> blocks) {
        BlockChunkUtil.groupStructureBlockByChunk(blocks).forEach((chunk, chunkBlocks) -> {
            WrapperPlayServerMultiBlockChange.EncodedBlock[] encodedBlocks = chunkBlocks.stream()
                    .map(this::createEncodedBlock)
                    .toArray(WrapperPlayServerMultiBlockChange.EncodedBlock[]::new);

            sendMultiBlockChangePacket(player, chunk, encodedBlocks);
        });
    }

    @Override
    @SneakyThrows
    public void setType(@NotNull Block block, @NotNull ItemStack item) {
        block.setType(item.getType(), false);
        setDataHandle.invoke(block, (byte) item.getDurability());
    }

    @Override
    public void clearFakeBlockChange(@NotNull Player player, @NotNull List<Location> locations) {
        BlockChunkUtil.groupLocationByChunk(locations).forEach((chunk, chunkLocations) -> {
            WrapperPlayServerMultiBlockChange.EncodedBlock[] encodedBlocks = chunkLocations.stream()
                    .map(this::createEncodedBlockFromLocation)
                    .toArray(WrapperPlayServerMultiBlockChange.EncodedBlock[]::new);

            sendMultiBlockChangePacket(player, chunk, encodedBlocks);
        });
    }

    private WrapperPlayServerMultiBlockChange.EncodedBlock createEncodedBlock(@NotNull StructureBlock structureBlock) {
        val materialData = structureBlock.getMaterial().getData();
        return new WrapperPlayServerMultiBlockChange.EncodedBlock(
                SpigotConversionUtil.fromBukkitMaterialData(materialData),
                structureBlock.getLocation().getBlockX(),
                structureBlock.getLocation().getBlockY(),
                structureBlock.getLocation().getBlockZ()
        );
    }

    private WrapperPlayServerMultiBlockChange.EncodedBlock createEncodedBlockFromLocation(@NotNull Location location) {
        val block = location.getBlock();
        return new WrapperPlayServerMultiBlockChange.EncodedBlock(
                SpigotConversionUtil.fromBukkitMaterialData(new MaterialData(block.getType(), block.getData())),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    private void sendMultiBlockChangePacket(Player player, Vector3i chunk, WrapperPlayServerMultiBlockChange.EncodedBlock[] encodedBlocks) {
        WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange(
                chunk,
                true,
                encodedBlocks
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

}
