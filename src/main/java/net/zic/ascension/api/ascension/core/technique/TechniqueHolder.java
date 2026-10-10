package net.zic.ascension.api.ascension.core.technique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class TechniqueHolder implements DataSourceInstance<TechniqueDataSource> {
    final HashMap<Identifier, TechniqueData> techniques = new HashMap<>();
    final HashSet<Identifier> dirtyTechniques = new HashSet<>();
    final HashSet<Identifier> toRemoveTechniques = new HashSet<>();

    public boolean addTechnique(Identifier technique, TechniqueData data) {
        if (technique == null || data == null || hasTechnique(technique)) {
            return false;
        }
        techniques.put(technique, data);
        dirtyTechniques.add(technique);
        toRemoveTechniques.remove(technique);
        return true;
    }

    public boolean removeTechnique(Identifier technique) {
        if (!hasTechnique(technique)) {
            return false;
        }
        techniques.remove(technique);
        dirtyTechniques.remove(technique);
        toRemoveTechniques.add(technique);
        return true;
    }

    public boolean hasTechnique(Identifier technique) {
        return techniques.containsKey(technique);
    }

    public TechniqueData getTechniqueData(Identifier technique) {
        return techniques.get(technique);
    }

    public Technique getTechnique(Identifier technique, RegistryAccess access) {
        return CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, technique, access);
    }

    public Collection<Identifier> getTechniques() {
        return techniques.keySet();
    }

    public void markTechniqueDirty(Identifier technique) {
        if (hasTechnique(technique)) {
            dirtyTechniques.add(technique);
        }
    }
    public Map<Identifier, TechniqueData> getRawData() {
        return Map.copyOf(techniques);
    }

    public void setRawData(Map<Identifier, TechniqueData> rawData) {
        clearContainer();
        for (Map.Entry<Identifier, TechniqueData> entry : rawData.entrySet()) {
            addTechnique(entry.getKey(), entry.getValue());
        }
    }

    public void clearContainer() {
        techniques.clear();
        dirtyTechniques.clear();
        toRemoveTechniques.clear();
    }
    @Override
    public TechniqueDataSource getDataSource() {
        return CoreDataSources.TECHNIQUE_DATA_SOURCE.get();
    }
}
