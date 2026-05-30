package net.zic.ascension.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeHandler;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.bloodline.BloodlineType;
import net.zic.ascension.datapack.bloodline.AscensionBloodlineTypes;
import net.zic.ascension.datapack.bloodline.SimpleBloodlineType;

import java.util.Collection;
import java.util.List;

public class SimpleBloodline implements Bloodline {

    private final Component name;
    private final Component description;
    private final PurityChangeHandler handler;

    public SimpleBloodline(Component name, Component description, List<PurityChangeHandler.PurityChangeListener> handler){
        this.name = name;
        this.description = description;
        this.handler = new PurityChangeHandler(handler);
    }

    @Override
    public BloodlineType getType() {
        return AscensionBloodlineTypes.SIMPLE_BLOODLINE_TYPE.get();
    }

    public List<PurityChangeHandler.PurityChangeListener> getListeners(){
        return handler.getListeners();
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public Collection<Identifier> onAdded(OriginSource source, BloodlineData data) {
        return List.of(); //TODO run purity change 1 up
    }

    @Override
    public Collection<Identifier> onRemoved(OriginSource source, BloodlineData data) {
        return List.of(); //TODO run purity change 1 down
    }

    @Override
    public void applyToEntity(LivingEntity entity, BloodlineData data) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, BloodlineData data) {

    }

    @Override
    public void purityDown(OriginSource source, BloodlineData data, int newPurity) {
        handler.runPurityDown(source,this,data);
    }

    @Override
    public void purityUp(OriginSource source, BloodlineData data, int newPurity) {
        handler.runPurityUp(source,this,data);
    }

    @Override
    public BloodlineData newData() {
        return new SimpleBloodlineData();
    }

    @Override
    public BloodlineData loadData(ValueInput input) {
        return new SimpleBloodlineData(input);
    }

    @Override
    public BloodlineData loadData(ByteBuf buf) {
        return new SimpleBloodlineData(buf);
    }
}
