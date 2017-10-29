package me.fru1t.worddropper.settings.colortheme;

import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleableRes;
import android.util.AttributeSet;
import android.view.View;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import me.fru1t.worddropper.settings.ColorTheme;

/**
 * Handles the xml attributes associated to a themed widget (view). Only a single proxy should be
 * assigned to a widget instance.
 */
public class ColorThemeViewProxy implements ColorThemeEventHandler {
    /**
     * An action corresponds to mapping a single attribute to a method call. For example, mapping
     * the xml attribute "backgroundColorTheme" to the View#setBackgroundColor method.
     */
    public static class AttributeMap {
        private final @StyleableRes int attr;
        private final ColorThemeXml defaultColor;
        private final Consumer<Integer> action;

        /**
         * @param attr The attribute to read from the xml.
         * @param defaultColor The default value this action should be passed if the attribute
         *                     doesn't exist.
         * @param action The method that's called, passed the color that's placed in the attribute.
         */
        public AttributeMap(@StyleableRes int attr, ColorThemeXml defaultColor,
                Consumer<Integer> action) {
            this.attr = attr;
            this.defaultColor = defaultColor;
            this.action = action;
        }
    }

    private static class Action {
        final Consumer<Integer> action;
        final ColorThemeXml colorThemeXml;

        Action(Consumer<Integer> action, ColorThemeXml colorThemeXml) {
            this.action = action;
            this.colorThemeXml = colorThemeXml;
        }
    }

    private final Action[] actions;

    /**
     * @param view The widget to read the xml from.
     * @param set The widget's AttributeSet. This should be passed through from the constructor.
     * @param attrs The value from R.styleable.<code>&lt;declare-styleable&gt;</code>
     * @param attributeMaps What to do with each attribute.
     */
    public ColorThemeViewProxy(View view, @Nullable AttributeSet set, @StyleableRes int[] attrs,
            AttributeMap... attributeMaps) {
        actions = new Action[attributeMaps.length];

        TypedArray styledAttributes = view.getContext().obtainStyledAttributes(set, attrs);
        for (int i = 0; i < attributeMaps.length; i++) {
            actions[i] = new Action(
                    attributeMaps[i].action,
                    ColorThemeXml.getColorThemeXmlFromValue(
                            styledAttributes.getInt(
                                    attributeMaps[i].attr,
                                    attributeMaps[i].defaultColor.xmlValue)));
        }
        styledAttributes.recycle();
    }

    @Override
    public void onColorThemeChange(@NonNull ColorTheme colorTheme) {
        for (Action action : actions) {
            action.action.accept(action.colorThemeXml.colorMap.apply(colorTheme));
        }
    }
}
