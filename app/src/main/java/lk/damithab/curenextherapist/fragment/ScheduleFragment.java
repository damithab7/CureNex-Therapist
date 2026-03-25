package lk.damithab.curenextherapist.fragment;

import static android.content.Context.MODE_PRIVATE;

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
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.damithab.curenextherapist.adapter.HomeBookingsAdapter;
import lk.damithab.curenextherapist.adapter.ScheduleAdapter;
import lk.damithab.curenextherapist.adapter.TherapistScheduleAdapter;
import lk.damithab.curenextherapist.databinding.FragmentScheduleBinding;
import lk.damithab.curenextherapist.dialog.AddScheduleBottomSheet;
import lk.damithab.curenextherapist.dialog.CustomAlertDialog;
import lk.damithab.curenextherapist.dialog.SpinnerDialog;
import lk.damithab.curenextherapist.dialog.ToastDialog;
import lk.damithab.curenextherapist.model.TherapistSchedule;

public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;

    private FirebaseFirestore db;

    private String therapistId;

    private static final String PREFERENCE_NAME = "therapist";

    private int completedTasks = 0;
    private final int TOTAL_TASKS = 1;

    private List<TherapistSchedule> therapistSchedules = new ArrayList<>();
    private TherapistScheduleAdapter scheduleAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        if (therapistId == null) {
            SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
            therapistId = preferences.getString("therapistId", null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.therapistScheduleRecycle.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        startDataLoading(true);

        scheduleAdapter = new TherapistScheduleAdapter();

        db.collection("therapist").document(therapistId).collection("schedule")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        checkAllTasksFinished();
                        if (!qds.isEmpty()) {
                            Log.d("Schedule Fragment", "onSuccess: Schedules is available");
                            therapistSchedules = qds.toObjects(TherapistSchedule.class);

                            scheduleAdapter.setScheduleList(therapistSchedules);
                            binding.therapistScheduleRecycle.setAdapter(scheduleAdapter);
                        }
                    }
                }).addOnFailureListener(error -> {
                    checkAllTasksFinished();
                });

        binding.therapistScheduleRecycle.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        scheduleAdapter.setOnRemoveListener(new TherapistScheduleAdapter.OnRemoveListener() {
            @Override
            public void onRemoved(int position, TherapistSchedule schedule) {

                new CustomAlertDialog(requireContext())
                        .setMessage("Are you sure you want to remove this schedule?")
                        .setPositiveButton("Yes", v -> {
                            therapistSchedules.remove(position);
                            scheduleAdapter.setScheduleList(therapistSchedules);

                            SpinnerDialog dialog = SpinnerDialog.show(getParentFragmentManager());
                            db.collection("therapist").document(therapistId).collection("schedule")
                                    .document(schedule.getScheduleId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        dialog.dismiss();
                                        new ToastDialog(getParentFragmentManager(), "Schedule removed successfully!");
                                    }).addOnFailureListener(error -> {
                                        dialog.dismiss();
                                    });
                        }).setNegativeButton()
                        .show();

            }
        });

        binding.addScheduleBtn.setOnClickListener(v -> {
            AddScheduleBottomSheet sheet = new AddScheduleBottomSheet((schedule) -> {
                therapistSchedules.add(schedule);
                Log.d("ScheduleFragment", "onViewCreated: Schedule Time" + schedule.getStartTime());
                scheduleAdapter.setScheduleList(therapistSchedules);
                binding.therapistScheduleRecycle.setAdapter(scheduleAdapter);

                SpinnerDialog dialog = SpinnerDialog.show(getParentFragmentManager());

                db.collection("therapist").document(therapistId).collection("schedule")
                        .document()
                        .set(schedule)
                        .addOnSuccessListener(aVoid -> {
                            dialog.dismiss();
                            new ToastDialog(getParentFragmentManager(), "Schedule added successfully!");
                        }).addOnFailureListener(error -> {
                            dialog.dismiss();
                        });
            });

            sheet.show(getChildFragmentManager(), "ScheduleBottomSheet");
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
    }

    private synchronized void onDataLoad(boolean isShimmer) {
        if (isShimmer) {
            binding.shimmerScheduleContainer.startShimmer();
            binding.shimmerScheduleContainer.setVisibility(View.VISIBLE);
            binding.scheduleBottom.setVisibility(View.GONE);
            binding.scheduleMain.setVisibility(View.GONE);
        } else {
            binding.shimmerScheduleContainer.stopShimmer();
            binding.shimmerScheduleContainer.setVisibility(View.GONE);
            binding.scheduleBottom.setVisibility(View.VISIBLE);
            binding.scheduleMain.setVisibility(View.VISIBLE);
        }
    }
}