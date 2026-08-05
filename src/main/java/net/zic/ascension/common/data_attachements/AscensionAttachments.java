package net.zic.ascension.common.data_attachements;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.chunks.atmospheric_qi.ChunkQiContainer;
import net.zic.ascension.common.util.AscensionAttributes;
import net.zic.ascension.impl.core.entity.SimpleAscensionEntityData;
import net.zic.ascension.mob_cultivation.MobCultivationData;
import net.zic.ascension.skill_casting.SkillCastHandler;

import java.util.function.Supplier;
import net.zic.ascension.impl.core.control.StaggerService;
import net.zic.ascension.impl.core.effect.FrozenStateService;
import net.zic.ascension.impl.core.movement.MovementService;
import net.zic.ascension.impl.runtime.projectile.NormalProjectileService;
import net.zic.ascension.impl.core.effect.SkillEffectManager;

public class AscensionAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AscensionCraft.MOD_ID);

    public static final Supplier<AttachmentType<SimpleAscensionEntityData>> SIMPLE_ENTITY_DATA = ATTACHMENT_TYPES.register(
            "simple_ascension_entity_data",()->AttachmentType.builder(
                    (holder)->{
                        if(holder instanceof LivingEntity entity){
                            return new SimpleAscensionEntityData(
                                    new OriginSource(),
                                    entity
                            );
                        }
                        return null;
                    }
            )
                    .sync(new SimpleAscensionEntityData.SyncHandler())
                    .serialize(new SimpleAscensionEntityData.Provider())
                    .copyOnDeath()
                    .build()
    );


    public static final Supplier<AttachmentType<MobCultivationData>> MOB_CULTIVATION_DATA = ATTACHMENT_TYPES.register(
            "mob_cultivation_data", () -> AttachmentType.builder(holder -> new MobCultivationData())
                    .serialize(new MobCultivationData.Provider())
                    .build()
    );


    public static final Supplier<AttachmentType<SkillCastHandler>> ASCENSION_SKILL_CAST_HANDLER = ATTACHMENT_TYPES.register(
            "ascension_skill_cast_handler",()->AttachmentType.builder(
                            (holder)->{
                                if(holder instanceof Player entity){
                                    return new SkillCastHandler(
                                            entity
                                    );
                                }
                                return null;
                            }
                    )
                    .sync(new SkillCastHandler.SyncHandler())
                    .serialize(new SkillCastHandler.Provider())
                    .copyOnDeath()
                    .build()
    );
    public static final Supplier<AttachmentType<ChunkQiContainer>> ASCENSION_CHUNK_QI_CONTAINER = ATTACHMENT_TYPES.register(
            "ascension_chunk_qi_container",()->AttachmentType.builder(
                holder-> new ChunkQiContainer(0,0,0)
            )
                    .serialize(new ChunkQiContainer.Provider())
                    .build()
    );
    public static final Supplier<AttachmentType<Double>> ENTITY_QI = ATTACHMENT_TYPES.register(
            "entity_qi", () -> AttachmentType.builder((holder)->{
                        if(!(holder instanceof LivingEntity entity)) return 0.0;
                        EntityQiProvider provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
                        if(provider == null) return 0.0;
                        return provider.getMaxQi();
                    })
                    .serialize(Codec.DOUBLE.fieldOf("value"))
                    .sync(ByteBufCodecs.DOUBLE)
                    .copyOnDeath().build()
    );


    public static final Supplier<AttachmentType<Double>> ENTITY_STAMINA = ATTACHMENT_TYPES.register(
            "entity_stamina", () -> AttachmentType.builder(holder -> {
                        if (!(holder instanceof LivingEntity entity)) {
                            return 0.0D;
                        }
                        return entity.getAttributes().hasAttribute(AscensionAttributes.MAX_STAMINA)
                                ? entity.getAttributeValue(AscensionAttributes.MAX_STAMINA)
                                : 0.0D;
                    })
                    .serialize(Codec.DOUBLE.fieldOf("value"))
                    .sync(ByteBufCodecs.DOUBLE)
                    .copyOnDeath()
                    .build()
    );
    public static final Supplier<AttachmentType<Integer>> ENTITY_STAMINA_REGEN_DELAY = ATTACHMENT_TYPES.register(
            "entity_stamina_regen_delay", () -> AttachmentType.builder(holder -> 0)
                    .serialize(Codec.INT.fieldOf("ticks"))
                    .copyOnDeath()
                    .build()
    );


    public static final Supplier<AttachmentType<FrozenStateService.State>> FROZEN_STATE = ATTACHMENT_TYPES.register(
            "frozen_state", () -> AttachmentType.builder(holder -> new FrozenStateService.State())
                    .serialize(FrozenStateService.State.CODEC.fieldOf("data"))
                    .copyOnDeath()
                    .build()
    );
    public static final Supplier<AttachmentType<StaggerService.State>> STAGGER_STATE = ATTACHMENT_TYPES.register(
            "stagger_state", () -> AttachmentType.builder(holder -> new StaggerService.State())
                    .serialize(StaggerService.State.CODEC.fieldOf("data"))
                    .build()
    );
    public static final Supplier<AttachmentType<MovementService.Anchors>> MOVEMENT_ANCHORS = ATTACHMENT_TYPES.register(
            "movement_anchors", () -> AttachmentType.builder(holder -> new MovementService.Anchors())
                    .serialize(MovementService.Anchors.CODEC.fieldOf("data"))
                    .copyOnDeath()
                    .build()
    );
    public static final Supplier<AttachmentType<SkillEffectManager.Container>> ACTIVE_SKILL_EFFECTS = ATTACHMENT_TYPES.register(
            "active_skill_effects", () -> AttachmentType.builder(holder -> new SkillEffectManager.Container())
                    .serialize(SkillEffectManager.Container.CODEC.fieldOf("data"))
                    .copyOnDeath()
                    .build()
    );
    public static final Supplier<AttachmentType<NormalProjectileService.Data>> NORMAL_PROJECTILE_DATA = ATTACHMENT_TYPES.register(
            "normal_projectile_data", () -> AttachmentType.builder(holder -> new NormalProjectileService.Data())
                    .serialize(NormalProjectileService.Data.CODEC.fieldOf("data"))
                    .build()
    );

    public static void register(IEventBus bus){
        ATTACHMENT_TYPES.register(bus);
    }
}
