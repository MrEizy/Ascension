package net.thejadeproject.ascension.data_attachments;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.attachments.PlayerInputStates;
import net.thejadeproject.ascension.mob_cultivation.MobCultivationData;
import net.thejadeproject.ascension.refactor_packages.entity_data.EntityDataProvider;
import net.thejadeproject.ascension.refactor_packages.entity_data.GenericEntityData;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AscensionCraft.MOD_ID);
    public static final Supplier<AttachmentType<Double>> MOVEMENT_SPEED = ATTACHMENT_TYPES.register(
            "movement_speed", () -> AttachmentType.builder(() -> 0.1).serialize(Codec.DOUBLE).copyOnDeath().build()
    );
    public static final Supplier<AttachmentType<Double>> ATTACK_DAMAGE = ATTACHMENT_TYPES.register(
            "attack_damage", () -> AttachmentType.builder(() -> 1.0).serialize(Codec.DOUBLE).copyOnDeath().build()
    );



    public static final Supplier<AttachmentType<IEntityData>> ENTITY_DATA = ATTACHMENT_TYPES.register(
            "entity_data",
            ()->AttachmentType.<IEntityData>builder((holder) -> holder instanceof Entity entity ? new GenericEntityData(entity):null)
                    .serialize(new EntityDataProvider())
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<MobCultivationData>> MOB_RANK = ATTACHMENT_TYPES.register(
            "mob_rank",
            () -> AttachmentType.builder(holder -> holder instanceof LivingEntity ? new MobCultivationData() : null)
                    .serialize(MobCultivationData.CODEC)
                    .build()
    );


    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerInputStates>> INPUT_STATES = ATTACHMENT_TYPES.register("input_states",
            () -> AttachmentType.builder((holder) -> holder instanceof Player player ? new PlayerInputStates(player):null).build());


    public static void register(IEventBus modEventBus){
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
