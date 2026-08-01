package net.zic.ascension.api.ascension.core;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityPathBonusHolder;
import net.zic.zenithlib.stats.ZenithStatHolder;

import java.util.function.Supplier;

public class CoreAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AscensionCraft.MOD_ID);

    public static final Supplier<AttachmentType<AscensionEntityPathBonusHolder>> PATH_BONUS_HOLDER = ATTACHMENT_TYPES.register(
            "path_bonus_holder",()-> AttachmentType.builder(
                            (holder)-> new AscensionEntityPathBonusHolder((LivingEntity) holder)
                    )
                    .sync(new AscensionEntityPathBonusHolder.SyncHandler())
                    .build()
    );

    public static void register(IEventBus bus){
        ATTACHMENT_TYPES.register(bus);
    }
}
