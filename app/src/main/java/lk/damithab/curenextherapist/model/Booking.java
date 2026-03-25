package lk.damithab.curenextherapist.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    private String docId;
    private String bookingId;
    private String scheduleId;
    private String therapistId;
    private String bookingDate;
    private String bookingTime;
    private String patientName;
    private String patientCity;
    private String patientAddress;
    private String patientMobile;
    private String patientDateOfBirth;
    private double total;
    private String uid;
    private String status;

}
