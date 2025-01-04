package app.miyuki.miyukistructurepattern.util.block.version;

import app.miyuki.miyukistructurepattern.structure.workload.StructureBlock;
import app.miyuki.miyukistructurepattern.util.chunk.ChunkUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModernBlockChange implements BlockChange<BlockData> {

    @Override
    public void sendFakeBlockChange(@NotNull Player player, @NotNull List<StructureBlock<BlockData>> structureBlocks) {
        ChunkUtil.groupStructureBlockByChunk(structureBlocks).forEach((chunk, chunkBlocks) -> {
            WrapperPlayServerMultiBlockChange.EncodedBlock[] encodedBlocks = chunkBlocks.stream()
                    .map(this::createEncodedBlock)
                    .toArray(WrapperPlayServerMultiBlockChange.EncodedBlock[]::new);

            sendMultiBlockChangePacket(player, chunk, encodedBlocks, true);
        });
    }

    @Override
    public void setType(@NotNull StructureBlock<BlockData> item) {
        val location = item.getLocation();
        val block = location.getBlock();

        location.getBlock().setType(item.getItemData().getMaterial(), false);
        block.setBlockData(item.getItemData());

    }


    @Override
    public void clearFakeBlockChange(@NotNull Player player, @NotNull List<Location> locations) {
        ChunkUtil.groupLocationByChunk(locations).forEach((chunk, chunkLocations) -> {
            WrapperPlayServerMultiBlockChange.EncodedBlock[] blockChangeInfos = chunkLocations.stream()
                    .map(this::createEncodedBlockFromLocation)
                    .toArray(WrapperPlayServerMultiBlockChange.EncodedBlock[]::new);

            sendMultiBlockChangePacket(player, chunk, blockChangeInfos, false);
        });
    }

    @Override
    public @NotNull BlockData extractData(@NotNull ItemStack itemStack) {
        return itemStack.getType().createBlockData();
    }

    @Override
    public boolean isAir(@NotNull BlockData itemData) {
        return itemData.getMaterial() == Material.AIR;
    }

    private WrapperPlayServerMultiBlockChange.EncodedBlock createEncodedBlock(StructureBlock<BlockData> structureBlock) {
        val blockData = SpigotConversionUtil.fromBukkitBlockData(structureBlock.getItemData());
        return new WrapperPlayServerMultiBlockChange.EncodedBlock(
                blockData,
                structureBlock.getLocation().getBlockX(),
                structureBlock.getLocation().getBlockY(),
                structureBlock.getLocation().getBlockZ()
        );
    }

    private WrapperPlayServerMultiBlockChange.EncodedBlock createEncodedBlockFromLocation(Location location) {
        val blockData = SpigotConversionUtil.fromBukkitBlockData(location.getBlock().getBlockData());
        return new WrapperPlayServerMultiBlockChange.EncodedBlock(
                blockData,
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    private void sendMultiBlockChangePacket(Player player, Vector3i chunk, WrapperPlayServerMultiBlockChange.EncodedBlock[] encodedBlocks, boolean isFake) {
        WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange(
                chunk,
                isFake,
                encodedBlocks
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

}
