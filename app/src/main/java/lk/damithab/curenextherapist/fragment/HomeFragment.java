package lk.damithab.curenextherapist.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import lk.damithab.curenextherapist.R;
import lk.damithab.curenextherapist.adapter.HomeScheduleAdapter;
import lk.damithab.curenextherapist.databinding.FragmentHomeBinding;
import lk.damithab.curenextherapist.model.Booking;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private String fromDate;
    private String toDate;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private FirebaseAuth auth;


    private String therapistId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        therapistId = auth.getUid();

        DatePickerDialog dateDialog = new DatePickerDialog(getContext());
        dateDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        binding.homeServicesRecycle.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));


        binding.selectDateFromBtn.setOnClickListener(v->{
            dateDialog.show();
            dateDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    fromDate = String.format("%04d-%02d-%02d", i , i1, i2);
                    binding.selectScheduleFromInput.setText(fromDate);
                    Log.d("HomeF", fromDate);
                }
            });
        });

        binding.selectDateToBtn.setOnClickListener(v->{
            dateDialog.show();
            dateDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    toDate = String.format("%04d-%02d-%02d", i , i1, i2);
                    binding.selectScheduleToInput.setText(fromDate);
                    Log.d("HomeF", fromDate);
                }
            });
        });

        binding.fragmentHomeSearchSchedule.setOnClickListener(v->{
            if(fromDate == null){
                return;
            }

            if(toDate == null){
                /// search query with from date
            db.collection("therapist").document(therapistId).collection("schedule")
                    .whereGreaterThan("startTime", fromDate)
                    .whereLessThan("endTime", toDate)
                    .orderBy("startTime")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot qsd) {
                            if(!qsd.isEmpty()){
                                List<Booking> bookingList = qsd.toObjects(Booking.class);
                                HomeScheduleAdapter adapter = new HomeScheduleAdapter(bookingList, booking -> {
                                    db.collection("bookings").document(booking.getDocId())
                                            .update("status", "Cancelled")
                                            .addOnSuccessListener( aVoid->{
                                                Toast.makeText(getContext(), "booking canceled!", Toast.LENGTH_SHORT).show();
                                            });
                                });
                                binding.homeServicesRecycle.setAdapter(adapter);
                            }
                        }
                    });

            }else{
                /// search query with both dates
            }
        });

        db.collection("bookings").whereEqualTo("therapistId", therapistId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qsd) {
                        List<Booking> bookingList = qsd.toObjects(Booking.class);
                        HomeScheduleAdapter adapter = new HomeScheduleAdapter(bookingList, booking -> {
                            db.collection("bookings").document(booking.getDocId())
                                    .update("status", "Cancelled")
                                    .addOnSuccessListener( aVoid->{
                                        Toast.makeText(getContext(), "booking canceled!", Toast.LENGTH_SHORT).show();
                                    });
                        });
                        binding.homeServicesRecycle.setAdapter(adapter);
                    }
                }).addOnFailureListener(error->{

                });

    }
}