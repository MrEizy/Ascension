package net.thejadeproject.ascension.refactor_packages.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByteBufUtil {
    public static StreamCodec<ByteBuf, List<String>> STRING_LIST = new StreamCodec<ByteBuf, List<String>>() {
        public List<String> decode(ByteBuf buf) {
            int size=buf.readInt();
            String[] array = new String[size];
            for(int i =0;i<size;i++){
                array[i] = readString(buf);
            }
            return Arrays.stream(array).toList();
        }

        public void encode(ByteBuf buf, List<String> array) {
            buf.writeInt(array.size());
            for(String val : array){
                encodeString(buf,val);
            }
        }
    };
    public static StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> ITEM_STACK_LIST = new StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>>() {
        @Override
        public List<ItemStack> decode(RegistryFriendlyByteBuf buf) {
            int size=buf.readInt();
            ArrayList<ItemStack> array = new ArrayList<>();
            for(int i =0;i<size;i++){
                //System.out.println("decoding slot " + i );
                if(!buf.readBoolean()) array.add(ItemStack.EMPTY);
                else array.add(ItemStack.STREAM_CODEC.decode(buf));
            }
            return array;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, List<ItemStack> itemStacks) {
            buf.writeInt(itemStacks.size());
            //System.out.println("encoding " + itemStacks.size() +" items");
            for(ItemStack itemStack : itemStacks){
                buf.writeBoolean(!itemStack.isEmpty());
                if(itemStack.isEmpty()) continue;
                ItemStack.STREAM_CODEC.encode(buf,itemStack);
            }
        }
    };

    public static void encodeString(RegistryFriendlyByteBuf buf,String string){
        buf.writeInt(string.length());
        buf.writeCharSequence(string, Charset.defaultCharset());
    }
    public static void encodeString(ByteBuf buf,String string){
        buf.writeInt(string.length());
        buf.writeCharSequence(string, Charset.defaultCharset());
    }
    public static String readString(ByteBuf buf){
        return (String) buf.readCharSequence(buf.readInt(),Charset.defaultCharset());
    }
    public static String readString(RegistryFriendlyByteBuf buf){
        return (String) buf.readCharSequence(buf.readInt(),Charset.defaultCharset());
    }
    public static ResourceLocation readResourceLocation(RegistryFriendlyByteBuf buf){
        return ResourceLocation.bySeparator(readString(buf),':');
    }
}
