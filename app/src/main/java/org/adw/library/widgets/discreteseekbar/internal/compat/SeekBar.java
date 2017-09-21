/*
 * Copyright (c) Gustavo Claramunt (AnderWeb) 2014.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.adw.library.widgets.discreteseekbar.internal.compat;

import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.widget.TextView;

import org.adw.library.widgets.discreteseekbar.internal.drawable.MarkerDrawable;

/**
 * @hide
 */
public class SeekBar {

    /**
     * Sets the custom Outline provider.
     *
     * @param view
     * @param markerDrawable
     */
    public static void setOutlineProvider(View view, final MarkerDrawable markerDrawable) {
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setConvexPath(markerDrawable.getPath());
            }
        });
    }

    public static Drawable getRipple(ColorStateList colorStateList) {
        return new RippleDrawable(colorStateList, null, null);
    }

    /**
     * Sets the color of the seekbar ripple
     * @param drawable
     * @param colorStateList The ColorStateList the track ripple will be changed to
     */
    public static void setRippleColor(@NonNull Drawable drawable, ColorStateList colorStateList) {
        ((RippleDrawable) drawable).setColor(colorStateList);
    }

    public static void setHotspotBounds(Drawable drawable, int left, int top, int right, int bottom) {
        //We don't want the full size rect, Lollipop ripple would be too big
        int size = (right - left) / 8;
        drawable.setHotspotBounds(left + size, top + size, right - size, bottom - size);
    }

    /**
     * android.support.v4.view.ViewCompat SHOULD include this once and for all!!
     * But it doesn't...
     *
     * @param view
     * @param background
     */
    @SuppressWarnings("deprecation")
    public static void setBackground(View view, Drawable background) {
        view.setBackground(background);
    }

    /**
     * Sets the TextView text direction attribute when possible
     *
     * @param textView
     * @param textDirection
     * @see android.widget.TextView#setTextDirection(int)
     */
    public static void setTextDirection(TextView textView, int textDirection) {
        textView.setTextDirection(textDirection);
    }

    public static boolean isInScrollingContainer(ViewParent p) {
        while (p instanceof ViewGroup) {
            if (((ViewGroup) p).shouldDelayChildPressedState()) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    public static boolean isHardwareAccelerated(View view) {
        return view.isHardwareAccelerated();
    }
}
