package app.miyuki.miyukistructurepattern.structure;

import java.util.List;

public interface StructureLoader {

    Structure getLoadedStructure(String id);

    List<Structure> getLoadedStructures();

    void loadAll();

}
