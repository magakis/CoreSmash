package com.archapp.coresmash;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.archapp.coresmash.platform.GoogleGames;
import com.archapp.coresmash.platform.PlayerInfo;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class GoogleGamesAndroid implements GoogleGames {
    private List<PropertyChangeListener> changeListeners;
    private PlayerInfo playerInfo;

    private GoogleSignInAccount googleSignInAccount;
    private GoogleSignInClient signInClient;
    private Activity activity;

    public GoogleGamesAndroid(final Activity activity) {
        this.activity = activity;
        changeListeners = new ArrayList<>();

        signInClient = GoogleSignIn.getClient(activity,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                        .requestScopes(
                                RequiredScopes.PROFILE,
                                RequiredScopes.GAMES
                        )
                        .build());

        playerInfo = new PlayerInfo();

        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (googleSignInAccount != null) {
            ensureAllScopesAreGranted();
            updateAccountInfo();
        }
    }

    @Override
    public boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(activity) != null;
    }

    @Override
    public void signIn() {
        if (!isSignedIn()) {
            googleSignInSilently();
            if (!isSignedIn())
                googleStartSignInIntent();
        }
    }

    @Override
    public PlayerInfo getAccountInfo() {
        return playerInfo;
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        if (!changeListeners.contains(listener))
            changeListeners.add(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        changeListeners.remove(listener);
    }

    @Override
    public void signOut() {
        if (isSignedIn())
            signInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                        notifyListeners("Signed Out", null);
                    else
                        new AlertDialog.Builder(activity)
                                .setMessage("Hmm.. Something didn't work out")
                                .setNeutralButton(android.R.string.ok, null)
                                .show();
                }
            });

    }

    private void notifyListeners(String name, Object newValue) {
        for (PropertyChangeListener listener : changeListeners)
            listener.onChange(name, newValue);
    }

    public void onActivityResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

        try {
            googleSignInAccount = task.getResult(ApiException.class);
            updateAccountInfo();
        } catch (ApiException apiException) {
            String message = apiException.getMessage();
            if (message == null || message.isEmpty()) {
                message = "Failed Google Play login";
            }
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        }
    }

    private void updateAccountInfo() {
        Games.getPlayersClient(activity, googleSignInAccount).getCurrentPlayer().addOnCompleteListener(
                new OnCompleteListener<Player>() {
                    @Override
                    public void onComplete(@NonNull Task<Player> task) {
                        try {
                            Player player = task.getResult(ApiException.class);
                            playerInfo.displayName = player.getDisplayName();
                            playerInfo.id = player.getPlayerId();

                            ImageManager imgManager = ImageManager.create(activity);
                            imgManager.loadImage(new ImageManager.OnImageLoadedListener() {
                                @Override
                                public void onImageLoaded(Uri uri, Drawable drawable, boolean b) {
                                    final Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

                                    Gdx.app.postRunnable(new Runnable() {
                                        @Override
                                        public void run() {
                                            Texture tex = new Texture(
                                                    bitmap.getWidth(), bitmap.getHeight(),
                                                    Pixmap.Format.RGBA8888);
                                            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex.getTextureObjectHandle());
                                            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                                            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                                            bitmap.recycle();

                                            playerInfo.avatar = tex;
                                            notifyListeners("UpdatedAccountInfo", null);
                                        }
                                    });
                                }
                            }, player.getHiResImageUri());
                        } catch (ApiException ex) {
                            Log.e("SHIIIIIIIT", "Couldn't get Player Object");
                        }
                    }
                });
    }

    private void googleStartSignInIntent() {
        activity.startActivityForResult(signInClient.getSignInIntent(),
                AndroidLauncher.RequestCode.GOOGLE_SIGN_IN);
    }

    private void googleSignInSilently() {
        signInClient.silentSignIn().addOnCompleteListener(activity, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                if (task.isSuccessful()) {
                    googleSignInAccount = task.getResult();
                    updateAccountInfo();
                }
            }
        });
    }

    private void ensureAllScopesAreGranted() {
        if (!GoogleSignIn.hasPermissions(googleSignInAccount, RequiredScopes.PROFILE)) {
            GoogleSignIn.requestPermissions(activity, 0, googleSignInAccount, RequiredScopes.PROFILE);
        }
        if (!GoogleSignIn.hasPermissions(googleSignInAccount, RequiredScopes.GAMES)) {
            GoogleSignIn.requestPermissions(activity, 0, googleSignInAccount, RequiredScopes.GAMES);
        }
    }

    private static class RequiredScopes {
        private static final Scope PROFILE = new Scope(Scopes.PROFILE);
        private static final Scope GAMES = new Scope(Scopes.GAMES);
//        private static final Scope PLUS_ME = new Scope(Scopes.PLUS_ME);
//        private static final Scope PLUS_LOGIN = new Scope(Scopes.LEGACY_USERINFO_PROFILE);
    }

}
