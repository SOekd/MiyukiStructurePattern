package app.miyuki.miyukistructurepattern.util.block.version;

import app.miyuki.miyukistructurepattern.structure.workload.StructureBlock;
import app.miyuki.miyukistructurepattern.util.block.BlockChunkUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModernBlockChange implements BlockChange {

    @Override
    public void sendFakeBlockChange(@NotNull Player player, @NotNull List<StructureBlock> blocks) {
        BlockChunkUtil.groupStructureBlockByChunk(blocks).forEach((chunk, chunkBlocks) -> {
            WrapperPlayServerMultiBlockChange.EncodedBlock[] encodedBlocks = chunkBlocks.stream()
                    .map(this::createEncodedBlock)
                    .toArray(WrapperPlayServerMultiBlockChange.EncodedBlock[]::new);

            sendMultiBlockChangePacket(player, chunk, encodedBlocks, true);
        });
    }

    @Override
    public void setType(@NotNull Block block, @NotNull ItemStack item) {
        block.setType(item.getType());
    }

    @Override
    public void clearFakeBlockChange(@NotNull Player player, @NotNull List<Location> locations) {
        BlockChunkUtil.groupLocationByChunk(locations).forEach((chunk, chunkLocations) -> {
            WrapperPlayServerMultiBlockChange.EncodedBlock[] blockChangeInfos = chunkLocations.stream()
                    .map(this::createEncodedBlockFromLocation)
                    .toArray(WrapperPlayServerMultiBlockChange.EncodedBlock[]::new);

            sendMultiBlockChangePacket(player, chunk, blockChangeInfos, false);
        });
    }

    private WrapperPlayServerMultiBlockChange.EncodedBlock createEncodedBlock(StructureBlock structureBlock) {
        val blockData = SpigotConversionUtil.fromBukkitBlockData(
                structureBlock.getMaterial().getType().createBlockData()
        );
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
