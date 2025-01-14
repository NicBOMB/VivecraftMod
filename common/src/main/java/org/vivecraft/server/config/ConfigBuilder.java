package org.vivecraft.server.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.ConfigSpec.CorrectionListener;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.vivecraft.client.gui.settings.GuiListValueEditScreen;
import org.vivecraft.client.gui.widgets.SettingsList.ResettableEntry;

import java.util.*;
import java.util.function.Predicate;

import static org.vivecraft.client_vr.VRState.mc;

public class ConfigBuilder {

    private final CommentedConfig config;
    private final ConfigSpec spec;
    private final Deque<String> stack = new ArrayDeque<>();
    private final List<ConfigValue> configValues = new ArrayList<>();

    public ConfigBuilder(CommentedConfig config, ConfigSpec spec) {
        this.config = config;
        this.spec = spec;
    }

    /**
     * pushes the given subPath to the path
     *
     * @param subPath new sub path
     * @return this builder, for chaining commands
     */
    public ConfigBuilder push(String subPath) {
        stack.add(subPath);
        return this;
    }

    /**
     * pops the last sub path
     *
     * @return this builder, for chaining commands
     */
    public ConfigBuilder pop() {
        stack.removeLast();
        return this;
    }

    /**
     * add a comment to the config
     *
     * @param comment Text for the comment
     * @return this builder, for chaining commands
     */
    public ConfigBuilder comment(String comment) {
        config.setComment(stack.stream().toList(), comment);
        return this;
    }

    private void addDefaultValueComment(List<String> path, int defaultValue, int min, int max) {
        String oldComment = config.getComment(path);
        config.setComment(path, (oldComment == null ? "" : oldComment + "\n ")
            + "default: %d, min: %d, max: %d".formatted(defaultValue, min, max));
    }

    private void addDefaultValueComment(List<String> path, double defaultValue, double min, double max) {
        String oldComment = config.getComment(path);
        config.setComment(path, (oldComment == null ? "" : oldComment + "\n ")
            + new Formatter(Locale.US).format("default: %.2f, min: %.2f, max: %.2f", defaultValue, min, max));
    }

    /**
     * corrects the attached config, with the built spec
     *
     * @param listener listener to send correction to
     */
    public void correct(CorrectionListener listener) {
        spec.correct(config, listener);
    }

    public List<ConfigValue> getConfigValues() {
        return configValues;
    }

