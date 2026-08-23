package net.zic.ascension.configuration.mob_traits;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinitionReference;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.configuration.ConfigurationRegistries;

public record MobTraitReference(Identifier id){

    private static final Identifier defaultId = Identifier.parse("none");

    public boolean isValid(RegistryAccess access){
        if(id.equals(defaultId)) return false;
        return getTrait(access) != null;
    }

    public MobTraitDefinition getTrait(RegistryAccess access){
        Registry<MobTraitDefinition> registry = ConfigurationRegistries.MOB_TRAIT_DEFINITION_REGISTRY.get(access);
        return registry.containsKey(id) ? registry.getValue(id) : null;
    }

    public static final Codec<MobTraitReference> CODEC = Identifier.CODEC.xmap(MobTraitReference::new,MobTraitReference::id);

    public void write(ValueOutput output){
        output.putString("id",id.toString());
    }
    public static MobTraitReference of(ValueInput input){
        return new MobTraitReference( Identifier.parse(input.getStringOr("id","none")));
    }
}


