package net.zic.ascension.api.ascension.core.technique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

public class TechniqueHolder implements DataSourceInstance {

    private final HashMap<Identifier, TechniqueData> techniques = new HashMap<>();
    private final HashSet<Identifier> dirtyTechniques = new HashSet<>();
    private final HashSet<Identifier> toRemoveTechniques = new HashSet<>();

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

    @Override
    public DataSource getDataSource() {
        return CoreHolderProviders.TECHNIQUE_HOLDER_PROVIDER.get();
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

    public void write(ValueOutput output, RegistryAccess access) {
        ValueOutput.ValueOutputList techniqueOutputList = output.childrenList("techniques");
        for (Identifier techniqueId : getTechniques()) {
            AscensionCraft.LOGGER.debug("Writing Technique {}", techniqueId);
            try {
                ValueOutput techniqueOutput = techniqueOutputList.addChild();
                NbtHelpers.writeIdentifier(techniqueOutput, "id", techniqueId);
                getTechniqueData(techniqueId).write(techniqueOutput.child("data"));
            } catch (Exception exception) {
                AscensionCraft.LOGGER.error("Error writing technique {}", techniqueId);
                AscensionCraft.LOGGER.error("stacktrace: ", exception);
            }
        }
    }

    public void read(ValueInput input, RegistryAccess access) {
        try {
            ValueInput.ValueInputList techniquesInput = input.childrenListOrEmpty("techniques");
            for (ValueInput techniqueInput : techniquesInput.stream().toList()) {
                try {
                    Identifier techniqueId = NbtHelpers.readIdentifier(techniqueInput, "id");
                    AscensionCraft.LOGGER.debug("Reading Technique {}", techniqueId);

                    Optional<ValueInput> dataInput = techniqueInput.child("data");
                    Technique technique = getTechnique(techniqueId, access);
                    if (technique == null) {
                        throw new IllegalStateException("unknown technique " + techniqueId);
                    }
                    if (dataInput.isEmpty()) {
                        throw new IllegalStateException("no technique data present for technique " + techniqueId);
                    }
                    addTechnique(techniqueId, technique.loadData(dataInput.get()));
                } catch (Exception exception) {
                    AscensionCraft.LOGGER.error("Error loading technique");
                    AscensionCraft.LOGGER.error("stacktrace: ", exception);
                }
            }
        } catch (Exception exception) {
            AscensionCraft.LOGGER.error("Error loading all techniques");
            AscensionCraft.LOGGER.error("stacktrace: ", exception);
        }
    }

    public void encode(ByteBuf buffer, RegistryAccess access, boolean fullPatch) {
        buffer.writeBoolean(fullPatch);
        if (fullPatch) {
            encodeFullPatch(buffer, access);
        } else {
            encodePartialPatch(buffer, access);
        }
        dirtyTechniques.clear();
        toRemoveTechniques.clear();
    }

    protected void encodeFullPatch(ByteBuf buffer, RegistryAccess access) {
        buffer.writeInt(techniques.size());
        for (Identifier techniqueId : techniques.keySet()) {
            ByteBufHelpers.encodeIdentifier(techniqueId, buffer);
            getTechniqueData(techniqueId).encode(buffer);
        }
    }

    protected void encodePartialPatch(ByteBuf buffer, RegistryAccess access) {
        buffer.writeInt(dirtyTechniques.size());
        for (Identifier techniqueId : dirtyTechniques) {
            ByteBufHelpers.encodeIdentifier(techniqueId, buffer);
            getTechniqueData(techniqueId).encode(buffer);
        }
        ByteBufHelpers.encodeCollection(toRemoveTechniques, buffer, ByteBufHelpers::encodeIdentifier);
    }

    public void decode(ByteBuf buffer, RegistryAccess access) {
        if (buffer.readBoolean()) {
            decodeFullPatch(buffer, access);
        } else {
            decodePartialPatch(buffer, access);
        }
        dirtyTechniques.clear();
        toRemoveTechniques.clear();
    }

    protected void decodeFullPatch(ByteBuf buffer, RegistryAccess access) {
        techniques.clear();
        int size = buffer.readInt();
        for (int index = 0; index < size; index++) {
            Identifier techniqueId = ByteBufHelpers.decodeIdentifier(buffer);
            Technique technique = getTechnique(techniqueId, access);
            if (technique == null) {
                throw new IllegalStateException("unknown technique " + techniqueId);
            }
            techniques.put(techniqueId, technique.loadData(buffer));
        }
    }

    protected void decodePartialPatch(ByteBuf buffer, RegistryAccess access) {
        int size = buffer.readInt();
        for (int index = 0; index < size; index++) {
            Identifier techniqueId = ByteBufHelpers.decodeIdentifier(buffer);
            Technique technique = getTechnique(techniqueId, access);
            if (technique == null) {
                throw new IllegalStateException("unknown technique " + techniqueId);
            }
            techniques.put(techniqueId, technique.loadData(buffer));
        }
        ByteBufHelpers.decodeArray(buffer, ByteBufHelpers::decodeIdentifier)
                .forEach(techniques::remove);
    }
}
