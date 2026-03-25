package lk.damithab.curenextherapist.activity;

import static lk.damithab.curenextherapist.util.RegexUtil.isEmailValid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

import lk.damithab.curenextherapist.R;
import lk.damithab.curenextherapist.databinding.ActivitySignInBinding;
import lk.damithab.curenextherapist.dialog.SpinnerDialog;
import lk.damithab.curenextherapist.dialog.ToastDialog;
import lk.damithab.curenextherapist.model.Therapist;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private FirebaseAuth firebaseAuth;

    private FirebaseFirestore db;

    private static final String PREFERENCE_NAME = "therapist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadListeners();

        if(firebaseAuth.getCurrentUser() != null){
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding.signinBtn.setOnClickListener(v->{
            String email = binding.signInEmail.getText().toString();
            String password = binding.signInPassword.getText().toString();
            login(email, password);
        });
    }

    public void login(String email, String password) {

        Log.d("SignInActivity", "login: email" +email);
        if (email.isEmpty()) {
            binding.signInEmailLayout.setErrorEnabled(true);
            binding.signInEmailLayout.setError("Email address is required.");
            binding.signInEmail.requestFocus();
            return;
        }
        if (!isEmailValid(email)) {
            binding.signInEmailLayout.setErrorEnabled(true);
            binding.signInEmailLayout.setError("Invalid email format. Please use the format: name@example.com.");
            binding.signInEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.signInPasswordLayout.setErrorEnabled(true);
            binding.signInPasswordLayout.setError("Password is required.");
            binding.signInPassword.requestFocus();
            return;
        }

        SpinnerDialog spinner = SpinnerDialog.show(getSupportFragmentManager());

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if(authResult.getUser() != null){
                        String uid = authResult.getUser().getUid();
                        db.collection("therapist").whereEqualTo("uid", uid )
                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot qds) {
                                        spinner.dismiss();
                                        if(!qds.isEmpty()){

                                            Therapist therapist = qds.toObjects(Therapist.class).get(0);
                                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                            intent.putExtra("therapistId", therapist.getTherapistId());

                                            SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
                                            preferences.edit().putString("therapistId", therapist.getTherapistId()).apply();

                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();

                                        }else{
                                            firebaseAuth.signOut();
                                            new ToastDialog(getSupportFragmentManager(), "Invalid Credentials. Please try again!");
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        spinner.dismiss();
                                        firebaseAuth.signOut();
                                        new ToastDialog(getSupportFragmentManager(), "Invalid Credentials. Please try again!");
                                    }
                                });


                    }else{
                        new ToastDialog(getSupportFragmentManager(), "No Auth found. Please try again!");

                    }


                })
                .addOnFailureListener(e -> {
                    spinner.dismiss();
                    Log.e("AuthError", e.getMessage());
                    new ToastDialog(getSupportFragmentManager(), "System error. Please try again later!");
                });
    }

    private void loadListeners(){
        binding.signInEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.signInEmailLayout.setErrorEnabled(false);
            }
        });

        binding.signInPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.signInPasswordLayout.setErrorEnabled(false);
            }
        });
    }

    private void updateUI(FirebaseUser user){
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}