package com.gigamind.cognify.util;

/**
 * Immutable model representing a single page of the onboarding flow.
 * <p>
 * Instead of exposing mutable fields, we utilise the Builder pattern so that
 * callers can construct the object in a readable manner while keeping the
 * instance itself immutable once created.  This follows the "Design for
 * extension, or for modification" guideline from <em>Code Complete</em> and
 * helps avoid accidental changes to state.
 */
public final class OnboardingItem {
    private final int imageResId;
    private final String title;
    private final String description;

    private OnboardingItem(Builder builder) {
        this.imageResId = builder.imageResId;
        this.title = builder.title;
        this.description = builder.description;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    /** Builder for {@link OnboardingItem} */
    public static class Builder {
        private int imageResId;
        private String title;
        private String description;

        public Builder imageResId(int resId) {
            this.imageResId = resId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public OnboardingItem build() {
            return new OnboardingItem(this);
        }
    }
}
