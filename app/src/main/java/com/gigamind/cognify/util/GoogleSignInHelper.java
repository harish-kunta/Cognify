package com.gigamind.cognify.util;

import android.app.Activity;
import android.os.CancellationSignal;

import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;
import androidx.credentials.CustomCredential;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.gigamind.cognify.R;

import java.util.UUID;

public class GoogleSignInHelper {

    public interface Callback {
        void onSuccess(String idToken);
        void onError(Exception e);
    }

    /** Public entry point: will try first with only authorized accounts, then retry against all. */
    public static void signIn(Activity activity, Callback callback) {
        signInInternal(activity, /* filterByAuthorizedAccounts= */ true, callback);
    }

    private static void signInInternal(Activity activity,
                                       boolean filterByAuthorizedAccounts,
                                       Callback callback) {
        CredentialManager mgr = CredentialManager.create(activity);

        GetGoogleIdOption opt = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .setServerClientId(activity.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .setNonce(UUID.randomUUID().toString())
                .build();

        GetCredentialRequest req = new GetCredentialRequest.Builder()
                .addCredentialOption(opt)
                .build();

        mgr.getCredentialAsync(
                activity,
                req,
                new CancellationSignal(),
                activity.getMainExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleResult(result, callback);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        // if our first attempt was only “authorized” accounts and it failed
                        // because there simply weren’t any, retry once with the full list
                        if (filterByAuthorizedAccounts && (e instanceof NoCredentialException)) {
                            signInInternal(activity, /* filterByAuthorizedAccounts= */ false, callback);
                        } else {
                            callback.onError(e);
                        }
                    }
                }
        );
    }

    private static void handleResult(GetCredentialResponse result, Callback callback) {
        androidx.credentials.Credential cred = result.getCredential();
        if (!(cred instanceof CustomCredential)) {
            callback.onError(new Exception("Unexpected credential type"));
            return;
        }
        CustomCredential cc = (CustomCredential) cred;
        if (!GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(cc.getType())) {
            callback.onError(new Exception("Unexpected credential subtype"));
            return;
        }
        GoogleIdTokenCredential gid = GoogleIdTokenCredential.createFrom(cc.getData());
        callback.onSuccess(gid.getIdToken());
    }
}
