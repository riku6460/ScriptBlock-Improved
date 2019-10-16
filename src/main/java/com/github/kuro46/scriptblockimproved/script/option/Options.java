package com.github.kuro46.scriptblockimproved.script.option;

import com.github.kuro46.scriptblockimproved.script.option.placeholder.PlaceholderGroup;
import com.github.kuro46.scriptblockimproved.script.option.placeholder.SourceData;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;

public final class Options {

    private final ImmutableList<Option> options;

    public Options(final List<Option> options) {
        this.options = ImmutableList.copyOf(options);
    }

    public static Options parse(
            @NonNull final OptionHandlers handlers,
            @NonNull final String str) throws ParseException {
        return OptionParser.parse(handlers, str);
    }

    public static Options fromJson(final JsonArray json) {
        Objects.requireNonNull(json, "'json' cannot be null");

        final List<Option> options = new ArrayList<>();
        json.forEach(element -> options.add(Option.fromJson((JsonObject) element)));
        return new Options(options);
    }

    public JsonArray toJson() {
        final JsonArray json = new JsonArray();
        options.forEach(option -> json.add(option.toJson()));
        return json;
    }

    public Options replaced(
            final PlaceholderGroup placeholderGroup,
            final SourceData data) {
        final List<Option> replaced = options.stream()
            .map(option -> option.replaced(placeholderGroup, data))
            .collect(Collectors.toList());
        return new Options(replaced);
    }

    public void forEach(final Consumer<Option> consumer) {
        options.forEach(consumer);
    }

    public Stream<Option> stream() {
        return options.stream();
    }

    public ImmutableList<Option> getView() {
        return options;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Options)) return false;
        final Options castedOther = (Options) other;

        return this.options.equals(castedOther.options);
    }

    @Override
    public int hashCode() {
        return options.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("options", options)
            .toString();
    }
}
