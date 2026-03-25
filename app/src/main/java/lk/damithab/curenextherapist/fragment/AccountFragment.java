package lk.damithab.curenextherapist.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import lk.damithab.curenextherapist.activity.MainActivity;
import lk.damithab.curenextherapist.activity.SettingsActivity;
import lk.damithab.curenextherapist.activity.SignInActivity;
import lk.damithab.curenextherapist.databinding.FragmentAccountBinding;
import lk.damithab.curenextherapist.dialog.ProfileDialog;
import lk.damithab.curenextherapist.model.Therapist;
import lk.damithab.curenextherapist.model.User;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    FirebaseAuth auth;

    FirebaseFirestore db;

    private FirebaseStorage storage;

    private FirebaseAuth firebaseAuth;

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 2;

    private static final String PREFERENCE_NAME = "therapist";

    private String therapistId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        startDataLoading(true);

        SharedPreferences preferences = getActivity().getSharedPreferences("therapist", MODE_PRIVATE);
        therapistId = preferences.getString("therapistId", null);

//        binding.accountEditProfileBtn.bringToFront();
        binding.accountEditProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("FragmentAccount", "onViewCreated: Edit Button");
                ProfileDialog dialog = new ProfileDialog(therapistId);
                dialog.setOnProfileUpdateListener((newImageUri, firstName, lastName) -> {
                    binding.accountUserName.setText(firstName + " " + lastName);
                    if (newImageUri != null) {
                        Glide.with(binding.getRoot())
                                .load(newImageUri)
                                .circleCrop()
                                .into(binding.accountUserImage);
                    }
                });
                dialog.show(getParentFragmentManager(), "ProfileDialog");
            }
        });

        if (firebaseAuth.getCurrentUser() != null) {
            binding.accountBtnSignOut.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), SignInActivity.class);
                startActivity(intent);

                firebaseAuth.signOut();
                ((MainActivity) getActivity()).loadFragment(new HomeFragment());

            });
        }

        binding.accountSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

    }

    private void checkAllTasksFinished() {
        completedTasks++;
        Log.d("HomeFragment", "checkAllTasksFinished: " + completedTasks);
        if (completedTasks >= TOTAL_TASKS) {
            onDataLoad(false);
            completedTasks = 0; // Reset for swipe-to-refresh
        }
    }

    private void startDataLoading(boolean isShimmer) {
        onDataLoad(isShimmer);
        loadData();
    }

    private synchronized void onDataLoad(boolean isShimmer) {
        if (isShimmer) {
            binding.shimmerListingViewContainer.startShimmer();
            binding.shimmerListingViewContainer.setVisibility(View.VISIBLE);
            binding.accountMain.setVisibility(View.GONE);
        } else {
            binding.shimmerListingViewContainer.stopShimmer();
            binding.shimmerListingViewContainer.setVisibility(View.GONE);
            binding.accountMain.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
        db.collection("therapist").whereEqualTo("uid", auth.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        checkAllTasksFinished();
                        if (!isAdded()) return;
                        if (!qds.isEmpty()) {
                            Therapist therapist = qds.toObjects(Therapist.class).get(0);
                            binding.accountUserName.setText(therapist.getName());

                            if (therapist.getTherapistImage().startsWith("https")) {
                                checkAllTasksFinished();
                                Glide.with(binding.getRoot())
                                        .load(therapist.getTherapistImage())
                                        .centerCrop()
                                        .into(binding.accountUserImage);
                            } else {
                                storage.getReference(therapist.getTherapistImage())
                                        .getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            checkAllTasksFinished();
                                            Glide.with(binding.getRoot())
                                                    .load(uri)
                                                    .centerCrop()
                                                    .into(binding.accountUserImage);
                                        }).addOnFailureListener(error -> {
                                            checkAllTasksFinished();
                                        });
                            }

                        }


                    }

                }).addOnFailureListener(aVoid -> {
                    checkAllTasksFinished();
                });
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}