package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class CustomMaterialButtonToggleGroup extends MaterialButtonToggleGroup {
    public CustomMaterialButtonToggleGroup(@NonNull Context context) {
        super(context);
    }

    public CustomMaterialButtonToggleGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomMaterialButtonToggleGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if(child instanceof MaterialButton)
        {
            ((MaterialButton) child).setMaxLines(2);
        }
    }}