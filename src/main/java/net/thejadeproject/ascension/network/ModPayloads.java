package net.thejadeproject.ascension.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.thejadeproject.ascension.AscensionCraft;

import net.thejadeproject.ascension.network.serverBound.ToggleTabletDropModePayload;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.bloodline.SyncBloodline;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.qi.SyncQi;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.herb_pouch.SyncHerbPouchPayload;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.mob_culti.SyncMobCultivation;
import net.thejadeproject.ascension.network.serverBound.UnlockChapterPayload;
import net.thejadeproject.ascension.network.serverBound.input.ChangePlayerInputState;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.SyncEntityForm;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncCurrentHealth;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.path_data.SyncPathData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.physique.SyncPhysique;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.techniques.ShowMergePromptPayload;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.herb_pouch.ExtractHerbFromPouchPayload;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.herb_pouch.InsertCarriedHerbIntoPouchPayload;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.supressors.UpdateSuppressionValue;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.techniques.MergeResponsePayload;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.skills.casting.SyncCastingInstance;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.skills.casting.SyncSlot;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.stats.SyncStat;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.toast.ShowAscensionToast;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.skills.ClearSlot;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.skills.SyncHeldSkills;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.skills.SetActiveSlot;
import net.thejadeproject.ascension.refactor_packages.network.server_bound.skills.UpdateSkillSlot;


public class ModPayloads {
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(AscensionCraft.MOD_ID).versioned("1.0");

        //=================================== CLIENT======================================
        registrar.playToClient(
                SyncHeldSkills.TYPE,
                SyncHeldSkills.STREAM_CODEC,
                SyncHeldSkills::handlePayload
        );
        registrar.playToClient(
                SyncQi.TYPE,
                SyncQi.STREAM_CODEC,
                SyncQi::handlePayload
        );
        registrar.playToClient(
                SyncEntityForm.TYPE,
                SyncEntityForm.STREAM_CODEC,
                SyncEntityForm::handlePayload
        );
        registrar.playToClient(
                SyncSlot.TYPE,
                SyncSlot.STREAM_CODEC,
                SyncSlot::handlePayload
        );
        registrar.playToClient(
                SyncCastingInstance.TYPE,
                SyncCastingInstance.STREAM_CODEC,
                SyncCastingInstance::handlePayload
        );
        registrar.playToClient(
                SyncPathData.TYPE,
                SyncPathData.STREAM_CODEC,
                SyncPathData::handlePayload
        );
        registrar.playToClient(
                SyncAttributeHolder.TYPE,
                SyncAttributeHolder.STREAM_CODEC,
                SyncAttributeHolder::handlePayload
        );

        registrar.playToClient(
                SyncStat.TYPE,
                SyncStat.STREAM_CODEC,
                SyncStat::handlePayload
        );

        registrar.playToClient(
                SyncCurrentHealth.TYPE,
                SyncCurrentHealth.STREAM_CODEC,
                SyncCurrentHealth::handlePayload
        );

        registrar.playToClient(
                SyncPhysique.TYPE,
                SyncPhysique.STREAM_CODEC,
                SyncPhysique::handlePayload
        );

        registrar.playToClient(
                SyncBloodline.TYPE,
                SyncBloodline.STREAM_CODEC,
                SyncBloodline::handlePayload
        );

        registrar.playToClient(
                ShowAscensionToast.TYPE,
                ShowAscensionToast.STREAM_CODEC,
                ShowAscensionToast::handlePayload
        );


        // Temp for display purposes
        registrar.playToClient(
                SyncMobCultivation.TYPE,
                SyncMobCultivation.STREAM_CODEC,
                SyncMobCultivation::handlePayload
        );

        registrar.playToClient(
                ShowMergePromptPayload.TYPE,
                ShowMergePromptPayload.STREAM_CODEC,
                ShowMergePromptPayload::handlePayload
        );

        registrar.playToClient(
                SyncHerbPouchPayload.TYPE,
                SyncHerbPouchPayload.STREAM_CODEC,
                SyncHerbPouchPayload::handlePayload
        );

        //===================================== SERVER ==================================



        registrar.playToServer(
                ToggleTabletDropModePayload.TYPE,
                ToggleTabletDropModePayload.STREAM_CODEC,
                ToggleTabletDropModePayload::handlePayload
        );
        registrar.playToServer(
                UpdateSuppressionValue.TYPE,
                UpdateSuppressionValue.STREAM_CODEC,
                UpdateSuppressionValue::handlePayload
        );

        registrar.playToServer(
                UpdateSkillSlot.TYPE,
                UpdateSkillSlot.STREAM_CODEC,
                UpdateSkillSlot::handlePayload
        );
        registrar.playToServer(
                ClearSlot.TYPE,
                ClearSlot.STREAM_CODEC,
                ClearSlot::handlePayload
        );


        registrar.playToServer(
                ChangePlayerInputState.TYPE,
                ChangePlayerInputState.STREAM_CODEC,
                ChangePlayerInputState::handlePayload

        );
        registrar.playToServer(
                SetActiveSlot.TYPE,
                SetActiveSlot.STREAM_CODEC,
                SetActiveSlot::handlePayload
        );

        registrar.playToServer(
                MergeResponsePayload.TYPE,
                MergeResponsePayload.STREAM_CODEC,
                MergeResponsePayload::handlePayload
        );
        registrar.playToServer(
                UnlockChapterPayload.TYPE,
                UnlockChapterPayload.STREAM_CODEC,
                UnlockChapterPayload::handlePayload
        );

        registrar.playToServer(
                ExtractHerbFromPouchPayload.TYPE,
                ExtractHerbFromPouchPayload.STREAM_CODEC,
                ExtractHerbFromPouchPayload::handlePayload
        );

        registrar.playToServer(
                InsertCarriedHerbIntoPouchPayload.TYPE,
                InsertCarriedHerbIntoPouchPayload.STREAM_CODEC,
                InsertCarriedHerbIntoPouchPayload::handlePayload
        );
    }
}
