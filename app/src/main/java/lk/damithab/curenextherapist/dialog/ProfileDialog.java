package lk.damithab.curenextherapist.dialog;

import static lk.damithab.curenextherapist.util.RegexUtil.isCharacterValid;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import lk.damithab.curenextherapist.R;
import lk.damithab.curenextherapist.databinding.DialogProfileDialogBinding;
import lk.damithab.curenextherapist.model.Therapist;
import lk.damithab.curenextherapist.model.User;
import lk.damithab.curenextherapist.module.GlideApp;

public class ProfileDialog extends DialogFragment {

    private DialogProfileDialogBinding binding;

    private FirebaseFirestore firebaseFirestore;

    private FirebaseAuth firebaseAuth;

    private FirebaseStorage storage;

    String firstName, lastName, bio, workEmail, workMobileNo, therapistId;

    Uri imageUri;

    public interface OnProfileUpdateListener {
        void onProfileUpdated(Uri newImageUri, String firstName, String lastName);
    }

    private OnProfileUpdateListener listener;

    private Spinner titleSpinner;

    public void setOnProfileUpdateListener(OnProfileUpdateListener listener) {
        this.listener = listener;
    }

    public ProfileDialog(String therapistId) {
        this.therapistId = therapistId;
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogProfileDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleSpinner = binding.editTherapistTitleSpinner;
        List<String> therapistTitleList = new ArrayList<>();
        therapistTitleList.add("Dr.");
        therapistTitleList.add("Mr.");
        therapistTitleList.add("Mrs.");
        therapistTitleList.add("Ms.");

        ArrayAdapter<String> titleAdapter = new ArrayAdapter<String>(requireActivity(), R.layout.spinner_item, therapistTitleList);
        titleAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        titleSpinner.setAdapter(titleAdapter);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            firebaseFirestore.collection("therapist").document(therapistId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot ds) {
                            if (ds.exists()) {
                                Therapist therapist = ds.toObject(Therapist.class);

                                titleSpinner.setSelection(titleAdapter.getPosition(therapist.getTitle()));

                                String[] fullName = therapist.getName().split(" ");

                                if (therapist.getTherapistImage().startsWith("https")) {

                                    Glide.with(binding.getRoot())
                                            .load(therapist.getTherapistImage())
                                            .centerCrop()
                                            .into(binding.mainImageView);
                                } else {
                                    storage.getReference(therapist.getTherapistImage())
                                            .getDownloadUrl()
                                            .addOnSuccessListener(uri -> {
                                                Glide.with(binding.getRoot())
                                                        .load(uri)
                                                        .centerCrop()
                                                        .into(binding.mainImageView);
                                            }).addOnFailureListener(error -> {

                                            });
                                }


                                binding.firstNameProfileInput.setText(fullName[0]);
                                binding.lastNameProfileInput.setText(fullName[1]);
                                binding.therapistWorkEmail.setText(therapist.getWorkEmail());
                                binding.therapistWorkMobile.setText(therapist.getWorkMobileNo());
                                binding.therapistBio.setText(therapist.getBio());
                            }

                        }
                    });
            binding.saveProfileBtn.setOnClickListener(v -> {
                firstName = binding.firstNameProfileInput.getText().toString().trim();
                lastName = binding.lastNameProfileInput.getText().toString().trim();
                bio = binding.therapistBio.getText().toString();
                workEmail = binding.therapistWorkEmail.getText().toString().trim();
                workMobileNo = binding.therapistWorkMobile.getText().toString().trim();

                if (firstName.isEmpty()) {
                    return;
                }

                if (lastName.isEmpty()) {
                    return;
                }
                if (bio.isEmpty()) {
                    return;
                }
                if (workEmail.isEmpty()) {
                    return;
                }
                if (workMobileNo.isEmpty()) {
                    return;
                }

                if (!isCharacterValid(firstName)) {
                    return;
                }

                if (!isCharacterValid(lastName)) {
                    return;
                }

                String name = firstName + " " + lastName;

                firebaseFirestore.collection("therapist").document(therapistId)
                        .update("name", name,
                                "title", titleSpinner.getSelectedItem().toString(),
                                "bio", bio,
                                "workEmail", workEmail,
                                "workMobileNo", workMobileNo)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                new ToastDialog(getActivity().getSupportFragmentManager(), "Profile updated successfully!");
                                if (listener != null) {
                                    listener.onProfileUpdated(imageUri, firstName, lastName);
                                }
                                dismiss();
                            }
                        });


            });

            binding.profileEditImage.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                activityResultLauncher.launch(intent);
            });

        }
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri uri = result.getData().getData();
                    Log.i("Image uri", uri.getPath());

                    Glide.with(requireActivity())
                            .load(uri)
                            .circleCrop()
                            .into(binding.mainImageView);

                    String imageId = firebaseAuth.getUid();

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference imageReference = storage.getReference("profile-images").child(imageId);
                    imageReference.putFile(uri)
                            .addOnSuccessListener(takeSnapshot -> {

                                firebaseFirestore.collection("users")
                                        .document(firebaseAuth.getUid())
                                        .update("profileUrl", "profile-images/" + imageId)
                                        .addOnSuccessListener(aVoid -> {
                                            imageUri = uri;
                                        });

                            });
                }
            }
    );

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.BOTTOM);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setDimAmount(0.7f);

                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                /// Adjust the dialogFragment width programmatically
//                int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.90);
//                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);

            }
            dialog.setCanceledOnTouchOutside(true);
        }
    }


}
