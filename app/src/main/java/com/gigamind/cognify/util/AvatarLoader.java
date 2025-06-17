package com.gigamind.cognify.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.gigamind.cognify.data.repository.UserRepository;

/**
 * Utility class to load a user avatar from the {@link UserRepository} into an
 * {@link ImageView}. This centralizes the logic of decoding base64 images or
 * loading remote URLs so fragments do not duplicate the same code.
 */
public final class AvatarLoader {
    private AvatarLoader() { }

    /**
     * Loads the profile picture stored in the repository into the provided view.
     * If no avatar is stored, the view is left unchanged.
     */
    public static void load(UserRepository repository, ImageView target) {
        if (repository == null || target == null) return;
        String stored = repository.getProfilePicture();
        if (stored == null || stored.isEmpty()) return;

        if (stored.startsWith("http")) {
            Glide.with(target.getContext()).load(stored).into(target);
        } else {
            byte[] bytes = Base64.decode(stored, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            target.setImageBitmap(bmp);
        }
    }
}

