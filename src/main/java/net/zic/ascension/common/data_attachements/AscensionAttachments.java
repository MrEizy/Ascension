package net.zic.ascension.common.data_attachements;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.source.ServerOriginSource;
import net.zic.ascension.impl.core.entity.SimpleAscensionEntityData;
import net.zic.ascension.skill_casting.SkillCastHandler;

import java.util.function.Supplier;

public class AscensionAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AscensionCraft.MOD_ID);

    public static final Supplier<AttachmentType<SimpleAscensionEntityData>> SIMPLE_ENTITY_DATA = ATTACHMENT_TYPES.register(
            "simple_ascension_entity_data",()->AttachmentType.builder(
                    (holder)->{
                        if(holder instanceof LivingEntity entity){
                            return new SimpleAscensionEntityData(
                                    entity.level().isClientSide() ?
                                            new OriginSource() :
                                            new ServerOriginSource(),
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

    public static void register(IEventBus bus){
        ATTACHMENT_TYPES.register(bus);
    }
}
