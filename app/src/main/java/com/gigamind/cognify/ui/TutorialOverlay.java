package com.gigamind.cognify.ui;

import android.app.Activity;
import android.graphics.Point;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.gigamind.cognify.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple sequential popup tutorial overlay.
 */
public class TutorialOverlay {
    private final Activity activity;
    private final List<Step> steps = new ArrayList<>();
    private int index = 0;
    private PopupWindow popup;
    private Runnable onComplete;

    public TutorialOverlay(Activity activity) {
        this.activity = activity;
    }

    private static class Step {
        final View anchor;
        final String text;
        Step(View anchor, String text) {
            this.anchor = anchor;
            this.text = text;
        }
    }

    public void addStep(View anchor, String text) {
        steps.add(new Step(anchor, text));
    }

    public void setOnComplete(Runnable r) {
        onComplete = r;
    }

    public void start() {
        if (steps.isEmpty()) return;
        index = 0;
        showStep();
    }

    private void showStep() {
        Step step = steps.get(index);
        View view = LayoutInflater.from(activity).inflate(R.layout.tutorial_popup, null);
        TextView text = view.findViewById(R.id.tutorialText);
        MaterialButton next = view.findViewById(R.id.tutorialNextButton);
        text.setText(step.text);
        next.setText(index == steps.size() - 1 ? activity.getString(R.string.tutorial_got_it)
                : activity.getString(R.string.tutorial_next));
        popup = new PopupWindow(view,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popup.setOutsideTouchable(false);
        popup.setFocusable(true);
        int[] loc = new int[2];
        step.anchor.getLocationInWindow(loc);
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = view.getMeasuredHeight();
        int y = loc[1] - popupHeight;
        if (y < 0) y = loc[1] + step.anchor.getHeight();
        popup.showAtLocation(step.anchor, Gravity.NO_GRAVITY, loc[0], y);

        next.setOnClickListener(v -> {
            popup.dismiss();
            index++;
            if (index < steps.size()) {
                showStep();
            } else if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}
