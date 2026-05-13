package net.zic.ascension.util.ToolTips;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.zic.ascension.AscensionCraft;

import java.util.List;

public class ToolTipsGradient
{

    public static void modifyItemTooltip(ItemTooltipEvent event)
    {
        List<Component> tooltipComponents = event.getToolTip();
        ItemStack stack = event.getItemStack();

        if (I18n.exists("tooltip." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace() + "." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".0"))
        {
            if (event.getFlags().hasShiftDown())
            {
                for (int i = 0; i < 100; i++)
                {
                    if (!I18n.exists("tooltip." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace() + "." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "." + i))
                        break;

                    String s = I18n.get("tooltip." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace() + "." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "." + i);

                    tooltipComponents.add(DecodeTranslationKeyTags(s));

                }

            }
        }
    }


    public static Component DecodeTranslationKeyTags(String translationKey)
    {
        String s = I18n.get(translationKey);

        if (s.contains("<rgb>"))
        {
            return Component.empty()
                    .append(s.substring(0, s.indexOf("<rgb>")))
                    .append(RGBEachLetter(AscensionCraft.hue, s.substring(s.indexOf("<rgb>") + 5, s.indexOf("</rgb>")), 0.01f))
                    .append(s.substring(s.indexOf("</rgb>") + 6));
        }
        else if (s.contains("<gradient"))
        {
            float min = Float.parseFloat("0." + s.substring(s.indexOf("<gradient") + 10, s.indexOf("<gradient") + 12));
            float max = Float.parseFloat("0." + s.substring(s.indexOf("</gradient") + 11, s.indexOf("</gradient") + 13));

            return Component.empty()
                    .append(s.substring(0, s.indexOf("<gradient")))
                    .append(Gradient(AscensionCraft.hue, s.substring(s.indexOf("<gradient") + 13, s.indexOf("</gradient")), min, max))
                    .append(s.substring(s.indexOf("</gradient") + 14));
        }
        else
        {
            return Component.translatable(translationKey);
        }

    }


    public static MutableComponent Gradient(float hue, String text, float min, float max) {
        MutableComponent c = Component.empty();

        for (int i = 0; i < text.length(); i++) {
            String s = text.substring(i, i + 1);

            float pingPongedHue = mapHuePingPong(i * 0.01f + hue, min, max);
            int color = HueToRGBInt(pingPongedHue);

            MutableComponent l = Component.literal(s).withColor(color);
            c = c.append(l);
        }

        return c;
    }

    public static float mapHuePingPong(float h, float min, float max)
    {
        h = h % 1;

        // Triangle wave between 0 and 1
        float t = Math.abs(2f * h - 1f);

        // Scale to [min,max]
        return min + t * (max - min);
    }


    public static MutableComponent RGBEachLetter(float hue, String text, float speed) {
        MutableComponent c = Component.empty();

        for (int i = 0; i < text.length(); i++) {
            String s = text.substring(i, i + 1);
            int color = HueToRGBInt(i * speed + hue);
            MutableComponent l = Component.literal(s).withColor(color);
            c = c.append(l);
        }

        return c;
    }


    public static int HueToRGBInt(float hue)
    {
        int r = 0, g = 0, b = 0;

        float h = (hue - (float) Math.floor(hue)) * 6.0f;
        float f = h - (float) Math.floor(h);
        float q = 1.0f - f;
        float t = 1.0f - (1.0f - f);
        switch ((int) h)
        {
            case 0:
                r = (int) (255.0f + 0.5f);
                g = (int) (t * 255.0f + 0.5f);
                break;
            case 1:
                r = (int) (q * 255.0f + 0.5f);
                g = (int) (255.0f + 0.5f);
                break;
            case 2:
                g = (int) (255.0f + 0.5f);
                b = (int) (t * 255.0f + 0.5f);
                break;
            case 3:
                g = (int) (q * 255.0f + 0.5f);
                b = (int) (255.0f + 0.5f);
                break;
            case 4:
                r = (int) (t * 255.0f + 0.5f);
                g = 0;
                b = (int) (255.0f + 0.5f);
                break;
            case 5:
                r = (int) (255.0f + 0.5f);
                g = 0;
                b = (int) (q * 255.0f + 0.5f);
                break;
        }
        return 0xff000000 | (r << 16) | (g << 8) | (b);
    }




}

