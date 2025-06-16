package com.gigamind.cognify.ui;

import android.app.Activity;
import android.graphics.RectF;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.gigamind.cognify.R;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.gigamind.cognify.ui.HighlightScrimView;

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
    private HighlightScrimView scrim;
    private Step currentStep;

    public TutorialOverlay(Activity activity) {
        this.activity = activity;
    }

    private static class Step {
        final View anchor;
        final String text;
        final float originalElevation;

        Step(View anchor, String text) {
            this.anchor = anchor;
            this.text = text;
            this.originalElevation = anchor.getElevation();
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
        clearHighlight();
        currentStep = step;

        ViewGroup root = activity.findViewById(android.R.id.content);
        if (scrim == null) {
            scrim = new HighlightScrimView(activity);
        }
        if (scrim.getParent() == null) {
            root.addView(scrim, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        scrim.setElevation(1f);
        // Use elevation to show the highlight above the scrim without altering
        // the child's position within its parent layout. Calling bringToFront()
        // caused layout reordering which shifted views in LinearLayouts.
        step.anchor.setElevation(step.originalElevation + 10f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            step.anchor.setForeground(ContextCompat.getDrawable(activity, R.drawable.tutorial_highlight));
        }
        View view = LayoutInflater.from(activity).inflate(R.layout.tutorial_popup, null);
        TextView text = view.findViewById(R.id.tutorialText);
        MaterialButton next = view.findViewById(R.id.tutorialNextButton);
        MaterialButton skip = view.findViewById(R.id.tutorialSkipButton);
        text.setText(step.text);
        next.setText(index == steps.size() - 1 ? activity.getString(R.string.tutorial_got_it)
                : activity.getString(R.string.tutorial_next));
        popup = new PopupWindow(view,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                false);
        popup.setOutsideTouchable(false);
        int[] loc = new int[2];
        step.anchor.getLocationInWindow(loc);
        int[] rootLoc = new int[2];
        root.getLocationInWindow(rootLoc);

        RectF hole = new RectF(
                loc[0] - rootLoc[0],
                loc[1] - rootLoc[1],
                loc[0] - rootLoc[0] + step.anchor.getWidth(),
                loc[1] - rootLoc[1] + step.anchor.getHeight());
        scrim.setHole(hole);
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
            } else {
                finishTutorial();
            }
        });

        skip.setOnClickListener(v -> {
            popup.dismiss();
            index = steps.size();
            finishTutorial();
        });
    }

    private void finishTutorial() {
        clearHighlight();
        if (scrim != null && scrim.getParent() != null) {
            ((ViewGroup) scrim.getParent()).removeView(scrim);
        }
        if (onComplete != null) {
            onComplete.run();
        }
    }

    private void clearHighlight() {
        if (currentStep != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                currentStep.anchor.setForeground(null);
            }
            currentStep.anchor.setElevation(currentStep.originalElevation);
        }
        if (scrim != null) {
            scrim.setHole(null);
        }
    }
}

