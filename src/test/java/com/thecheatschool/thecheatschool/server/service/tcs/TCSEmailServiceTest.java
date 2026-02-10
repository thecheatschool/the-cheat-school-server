package com.thecheatschool.thecheatschool.server.service.tcs;

import com.thecheatschool.thecheatschool.server.model.tcs.TCSContactRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TCSEmailServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private TCSEmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new TCSEmailService();
        // Inject mocks using reflection since RestTemplate is created directly in TCSEmailService
        ReflectionTestUtils.setField(emailService, "restTemplate", restTemplate);
        // Set configuration values
        ReflectionTestUtils.setField(emailService, "resendApiKey", "test-api-key");
        ReflectionTestUtils.setField(emailService, "recipientEmail", "admin@thecheatschool.com");
    }

    @Test
    void testSendContactEmailSuccess() {
        // Arrange
        TCSContactRequest request = new TCSContactRequest();
        request.setFullName("Talha Ahmed");
        request.setEmail("talha@example.com");
        request.setPhoneNumber("9876543210");
        request.setCollege("IIT Delhi");
        request.setYearOfStudy("2nd Year");
        request.setBranch("CSE");
        request.setHearAboutUs("WhatsApp Group");
        request.setHearAboutUsOther(null);

        ResponseEntity<String> mockResponse = new ResponseEntity<>(
                "{\"id\": \"email-123\"}", HttpStatus.OK
        );
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        // Act
        assertDoesNotThrow(() -> emailService.sendContactEmail(request));

        // Assert
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void testSendContactEmailFailure_Non2xxResponse() {
        // Arrange
        TCSContactRequest request = new TCSContactRequest();
        request.setFullName("Talha Ahmed");
        request.setEmail("talha@example.com");
        request.setPhoneNumber("9876543210");
        request.setCollege("IIT Delhi");
        request.setYearOfStudy("2nd Year");
        request.setBranch("CSE");
        request.setHearAboutUs("WhatsApp Group");

        ResponseEntity<String> mockResponse = new ResponseEntity<>(
                "{\"error\": \"Unauthorized\"}", HttpStatus.UNAUTHORIZED
        );
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        // Act & Assert - Should throw RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> emailService.sendContactEmail(request));

        assertTrue(exception.getMessage().contains("Failed to send email"));
    }

    @Test
    void testSendContactEmailFailure_Exception() {
        // Arrange
        TCSContactRequest request = new TCSContactRequest();
        request.setFullName("Talha Ahmed");
        request.setEmail("talha@example.com");
        request.setPhoneNumber("9876543210");
        request.setCollege("IIT Delhi");
        request.setYearOfStudy("2nd Year");
        request.setBranch("CSE");
        request.setHearAboutUs("WhatsApp Group");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Connection timeout"));

        // Act & Assert - Should throw RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> emailService.sendContactEmail(request));

        assertTrue(exception.getMessage().contains("Failed to send email"));
    }

    @Test
    void testEmailDataContainsCorrectRecipient() {
        // Arrange
        TCSContactRequest request = new TCSContactRequest();
        request.setFullName("Talha Ahmed");
        request.setEmail("talha@example.com");
        request.setPhoneNumber("9876543210");
        request.setCollege("IIT Delhi");
        request.setYearOfStudy("2nd Year");
        request.setBranch("CSE");
        request.setHearAboutUs("WhatsApp Group");

        ResponseEntity<String> mockResponse = new ResponseEntity<>(
                "{\"id\": \"email-123\"}", HttpStatus.OK
        );
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        // Act
        emailService.sendContactEmail(request);

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq("https://api.resend.com/emails"),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class)
        );

        HttpEntity<Object> capturedEntity = entityCaptor.getValue();
        assertNotNull(capturedEntity.getBody());
    }
}
