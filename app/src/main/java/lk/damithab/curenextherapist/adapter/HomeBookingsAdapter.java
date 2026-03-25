package lk.damithab.curenextherapist.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;
import java.util.Objects;

import lk.damithab.curenextherapist.R;
import lk.damithab.curenextherapist.model.Booking;

public class HomeBookingsAdapter extends RecyclerView.Adapter<HomeBookingsAdapter.ViewHolder> {

    private List<Booking> bookingList;

    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private OnCancelBtnListener listener;

    private Context context;

    public void setCancelListener(OnCancelBtnListener listener){
        this.listener = listener;
    }

    public void setBookingList(List<Booking> bookingList){
        this.bookingList = bookingList;
        notifyDataSetChanged();
    }

    public HomeBookingsAdapter(Context context) {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_schedule, parent, false);
        return new HomeBookingsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        int currentPosition = holder.getAbsoluteAdapterPosition();
        if(currentPosition == RecyclerView.NO_POSITION){
            return;
        }

        holder.patientMobile.setText(booking.getPatientMobile());
        holder.patientCity.setText(booking.getPatientCity());
        holder.patientName.setText(booking.getPatientName());

        holder.bookingDate.setText(booking.getBookingDate());
        holder.bookingTimeSlot.setText(booking.getBookingTime());
        holder.bookingStatus.setText(booking.getStatus());

        if ("Cancelled".equals(booking.getStatus())) {
            holder.statusCard.setCardBackgroundColor(Color.RED);
            holder.bookingStatus.setTextColor(Color.WHITE);
            holder.cancelButton.setText("Confirm");
        } else {
            holder.cancelButton.setText("Cancel");

            int surfaceColor = ContextCompat.getColor(context, R.color.md_theme_surfaceBright);
            int stockColor = ContextCompat.getColor(context, R.color.stock_color);

            holder.statusCard.setCardBackgroundColor(surfaceColor);
            holder.bookingStatus.setTextColor(stockColor);
        }

        holder.cancelButton.setOnClickListener(v->{
            if(listener != null){
                listener.onCancel(currentPosition, booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView patientName, bookingDate, bookingTimeSlot, bookingStatus, patientMobile, patientCity;

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
        void onCancel(int position, Booking booking);
    }
}
