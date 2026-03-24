package lk.damithab.curenextherapist.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import lk.damithab.curenextherapist.R;
import lk.damithab.curenextherapist.model.Booking;
import lk.damithab.curenextherapist.model.Therapist;

public class HomeScheduleAdapter extends RecyclerView.Adapter<HomeScheduleAdapter.ViewHolder> {

    private List<Booking> bookingList;

    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private OnCancelBtnListener listener;

    public HomeScheduleAdapter(List<Booking> bookingList, OnCancelBtnListener onCancelBtnListener) {
        this.bookingList = bookingList;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        this.listener = onCancelBtnListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_schedule, parent, false);
        return new HomeScheduleAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        db.collection("therapist").whereEqualTo("therapistId", booking.getTherapistId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if(!qds.isEmpty()){
                            Therapist therapist = qds.toObjects(Therapist.class).get(0);
                            holder.bookingTherapistName.setText(therapist.getTitle()+" "+therapist.getName());
                            StorageReference ref = storage.getReference(therapist.getTherapistImage());

//                            GlideApp.with(holder.itemView.getContext())
//                                    .load(ref)
//                                    .centerCrop()
//                                    .placeholder(R.drawable.imageplaceholder2)
//                                    .into(holder.therapistImage);
                        }
                    }
                });
        holder.patientMobile.setText(booking.getPatientMobile());
        holder.patientCity.setText(booking.getPatientCity());
        holder.patientName.setText(booking.getPatientName());

        holder.bookingDate.setText(booking.getBookingDate());
        holder.bookingTimeSlot.setText(booking.getBookingTime());
        holder.bookingStatus.setText(booking.getStatus());

        if(Objects.equals(booking.getStatus(), "Cancelled")){
            holder.statusCard.setCardBackgroundColor(Color.RED);
            holder.bookingStatus.setTextColor(Color.WHITE);
            holder.cancelButton.setEnabled(false);
        }

        holder.cancelButton.setOnClickListener(v->{
            if(listener != null){
                listener.onCancel(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView patientName, bookingTherapistName, bookingDate, bookingTimeSlot, bookingStatus, patientMobile, patientCity;

        ImageView therapistImage;

        MaterialButton cancelButton;

        CardView statusCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.patientName = itemView.findViewById(R.id.item_booking_patient_name);
            this.patientMobile = itemView.findViewById(R.id.item_booking_patient_number);
            this.bookingDate = itemView.findViewById(R.id.item_booking_date);
            this.bookingTimeSlot = itemView.findViewById(R.id.item_booking_time_slot);
            this.bookingStatus = itemView.findViewById(R.id.item_booking_status);
            this.patientCity = itemView.findViewById(R.id.item_booking_patient_city);
            this.cancelButton = itemView.findViewById(R.id.item_booking_cancel_btn);
            this.statusCard = itemView.findViewById(R.id.item_booking_status_card);
        }
    }

    public interface OnCancelBtnListener{
        void onCancel(Booking booking);
    }
}