    // general Settings

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T> ConfigValue<T> define(T defaultValue) {
        List<String> path = stack.stream().toList();
        spec.define(path, defaultValue);
        stack.removeLast();

        ConfigValue<T> value = new ConfigValue<>(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @param min          the minimum value, that  is valid for this setting
     * @param max          the maximum value, that  is valid for this setting
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T extends Comparable<? super T>> ConfigValue<T> defineInRange(T defaultValue, T min, T max) {
        List<String> path = stack.stream().toList();
        spec.defineInRange(path, defaultValue, min, max);
        stack.removeLast();

        ConfigValue<T> value = new ConfigValue<>(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @param validator    Predicate, that signals, what values are accepted
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T> ListValue<T> defineList(List<T> defaultValue, Predicate<Object> validator) {
        List<String> path = stack.stream().toList();
        spec.defineList(path, defaultValue, validator);
        stack.removeLast();

        ListValue<T> value = new ListValue<>(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @param validValues  Collection of values that are accepted
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T> InListValue<T> defineInList(T defaultValue, Collection<? extends T> validValues) {
        List<String> path = stack.stream().toList();
        spec.defineInList(path, defaultValue, validValues);
        stack.removeLast();

        InListValue<T> value = new InListValue<>(config, path, defaultValue, validValues);
        configValues.add(value);
        return value;
    }

    /**
     * same as {@link #define define(T defaultValue)} but returns a {@link BooleanValue}
     */
    public BooleanValue define(boolean defaultValue) {
        List<String> path = stack.stream().toList();
        spec.define(path, defaultValue);
        stack.removeLast();

        BooleanValue value = new BooleanValue(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    /**
     * same as {@link #define define(T defaultValue)} but returns a {@link StringValue}
     */
    public StringValue define(String defaultValue) {
        List<String> path = stack.stream().toList();
        spec.define(path, defaultValue);
        stack.removeLast();

        StringValue value = new StringValue(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    /**
     * same as {@link #defineInRange defineInRange(T defaultValue, T min, T max)} but returns a {@link DoubleValue}
     */
    public DoubleValue defineInRange(double defaultValue, double min, double max) {
        List<String> path = stack.stream().toList();
        spec.defineInRange(path, defaultValue, min, max);
        stack.removeLast();
        addDefaultValueComment(path, defaultValue, min, max);

        DoubleValue value = new DoubleValue(config, path, defaultValue, min, max);
        configValues.add(value);
        return value;
    }

    /**
     * same as {@link #defineInRange defineInRange(T defaultValue, T min, T max)} but returns a {@link DoubleValue}
     */
    public IntValue defineInRange(int defaultValue, int min, int max) {
        List<String> path = stack.stream().toList();
        spec.defineInRange(path, defaultValue, min, max);
        stack.removeLast();
        addDefaultValueComment(path, defaultValue, min, max);

        IntValue value = new IntValue(config, path, defaultValue, min, max);
        configValues.add(value);
        return value;
    }


    public static class ConfigValue<T> {

        // the config, this setting is part of
        private final CommentedConfig config;
        private final List<String> path;
        private final T defaultValue;
        // cache te value to minimize config lookups
        private T cachedValue = null;

        public ConfigValue(CommentedConfig config, List<String> path, T defaultValue) {
            this.config = config;
            this.path = path;
            this.defaultValue = defaultValue;
        }

        public T get() {
            if (cachedValue == null) {
                cachedValue = config.get(path);
            }
            return cachedValue;
        }

        public void set(T newValue) {
            cachedValue = newValue;
            config.set(path, newValue);
        }

        public T reset() {
            config.set(path, defaultValue);
            cachedValue = defaultValue;
            return defaultValue;
        }

        public boolean isDefault() {
            return Objects.equals(get(), defaultValue);
        }

        public String getComment() {
            String comment = config.getComment(path);
            return comment != null ? comment : "";
        }

        public String getPath() {
            return String.join(".", path);
        }

        public AbstractWidget getWidget(int width, int height) {
            return Button
                .builder(Component.literal(String.valueOf(get())), button -> {
                })
                .bounds(0, 0, width, height)
                .tooltip(Tooltip.create(Component.literal(getComment())))
                .build();
        }
    }

    public static class BooleanValue extends ConfigValue<Boolean> {
        public BooleanValue(CommentedConfig config, List<String> path, boolean defaultValue) {
            super(config, path, defaultValue);
        }

        @Override
        public AbstractWidget getWidget(int width, int height) {
            return CycleButton
                .onOffBuilder(get())
                .displayOnlyValue()
                .withTooltip((bool) -> getComment() != null ? Tooltip.create(Component.literal(getComment())) : null)
                .create(0, 0, width, height, Component.empty(), (button, bool) -> set(bool));
        }
    }

    public static class StringValue extends ConfigValue<String> {
        public StringValue(CommentedConfig config, List<String> path, String defaultValue) {
            super(config, path, defaultValue);
        }

        @Override
        public AbstractWidget getWidget(int width, int height) {
            EditBox box = new EditBox(mc.font, 0, 0, width - 1, height, Component.literal(get())) {
                @Override
                public boolean charTyped(char c, int i) {
                    boolean ret = super.charTyped(c, i);
                    set(this.getValue());
                    return ret;
                }

                @Override
                public boolean keyPressed(int i, int j, int k) {
                    boolean ret = super.keyPressed(i, j, k);
                    set(this.getValue());
                    return ret;
                }
            };
            box.setMaxLength(1000);
            box.setValue(get());
            box.setTooltip(Tooltip.create(Component.literal(getComment())));
            return box;
        }
    }

    public static class ListValue<T> extends ConfigValue<List<T>> {
        public ListValue(CommentedConfig config, List<String> path, List<T> defaultValue) {
            super(config, path, defaultValue);
        }

        @Override
        public AbstractWidget getWidget(int width, int height) {
            // TODO handle other types than String
            return Button
                .builder(
                    Component.translatable("vivecraft.options.editlist"),
                    button -> mc.setScreen(
                        new GuiListValueEditScreen(Component.literal(getPath().substring(getPath().lastIndexOf("."))), mc.screen, (ListValue<String>) this)
                    )
                )
                .size(width, height)
                .tooltip(Tooltip.create(Component.literal(getComment())))
                .build();
        }
    }

    public static class InListValue<T> extends ConfigValue<T> {
        private final Collection<? extends T> validValues;

        public InListValue(CommentedConfig config, List<String> path, T defaultValue, Collection<? extends T> validValues) {
            super(config, path, defaultValue);
            this.validValues = validValues;
        }

        public Collection<? extends T> getValidValues() {
            return validValues;
        }

        @Override
        public AbstractWidget getWidget(int width, int height) {
            return CycleButton
                .builder((newValue) -> Component.literal(String.valueOf(newValue)))
                .withInitialValue(get())
                // toArray is needed here, because the button uses Objects, and the collection is of other types
                .withValues(getValidValues().toArray())
                .displayOnlyValue()
                .withTooltip((bool) -> getComment() != null ? Tooltip.create(Component.literal(getComment())) : null)
                .create(0, 0, width, height, Component.empty(), (button, newValue) -> set((T) newValue));
        }
    }

    public static abstract class NumberValue<E extends Number> extends ConfigValue<E> {

        private final E min;
        private final E max;

        public NumberValue(CommentedConfig config, List<String> path, E defaultValue, E min, E max) {
            super(config, path, defaultValue);
            this.min = min;
            this.max = max;
        }

        public E getMin() {
            return min;
        }

        public E getMax() {
            return max;
        }

        public double normalize() {
            return Mth.clamp((this.get().doubleValue() - min.doubleValue()) / (max.doubleValue() - min.doubleValue()), 0.0D, 1.0D);
        }

        abstract public void fromNormalized(double value);

        @Override
        public AbstractWidget getWidget(int width, int height) {
            AbstractSliderButton widget = new AbstractSliderButton(0, 0, ResettableEntry.valueButtonWidth, 20, Component.literal(String.valueOf(get())), normalize()) {
                @Override
                protected void updateMessage() {
                    setMessage(Component.literal(String.valueOf(get())));
                }

                @Override
                protected void applyValue() {
                    fromNormalized(value);
                }
            };
            widget.setTooltip(Tooltip.create(Component.literal(getComment())));
            return widget;
        }
    }

    public static class IntValue extends NumberValue<Integer> {

        public IntValue(CommentedConfig config, List<String> path, int defaultValue, int min, int max) {
            super(config, path, defaultValue, min, max);
        }

        @Override
        public void fromNormalized(double value) {
            double newValue = this.getMin() + (this.getMax() - this.getMin()) * value;
            this.set(Mth.floor(newValue + 0.5));
        }
    }

    public static class DoubleValue extends NumberValue<Double> {

        public DoubleValue(CommentedConfig config, List<String> path, double defaultValue, double min, double max) {
            super(config, path, defaultValue, min, max);
        }

        @Override
        public void fromNormalized(double value) {
            double newValue = this.getMin() + (this.getMax() - this.getMin()) * value;
            this.set(Math.round(newValue * 100.0) / 100.0);
        }
    }
}
