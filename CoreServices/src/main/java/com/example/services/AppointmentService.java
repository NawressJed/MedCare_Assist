package com.example.services;

import com.example.dto.AppointmentDTO;

import java.util.List;
import java.util.UUID;

public interface AppointmentService {
    AppointmentDTO createAppointment(AppointmentDTO appointment);
    AppointmentDTO createDoctorAppointment(UUID id, AppointmentDTO appointmentDTO);
    List<AppointmentDTO> getAllAppointments();
    AppointmentDTO findAppointmentByID(UUID appointmentID);
    List<AppointmentDTO> findAppointmentByDoctor(UUID uuid);
    List<AppointmentDTO> findAppointmentByPatient(UUID uuid);
    AppointmentDTO updateAppointment(AppointmentDTO appointment);
    void deleteAppointment(UUID appointmentID);
    AppointmentDTO requestAppointment(UUID patientId, AppointmentDTO appointmentDTO);
    AppointmentDTO approveAppointment(UUID appointmentId);
    void rejectAppointment(UUID appointmentId);
}
