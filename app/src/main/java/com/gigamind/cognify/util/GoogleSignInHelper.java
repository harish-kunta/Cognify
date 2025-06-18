package com.gigamind.cognify.util;

import android.app.Activity;
import android.os.CancellationSignal;

import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.CustomCredential;
import androidx.credentials.PasswordCredential;
import androidx.credentials.PublicKeyCredential;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.UUID;

import com.gigamind.cognify.R;

public class GoogleSignInHelper {

    public interface Callback {
        void onSuccess(String idToken);
        void onError(Exception e);
    }

    public static void signIn(Activity activity, boolean filterByAuthorizedAccounts, Callback callback) {
        CredentialManager credentialManager = CredentialManager.create(activity);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .setServerClientId(activity.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .setNonce(UUID.randomUUID().toString())
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                activity.getMainExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleResult(result, callback);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        callback.onError(e);
                    }
                }
        );
    }

    public static void signIn(Activity activity, Callback callback) {
        signIn(activity, false, callback);
    }

    private static void handleResult(GetCredentialResponse result, Callback callback) {
        androidx.credentials.Credential credential = result.getCredential();
        if (credential instanceof CustomCredential) {
            CustomCredential cc = (CustomCredential) credential;
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(cc.getType())) {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(cc.getData());
                callback.onSuccess(googleIdTokenCredential.getIdToken());
            } else {
                callback.onError(new Exception("Unexpected credential type"));
            }
        } else if (credential instanceof PasswordCredential || credential instanceof PublicKeyCredential) {
            callback.onError(new Exception("Unexpected credential"));
        }
    }
}
