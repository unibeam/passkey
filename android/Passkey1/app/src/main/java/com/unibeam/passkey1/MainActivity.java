package com.unibeam.passkey1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CreateCredentialResponse;
import androidx.credentials.CreatePublicKeyCredentialRequest;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.GetPasswordOption;
import androidx.credentials.GetPublicKeyCredentialOption;
import androidx.credentials.PasswordCredential;
import androidx.credentials.PublicKeyCredential;
import androidx.credentials.exceptions.CreateCredentialCancellationException;
import androidx.credentials.exceptions.CreateCredentialException;
import androidx.credentials.exceptions.CreateCredentialInterruptedException;
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException;
import androidx.credentials.exceptions.CreateCredentialUnknownException;
import androidx.credentials.exceptions.CreateCustomCredentialException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException;

import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Base64;
import android.util.Log;

import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "UNIBEAM";

    private CredentialManager credentialManager;

    private final String username = "ariel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        credentialManager = CredentialManager.create(this);

//        signIn();
        signUp();
    }

    private void signUp() {
        CreatePublicKeyCredentialRequest createPublicKeyCredentialRequest = new CreatePublicKeyCredentialRequest(fetchRegistrationJsonFromServer());

        credentialManager.createCredentialAsync(
                createPublicKeyCredentialRequest,
                this,
                new CancellationSignal(),
                getMainExecutor(),
                new CredentialManagerCallback<CreateCredentialResponse, CreateCredentialException>() {
                    @Override
                    public void onResult(CreateCredentialResponse result) {
                        Log.d(TAG, "Registration success");
                    }

                    @Override
                    public void onError(CreateCredentialException e) {
                        Log.e(TAG, "Registration failure - " + e.getClass().getName());

                        if (e instanceof CreatePublicKeyCredentialDomException) {
                            // Handle the passkey DOM errors thrown according to the
                            // WebAuthn spec.
                            Log.e(TAG, String.valueOf(((CreatePublicKeyCredentialDomException)e).getDomError()));
                        } else if (e instanceof CreateCredentialCancellationException) {
                            // The user intentionally canceled the operation and chose not
                            // to register the credential.
                        } else if (e instanceof CreateCredentialInterruptedException) {
                            // Retry-able error. Consider retrying the call.
                        } else if (e instanceof CreateCredentialProviderConfigurationException) {
                            // Your app is missing the provider configuration dependency.
                            // Most likely, you're missing the
                            // "credentials-play-services-auth" module.
                        } else if (e instanceof CreateCredentialUnknownException) {
                        } else if (e instanceof CreateCustomCredentialException) {
                            // You have encountered an error from a 3rd-party SDK. If
                            // you make the API call with a request object that's a
                            // subclass of
                            // CreateCustomCredentialRequest using a 3rd-party SDK,
                            // then you should check for any custom exception type
                            // constants within that SDK to match with e.type.
                            // Otherwise, drop or log the exception.
                        }
                    }
                }
        );
    }

    private void signIn() {
        GetPublicKeyCredentialOption getPublicKeyCredentialOption = new GetPublicKeyCredentialOption(fetchAuthJsonFromServer(), null, true);
        GetPasswordOption getPasswordOption = new GetPasswordOption();

        GetCredentialRequest getCredRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(getPasswordOption)
                .addCredentialOption(getPublicKeyCredentialOption)
                .build();

        credentialManager.getCredentialAsync(
                getCredRequest,
                this,
                new CancellationSignal(),
                getMainExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();
                        if (credential instanceof PublicKeyCredential) {
                            String responseJson = ((PublicKeyCredential) credential).getAuthenticationResponseJson();
                            Log.d(TAG, "Passkey: " + responseJson);
                        } else if (credential instanceof PasswordCredential) {
                            String id = ((PasswordCredential) credential).getId();
                            String password = ((PasswordCredential) credential).getPassword();
                            Log.d(TAG, "Got Password - User:" + id + " Password: " + password);
                        } else {
                            Log.e(TAG, "Unexpected type of credential: " + credential.getClass().getName());
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "Sign in failed with exception", e);
                    }
                }
        );
    }

    private String fetchRegistrationJsonFromServer() {
        String response = Utils.readFromAssets(this, "reg.txt");

        return response.replace("<userId>", getEncodedUserId())
                .replace("<userName>", username)
                .replace("<userDisplayName>", username)
                .replace("<challenge>", getEncodedChallenge());
    }

    private String fetchAuthJsonFromServer() {
        return Utils.readFromAssets(this, "auth.txt");
    }

    private String getEncodedUserId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);

        return Base64.encodeToString(bytes, Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING);
    }

    private String getEncodedChallenge() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        return Base64.encodeToString(bytes, Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING);
    }
}