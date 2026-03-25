package lk.damithab.curenextherapist.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import lk.damithab.curenextherapist.adapter.HomeBookingsAdapter;
import lk.damithab.curenextherapist.databinding.FragmentHomeBinding;
import lk.damithab.curenextherapist.dialog.CustomAlertDialog;
import lk.damithab.curenextherapist.listener.FirestoreCallback;
import lk.damithab.curenextherapist.model.Booking;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private String fromDate;
    private String toDate;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private FirebaseAuth auth;

    private static final String PREFERENCE_NAME = "therapist";

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 1;
    private String therapistId, todayDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            therapistId = getArguments().getString("therapistId");
        }
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

        DatePickerDialog dateDialog = new DatePickerDialog(getContext());
        dateDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        todayDate = sdf.format(calendar.getTime());

        if (therapistId == null) {
            SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
            therapistId = preferences.getString("therapistId", null);
        }

        Log.d("Home", "onCreate: TherapistId " + therapistId);

        binding.homeBookingsRecycle.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        binding.selectDateFromBtn.setOnClickListener(v -> {
            dateDialog.show();
            dateDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month,  int date) {
                    fromDate = String.format("%04d-%02d-%02d", year, month +1 , date);
                    binding.selectScheduleFromInput.setText(fromDate);
                    Log.d("HomeF", fromDate);
                }
            });
        });

        binding.selectDateToBtn.setOnClickListener(v -> {
            dateDialog.show();
            dateDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month,  int date) {
                    toDate = String.format("%04d-%02d-%02d", year, month +1 , date);
                    binding.selectScheduleToInput.setText(toDate);
                    Log.d("HomeF", toDate );
                }
            });
        });

        binding.fragmentHomeSearchSchedule.setOnClickListener(v -> {
            if (fromDate == null) {
                return;
            }

            if (toDate == null) {
                /// search query with from date
                db.collection("bookings").whereEqualTo("therapistId", therapistId)
                        .whereGreaterThanOrEqualTo("bookingDate", fromDate)
                        .orderBy("bookingDate", Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot qsd) {
                                checkAllTasksFinished();
                                List<Booking> bookingList = new ArrayList<>();
                                HomeBookingsAdapter adapter = new HomeBookingsAdapter(getContext());

                                if (!qsd.isEmpty()) {
                                    bookingList = qsd.toObjects(Booking.class);

                                    adapter.setCancelListener(new HomeBookingsAdapter.OnCancelBtnListener() {
                                        @Override
                                        public void onCancel(int position, Booking booking) {

                                            new CustomAlertDialog(requireContext())
                                                    .setMessage("Are you sure you want to cancel this booking?")
                                                    .setPositiveButton("Yes", v -> {
                                                        db.collection("bookings").document(booking.getDocId())
                                                                .update("status", "Cancelled")
                                                                .addOnSuccessListener(aVoid -> {
                                                                    adapter.notifyItemChanged(position);
                                                                    Toast.makeText(getContext(), "booking canceled!", Toast.LENGTH_SHORT).show();
                                                                });
                                                    }).setNegativeButton()
                                                    .show();

                                        }
                                    });
                                }

                                adapter.setBookingList(bookingList);
                                binding.homeBookingsRecycle.setAdapter(adapter);
                            }
                        }).addOnFailureListener(error -> {
                            checkAllTasksFinished();
                        });

            } else {
                /// search query with both dates

                db.collection("bookings").whereEqualTo("therapistId", therapistId)
                        .whereGreaterThanOrEqualTo("bookingDate", fromDate)
                        .whereLessThanOrEqualTo("bookingDate", toDate)
                        .orderBy("bookingDate", Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot qsd) {
                                checkAllTasksFinished();
                                List<Booking> bookingList = new ArrayList<>();
                                HomeBookingsAdapter adapter = new HomeBookingsAdapter(getContext());

                                if (!qsd.isEmpty()) {
                                    bookingList = qsd.toObjects(Booking.class);

                                    adapter.setCancelListener(new HomeBookingsAdapter.OnCancelBtnListener() {
                                        @Override
                                        public void onCancel(int position, Booking booking) {

                                            new CustomAlertDialog(requireContext())
                                                    .setMessage("Are you sure you want to cancel this booking?")
                                                    .setPositiveButton("Yes", v -> {
                                                        db.collection("bookings").document(booking.getDocId())
                                                                .update("status", "Cancelled")
                                                                .addOnSuccessListener(aVoid -> {
                                                                    adapter.notifyItemChanged(position);
                                                                    Toast.makeText(getContext(), "booking canceled!", Toast.LENGTH_SHORT).show();
                                                                });
                                                    }).setNegativeButton()
                                                    .show();

                                        }
                                    });
                                }

                                adapter.setBookingList(bookingList);
                                binding.homeBookingsRecycle.setAdapter(adapter);
                            }
                        }).addOnFailureListener(error -> {
                            checkAllTasksFinished();
                        });

            }
        });
        Log.d("Home", "today_date" + todayDate);

        startDataLoading(true);

        binding.homeResetBtn.setOnClickListener(v->{
            binding.selectScheduleFromInput.getText().clear();
            binding.selectScheduleToInput.getText().clear();
            startDataLoading(true);
        });


    }

    private void getAllBookings(FirestoreCallback<List<Booking>> onCallback) {
        db.collection("bookings").whereEqualTo("therapistId", therapistId)
                .whereGreaterThanOrEqualTo("bookingDate", todayDate)
                .orderBy("bookingDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qsd) {
                        checkAllTasksFinished();
                        List<Booking> bookingList = qsd.toObjects(Booking.class);
                        onCallback.onCallback(bookingList);
                    }
                }).addOnFailureListener(error -> {
                    checkAllTasksFinished();
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
        loadALlBookings();
    }

    private synchronized void onDataLoad(boolean isShimmer) {
        if (isShimmer) {
            binding.shimmerHomeBookingsContainer.startShimmer();
            binding.shimmerHomeBookingsContainer.setVisibility(View.VISIBLE);
            binding.fragmentHomeSearchSchedule.setEnabled(false);
            binding.homeBookingsRecycle.setVisibility(View.GONE);
        } else {
            binding.shimmerHomeBookingsContainer.stopShimmer();
            binding.fragmentHomeSearchSchedule.setEnabled(true);
            binding.shimmerHomeBookingsContainer.setVisibility(View.GONE);
            binding.homeBookingsRecycle.setVisibility(View.VISIBLE);
        }
    }

    private void loadALlBookings(){
        getAllBookings(bookingList -> {
            if (!bookingList.isEmpty()) {
                Log.d("Home", "onSuccess: bookings loading");

                HomeBookingsAdapter adapter = new HomeBookingsAdapter(getContext());
                adapter.setBookingList(bookingList);
                adapter.setCancelListener(new HomeBookingsAdapter.OnCancelBtnListener() {
                    @Override
                    public void onCancel(int position, Booking booking) {
                        boolean isCancelled = "Cancelled".equals(booking.getStatus());
                        String newStatus = isCancelled ? "Confirmed" : "Cancelled";
                        String newMsg = isCancelled ? "confirm this booking?" : "cancel this booking?";

                        new CustomAlertDialog(requireContext())
                                .setMessage("Are you sure you want to " + newMsg)
                                .setPositiveButton("Yes", v -> {
                                    db.collection("bookings").document(booking.getDocId())
                                            .update("status", newStatus)
                                            .addOnSuccessListener(aVoid -> {
                                                booking.setStatus(newStatus);
                                                adapter.notifyItemChanged(position);
                                                Toast.makeText(getContext(), "booking "+newStatus.toLowerCase()+"!", Toast.LENGTH_SHORT).show();
                                            });
                                }).setNegativeButton()
                                .show();


                    }
                });

                binding.homeBookingsRecycle.setAdapter(adapter);
            }
        });
    }

}