package com.openclassrooms.go4lunch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    //FOR DESIGN
    @BindView(R.id.profile_activity_imageview_profile)
    ImageView imageViewProfile;
    @BindView(R.id.profile_activity_text_view_username)
    TextView textViewUsername;
    @BindView(R.id.profile_activity_text_view_email)
    TextView textViewEmail;
    @BindView(R.id.profile_activity_progress_bar)
    ProgressBar progressBar;

    //FOR DATA
    private static final int RC_SIGN_IN = 123;
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    // --------------------
    // LIFE CYCLE
    // --------------------

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        ButterKnife.bind(this); //Configure Butterknife

        auth = FirebaseAuth.getInstance();

        if(!isCurrentUserLogged()){
            startSignInActivity();
        }
        else{
            firebaseUser = auth.getCurrentUser();
            this.configureToolbar();
            this.updateUIWhenCreating();
        }
    }

    // --------------------
    // ACTIONS
    // --------------------

    @OnClick(R.id.profile_activity_button_sign_out)
    public void onClickSignOutButton() { this.signOutUserFromFirebase(); }

    @OnClick(R.id.profile_activity_button_delete)
    public void onClickDeleteButton() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.popup_message_confirmation_delete_account)
                .setPositiveButton(R.string.popup_message_choice_yes, (dialogInterface, i) -> deleteUserFromFirebase())
                .setNegativeButton(R.string.popup_message_choice_no, null)
                .show();
    }

    // --------------------
    // REST REQUESTS
    // --------------------

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private void deleteUserFromFirebase(){
        if (this.getCurrentUser() != null) {

            this.firebaseUser.delete();

            AuthUI.getInstance()
                    .delete(this)
                    .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));
        }
    }

    // --------------------
    // UI
    // --------------------

    private void configureToolbar(){
        ActionBar ab = getSupportActionBar();
        Objects.requireNonNull(ab).hide();
    }

    private void updateUIWhenCreating(){
        firebaseUser = auth.getCurrentUser();

        if (this.getCurrentUser() != null){

            Glide.with(this)
                        .load(this.firebaseUser.getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageViewProfile);

            //Update views with data
            this.textViewEmail.setText(this.firebaseUser.getEmail());
            this.textViewUsername.setText(this.firebaseUser.getDisplayName());
        }
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return aVoid -> {
            switch (origin){
                case RC_SIGN_IN:
                    this.configureToolbar();
                    this.updateUIWhenCreating();
                    break;
                case SIGN_OUT_TASK:
                    startSignInActivity();
                    break;
                case DELETE_USER_TASK:
                    startSignInActivity();
                    break;
                default:
                    break;
            }
        };
    }

    // --------------------
    // ON RESULT HANDLER
    // --------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_CANCELED :
                startSignInActivity();
                Toast.makeText(getApplicationContext(), R.string.connect_first, Toast.LENGTH_LONG).show();
                break;
            case RESULT_OK:
                this.configureToolbar();
                this.updateUIWhenCreating();
                break;
            default:
                startSignInActivity();
                break;
        }
    }

    // --------------------
    // SIGN-IN ACTIVITY
    // --------------------

    private void startSignInActivity(){
        startActivityForResult(AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setTheme(R.style.LoginTheme)
                                .setAvailableProviders(
                                        Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(), //EMAIL
                                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build())) // SUPPORT GOOGLE))
                                .setIsSmartLockEnabled(false, true)
                                .setLogo(R.drawable.ic_logo)
                                .build(),
                        RC_SIGN_IN);
    }

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    private FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    private Boolean isCurrentUserLogged(){ return (this.getCurrentUser() != null); }
}
