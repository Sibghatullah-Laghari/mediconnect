package com.mediconnect.controller;

import com.mediconnect.dto.doctor.DoctorResponse;
import com.mediconnect.model.Gender;
import com.mediconnect.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DoctorControllerTest {

    @Test
    void specializationsAreReturnedSorted() {
        DoctorService doctorService = mock(DoctorService.class);
        DoctorController controller = new DoctorController(doctorService);
        when(doctorService.getAllSpecializations()).thenReturn(List.of("Neurology", "Cardiology", "Dermatology"));

        var response = controller.getSpecializations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of("Cardiology", "Dermatology", "Neurology"), response.getBody());
    }

    @Test
    void getDoctorByIdReturnsServiceResult() {
        DoctorService doctorService = mock(DoctorService.class);
        DoctorController controller = new DoctorController(doctorService);
        DoctorResponse expected = new DoctorResponse(
                1L,
                "Dr. A",
                Gender.MALE,
                "Cardiology",
                "1234567890",
                "doctor@example.com",
                new BigDecimal("2000.00"),
                5
        );

        when(doctorService.getDoctorById(1L)).thenReturn(expected);

        var response = controller.getDoctorById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }
}
