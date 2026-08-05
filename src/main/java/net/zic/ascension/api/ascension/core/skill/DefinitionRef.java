package net.zic.ascension.api.ascension.core.skill;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record DefinitionRef<T>(Optional<String> local, Optional<Identifier> global, Optional<T> inline) {
    public DefinitionRef {
        local = local == null ? Optional.empty() : local;
        global = global == null ? Optional.empty() : global;
        inline = inline == null ? Optional.empty() : inline;
        if ((local.isPresent() ? 1 : 0) + (global.isPresent() ? 1 : 0) + (inline.isPresent() ? 1 : 0) != 1) {
            throw new IllegalArgumentException("A definition reference must be local, global, or inline");
        }
    }

    public static <T> DefinitionRef<T> local(String name) {
        return new DefinitionRef<>(Optional.of(name), Optional.empty(), Optional.empty());
    }

    public static <T> DefinitionRef<T> global(Identifier id) {
        return new DefinitionRef<>(Optional.empty(), Optional.of(id), Optional.empty());
    }

    public static <T> DefinitionRef<T> inline(T value) {
        return new DefinitionRef<>(Optional.empty(), Optional.empty(), Optional.of(value));
    }

    public static <T> Codec<DefinitionRef<T>> codec(Codec<T> inlineCodec) {
        return Codec.either(Codec.STRING, inlineCodec).comapFlatMap(
                value -> value.map(DefinitionRef::parseReference, entry -> DataResult.success(DefinitionRef.inline(entry))),
                value -> value.inline()
                        .<Either<String, T>>map(Either::right)
                        .orElseGet(() -> Either.left(value.local().map(name -> "#" + name).orElseGet(() -> value.global().orElseThrow().toString())))
        );
    }

    private static <T> DataResult<DefinitionRef<T>> parseReference(String value) {
        if (value == null || value.isBlank()) {
            return DataResult.error(() -> "Definition reference cannot be blank");
        }
        if (value.charAt(0) == '#') {
            String name = value.substring(1);
            return name.isBlank()
                    ? DataResult.error(() -> "Local definition reference cannot be blank")
                    : DataResult.success(DefinitionRef.local(name));
        }
        try {
            return DataResult.success(DefinitionRef.global(Identifier.parse(value)));
        } catch (RuntimeException exception) {
            return DataResult.error(() -> "Invalid definition reference: " + value);
        }
    }
}
