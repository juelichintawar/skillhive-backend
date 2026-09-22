package com.example.skillhive;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.skillhive.model.Profile;
import com.example.skillhive.network.ProfileApi;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private EditText etProfileName;
    private EditText etProfilePhone;
    private EditText etProfileCollege;
    private EditText etProfileBio;

    private TextView tvProfileEmail;
    private TextView tvProfileRole;

    private ImageView ivProfilePhoto;
    private Button btnChoosePhoto;
    private Button btnUpdateProfile;

    private ProgressBar progressBarProfile;

    private ProfileApi profileApi;
    private SessionManager sessionManager;

    private ActivityResultLauncher<String> imagePickerLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);


        // =====================================================
        // FIND VIEWS
        // =====================================================

        etProfileName =
                findViewById(R.id.etProfileName);

        etProfilePhone =
                findViewById(R.id.etProfilePhone);

        etProfileCollege =
                findViewById(R.id.etProfileCollege);

        etProfileBio =
                findViewById(R.id.etProfileBio);

        tvProfileEmail =
                findViewById(R.id.tvProfileEmail);

        tvProfileRole =
                findViewById(R.id.tvProfileRole);

        ivProfilePhoto =
                findViewById(R.id.ivProfilePhoto);

        btnChoosePhoto =
                findViewById(R.id.btnChoosePhoto);

        btnUpdateProfile =
                findViewById(R.id.btnUpdateProfile);

        progressBarProfile =
                findViewById(R.id.progressBarProfile);


        // =====================================================
        // API
        // =====================================================

        profileApi =
                RetrofitClient
                        .getInstance()
                        .create(ProfileApi.class);


        // =====================================================
        // SESSION
        // =====================================================

        sessionManager =
                new SessionManager(this);


        // =====================================================
        // IMAGE PICKER
        // =====================================================

        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {

                            if (uri == null) {
                                return;
                            }

                            saveProfilePhoto(uri);
                        }
                );


        // =====================================================
        // LOAD SAVED PROFILE PHOTO
        // =====================================================

        loadSavedProfilePhoto();


        // =====================================================
        // CHOOSE PHOTO
        // =====================================================

        btnChoosePhoto.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );


        // =====================================================
        // LOAD PROFILE
        // =====================================================

        loadProfile();


        // =====================================================
        // UPDATE PROFILE
        // =====================================================

        btnUpdateProfile.setOnClickListener(
                v -> updateProfile()
        );
    }


    // =========================================================
    // SAVE PROFILE PHOTO
    // =========================================================

    private void saveProfilePhoto(Uri sourceUri) {

        try {

            /*
             * Open the selected image from the gallery.
             */
            InputStream inputStream =
                    getContentResolver()
                            .openInputStream(sourceUri);


            if (inputStream == null) {

                Toast.makeText(
                        this,
                        "Unable to open selected image",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            /*
             * Create a private file inside the app's storage.
             *
             * This file belongs to SkillHive and does not
             * depend on gallery permissions later.
             */
            File profilePhotoFile =
                    new File(
                            getFilesDir(),
                            "profile_photo.jpg"
                    );


            OutputStream outputStream =
                    new FileOutputStream(
                            profilePhotoFile
                    );


            byte[] buffer =
                    new byte[8192];

            int bytesRead;


            while ((bytesRead =
                    inputStream.read(buffer)) != -1) {

                outputStream.write(
                        buffer,
                        0,
                        bytesRead
                );
            }


            outputStream.flush();
            outputStream.close();
            inputStream.close();


            /*
             * Display the local file immediately.
             */
            ivProfilePhoto.setImageURI(
                    Uri.fromFile(profilePhotoFile)
            );


            /*
             * Save the local file path instead of
             * saving the gallery URI.
             */
            sessionManager.saveProfileImageUri(
                    profilePhotoFile.getAbsolutePath()
            );


            Toast.makeText(
                    this,
                    "Profile photo updated",
                    Toast.LENGTH_SHORT
            ).show();


        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Unable to save profile photo",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // =========================================================
    // LOAD SAVED PROFILE PHOTO
    // =========================================================

    private void loadSavedProfilePhoto() {

        String savedPath =
                sessionManager.getProfileImageUri();


        if (savedPath == null ||
                savedPath.trim().isEmpty()) {

            return;
        }


        try {

            File profilePhotoFile =
                    new File(savedPath);


            /*
             * Check whether the saved photo
             * still exists.
             */
            if (!profilePhotoFile.exists()) {

                sessionManager.saveProfileImageUri(null);

                return;
            }


            /*
             * Load the photo from SkillHive's
             * private storage.
             */
            ivProfilePhoto.setImageURI(
                    Uri.fromFile(profilePhotoFile)
            );


        } catch (Exception e) {

            e.printStackTrace();

            /*
             * If the saved image is invalid,
             * remove its path instead of crashing.
             */
            sessionManager.saveProfileImageUri(null);
        }
    }


    // =========================================================
    // LOAD PROFILE FROM BACKEND
    // =========================================================

    private void loadProfile() {

        String token =
                sessionManager.getToken();


        if (token == null ||
                token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        progressBarProfile.setVisibility(
                View.VISIBLE
        );


        profileApi.getProfile(
                "Bearer " + token
        ).enqueue(new Callback<Profile>() {

            @Override
            public void onResponse(
                    Call<Profile> call,
                    Response<Profile> response) {

                progressBarProfile.setVisibility(
                        View.GONE
                );


                if (response.isSuccessful()
                        && response.body() != null) {

                    Profile profile =
                            response.body();


                    etProfileName.setText(
                            profile.getFullName()
                    );


                    tvProfileEmail.setText(
                            profile.getEmail()
                    );


                    etProfilePhone.setText(
                            profile.getPhone()
                    );


                    etProfileCollege.setText(
                            profile.getCollege()
                    );


                    etProfileBio.setText(
                            profile.getBio()
                    );


                    tvProfileRole.setText(
                            profile.getRole()
                    );


                } else {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Failed to load profile: "
                                    + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }


            @Override
            public void onFailure(
                    Call<Profile> call,
                    Throwable t) {

                progressBarProfile.setVisibility(
                        View.GONE
                );


                Toast.makeText(
                        ProfileActivity.this,
                        "Connection failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }


    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    private void updateProfile() {

        String fullName =
                etProfileName
                        .getText()
                        .toString()
                        .trim();


        String phone =
                etProfilePhone
                        .getText()
                        .toString()
                        .trim();


        String college =
                etProfileCollege
                        .getText()
                        .toString()
                        .trim();


        String bio =
                etProfileBio
                        .getText()
                        .toString()
                        .trim();


        // =====================================================
        // VALIDATION
        // =====================================================

        if (fullName.isEmpty()) {

            etProfileName.setError(
                    "Enter your full name"
            );

            etProfileName.requestFocus();

            return;
        }


        if (phone.isEmpty()) {

            etProfilePhone.setError(
                    "Enter your phone number"
            );

            etProfilePhone.requestFocus();

            return;
        }


        if (college.isEmpty()) {

            etProfileCollege.setError(
                    "Enter your college"
            );

            etProfileCollege.requestFocus();

            return;
        }


        String token =
                sessionManager.getToken();


        if (token == null ||
                token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // CREATE PROFILE REQUEST
        // =====================================================

        Profile profile =
                new Profile();

        profile.setFullName(
                fullName
        );

        profile.setPhone(
                phone
        );

        profile.setCollege(
                college
        );

        profile.setBio(
                bio
        );


        // =====================================================
        // LOADING
        // =====================================================

        btnUpdateProfile.setEnabled(
                false
        );

        btnUpdateProfile.setText(
                "Updating..."
        );

        progressBarProfile.setVisibility(
                View.VISIBLE
        );


        // =====================================================
        // API CALL
        // =====================================================

        profileApi.updateProfile(
                profile,
                "Bearer " + token
        ).enqueue(new Callback<Profile>() {

            @Override
            public void onResponse(
                    Call<Profile> call,
                    Response<Profile> response) {

                progressBarProfile.setVisibility(
                        View.GONE
                );

                btnUpdateProfile.setEnabled(
                        true
                );

                btnUpdateProfile.setText(
                        "Update Profile"
                );


                if (response.isSuccessful()
                        && response.body() != null) {

                    Profile updatedProfile =
                            response.body();


                    // =================================================
                    // UPDATE UI
                    // =================================================

                    etProfileName.setText(
                            updatedProfile.getFullName()
                    );

                    tvProfileEmail.setText(
                            updatedProfile.getEmail()
                    );

                    etProfilePhone.setText(
                            updatedProfile.getPhone()
                    );

                    etProfileCollege.setText(
                            updatedProfile.getCollege()
                    );

                    etProfileBio.setText(
                            updatedProfile.getBio()
                    );

                    tvProfileRole.setText(
                            updatedProfile.getRole()
                    );


                    // =================================================
                    // UPDATE SESSION
                    // =================================================

                    sessionManager.saveSession(
                            sessionManager.getToken(),
                            updatedProfile.getId(),
                            updatedProfile.getFullName(),
                            updatedProfile.getEmail(),
                            updatedProfile.getRole()
                    );


                    Toast.makeText(
                            ProfileActivity.this,
                            "Profile updated successfully!",
                            Toast.LENGTH_LONG
                    ).show();


                } else {

                    String message =
                            "Failed to update profile: "
                                    + response.code();


                    try {

                        if (response.errorBody()
                                != null) {

                            message =
                                    response.errorBody()
                                            .string();
                        }

                    } catch (Exception e) {

                        e.printStackTrace();
                    }


                    Toast.makeText(
                            ProfileActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }


            @Override
            public void onFailure(
                    Call<Profile> call,
                    Throwable t) {

                progressBarProfile.setVisibility(
                        View.GONE
                );

                btnUpdateProfile.setEnabled(
                        true
                );

                btnUpdateProfile.setText(
                        "Update Profile"
                );


                Toast.makeText(
                        ProfileActivity.this,
                        "Connection failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}