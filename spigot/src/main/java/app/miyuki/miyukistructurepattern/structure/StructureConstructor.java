package app.miyuki.miyukistructurepattern.structure;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface StructureConstructor<S extends Structure> {

    void clearPreview(Player player);

    void preview(Player player, Location origin, S structure);

    void refreshPreview(Player player);

    boolean construct(Player player, Location origin, S structure);

}
