package net.zic.ascension.impl.core.path.foundation;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.source.OriginSource;
import net.zic.ascension.impl.core.path.simple.SimplePathData;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.ArrayList;

public class FoundationPathData extends SimplePathData {
    public FoundationPathData(Identifier path) {
        super(path);
    }

    private static class MajorRealmFoundation{
        private double progress;
        private int foundationRealm;

        public void setProgress(double progress){
            this.progress = progress;
        }
        public double getProgress(){
            return progress;
        }
        public void setFoundationRealm(int realm){
            this.foundationRealm = realm;
        }
        public int getFoundationRealm(){
            return foundationRealm;
        }
    }
    private final ArrayList<MajorRealmFoundation> foundations = new ArrayList<>(){{add(new MajorRealmFoundation());}};
    private ArrayList<MajorRealmFoundation> cachedFoundations = new ArrayList<>();
    @Override
    public void onRealmUp(OriginSource source) {
        super.onRealmUp(source);
        if(getMinorRealm() == 0 && foundations.size() <=getMajorRealm()) addFoundation(source);
    }

    @Override
    public void onRealmDown(OriginSource source) {
        super.onRealmDown(source);
        if(getMinorRealm() == getMaxMinorRealm(getMajorRealm(),source.getRegistryAccess())) removeFoundation(source);
    }

    public void addFoundation(OriginSource source){
        MajorRealmFoundation foundation = cachedFoundations.isEmpty() ? new MajorRealmFoundation() : cachedFoundations.removeFirst();
        foundations.add(foundation);
        int foundationRealm = foundation.foundationRealm;
        foundation.setFoundationRealm(0);
        handleFoundationRealmChange(source,getMajorRealm(),foundationRealm);
    }
    public void removeFoundation(OriginSource source){
        //TODO handle foundation change
        MajorRealmFoundation foundation = foundations.getLast();

        int foundationRealm = foundation.foundationRealm;
        handleFoundationRealmChange(source,getMajorRealm()+1,0);
        foundation.setFoundationRealm(foundationRealm); //since the cache holds onto an object reference we restore value
        foundations.removeLast();
    }

    public int getFoundationRealm(int majorRealm){
        return majorRealm>=foundations.size() ? 0 : foundations.get(majorRealm).foundationRealm;
    }
    public double getFoundationRealmProgress(int majorRealm){
        return majorRealm>=foundations.size() ? 0 : foundations.get(majorRealm).progress;
    }

    public int getCurrentFoundationRealm(){
        return getFoundationRealm(getMajorRealm());
    }
    public double getCurrentFoundationProgress(){
        return getFoundationRealmProgress(getMajorRealm());
    }

    public void setFoundationRealmProgress(int majorRealm,double progress){
        if(majorRealm>=foundations.size()) return;

        foundations.get(majorRealm).setProgress(progress);
    }

    public void handleFoundationRealmChange(OriginSource source,int majorRealm,int foundationRealm){
        if(foundations.size() <= majorRealm) return;

        Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,getPath(),source.getRegistryAccess());
        if(!(path instanceof FoundationPath foundationPath)) return;

        //TODO clamp foundation realm

        MajorRealmFoundation foundation = foundations.get(majorRealm);
        foundationRealm = Math.clamp(foundationRealm,0,foundationPath.getMaxFoundationRealm(majorRealm));

        if(foundation.foundationRealm == foundationRealm) return;

        if(foundation.foundationRealm < foundationRealm){
            for(int i = foundation.foundationRealm+1; i<= foundationRealm;i++){
                foundation.setFoundationRealm(i);
                if(foundationPath.getFoundationActionHolder(majorRealm) == null) continue;
                foundationPath.getFoundationActionHolder(majorRealm).run(
                        source,
                        getPath(),
                        this,
                        ProgressDirection.UP
                );
            }
        }else{
            for (int i = foundation.foundationRealm-1;i>=foundationRealm;i--){
                foundation.setFoundationRealm(i);
                if(foundationPath.getFoundationActionHolder(majorRealm) == null) continue;
                foundationPath.getFoundationActionHolder(majorRealm).run(
                        source,
                        getPath(),
                        this,
                        ProgressDirection.DOWN
                );

            }
        }
    }

    @Override
    public void simulateProgression(OriginSource source) {
        cachedFoundations = new ArrayList<>(foundations);
        foundations.clear();
        addFoundation(source); //ensures we add the first foundation
        super.simulateProgression(source);
        cachedFoundations.clear();


    }

    @Override
    public void removeFromSource(OriginSource source) {
        cachedFoundations = new ArrayList<>(foundations);
        super.removeFromSource(source);

        foundations.clear();
        foundations.addAll(cachedFoundations);
        cachedFoundations.clear();
    }

    //TODO

    @Override
    public void load(ValueInput input, RegistryAccess registryAccess) {

        super.load(input, registryAccess);
        foundations.clear();
        foundations.addAll(NbtHelpers.readList(input,"foundation",(elementInput,id)->{
            MajorRealmFoundation majorRealmFoundation = new MajorRealmFoundation();
            majorRealmFoundation.setFoundationRealm(elementInput.getIntOr("realm",0));
            majorRealmFoundation.setProgress(elementInput.getDoubleOr("progress",0));

            return majorRealmFoundation;
        }));

        System.out.println(foundations);
    }

    @Override
    public void write(ValueOutput output) {
        super.write(output);

        NbtHelpers.writeCollection(output,"foundation",foundations,(elementOutput,id,value)->{
            elementOutput.putInt("realm",value.foundationRealm);
            elementOutput.putDouble("progress",value.progress);
        });
    }

    @Override
    public void encode(ByteBuf buf) {
        super.encode(buf);

        ByteBufHelpers.encodeCollection(foundations, buf, (foundation, byteBuf) -> {
            byteBuf.writeInt(foundation.getFoundationRealm());
            byteBuf.writeDouble(foundation.getProgress());
        });
    }

    @Override
    public void decode(ByteBuf buf, RegistryAccess access) {
        super.decode(buf, access);

        foundations.clear();
        foundations.addAll(ByteBufHelpers.decodeArray(buf, byteBuf -> {
            MajorRealmFoundation foundation = new MajorRealmFoundation();
            foundation.setFoundationRealm(byteBuf.readInt());
            foundation.setProgress(byteBuf.readDouble());
            return foundation;
        }));

        if (foundations.isEmpty()) {
            foundations.add(new MajorRealmFoundation());
        }
    }
}
