package com.thecheatschool.thecheatschool.server.service.tcs;

import com.thecheatschool.thecheatschool.server.service.queue.ContactEmailPublisher;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSContact;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSContactRequest;
import com.thecheatschool.thecheatschool.server.repository.TCSContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TCSContactServiceTest {

    @Mock
    private TCSEmailService emailService;

    @Mock
    private TCSContactRepository contactRepository;

    @Mock
    private ContactEmailPublisher contactEmailPublisher;

    private TCSContactService contactService;

    private TCSContactRequest createValidRequest() {
        TCSContactRequest request = new TCSContactRequest();
        request.setFullName("Talha Ahmed");
        request.setEmail("talha@example.com");
        request.setPhoneNumber("9876543210");
        request.setCollege("IIT Delhi");
        request.setYearOfStudy("2nd Year");
        request.setBranch("CSE");
        request.setHearAboutUs("WhatsApp Group");
        request.setHearAboutUsOther(null);
        return request;
    }

    @BeforeEach
    void setUp() {
        contactService = new TCSContactService(emailService, contactRepository, contactEmailPublisher);
        ReflectionTestUtils.setField(contactService, "queueEnabled", false);
    }

    @Test
    void testProcessContactFormSuccess() {
        // Arrange - Email service succeeds
        TCSContactRequest request = createValidRequest();
        doNothing().when(emailService).sendContactEmail(request);

        when(contactRepository.save(any(TCSContact.class)))
                .thenAnswer(invocation -> {
                    TCSContact contact = invocation.getArgument(0);
                    contact.setId(1L);
                    return contact;
                });

        // Act
        contactService.processContactForm(request);

        // Assert
        verify(emailService, times(1)).sendContactEmail(request);
        verify(contactRepository, atLeastOnce()).save(any(TCSContact.class));
    }

    @Test
    void testProcessContactFormEmailFailure_DatabaseBackupWorks() {
        // Arrange - Email service throws exception
        TCSContactRequest request = createValidRequest();
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendContactEmail(request);

        // Setup mock for repository save
        when(contactRepository.save(any(TCSContact.class)))
                .thenAnswer(invocation -> {
                    TCSContact contact = invocation.getArgument(0);
                    contact.setId(1L); // Simulate ID generation
                    return contact;
                });

        // Act
        contactService.processContactForm(request);

        // Assert
        verify(emailService, times(1)).sendContactEmail(request);
        // Should save to database on failure
        verify(contactRepository, times(2)).save(any(TCSContact.class));
    }

    @Test
    void testProcessContactFormEmailFailure_SavesCorrectData() {
        // Arrange
        TCSContactRequest request = createValidRequest();
        request.setHearAboutUsOther("Friend recommendation");

        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendContactEmail(request);

        ArgumentCaptor<TCSContact> contactCaptor = ArgumentCaptor.forClass(TCSContact.class);
        when(contactRepository.save(any(TCSContact.class)))
                .thenAnswer(invocation -> {
                    TCSContact contact = invocation.getArgument(0);
                    contact.setId(1L);
                    return contact;
                });

        // Act
        contactService.processContactForm(request);

        // Assert - Verify the saved contact has correct data (final save)
        verify(contactRepository, atLeast(2)).save(contactCaptor.capture());
        TCSContact savedContact = contactCaptor.getAllValues().get(contactCaptor.getAllValues().size() - 1);

        assertEquals("Talha Ahmed", savedContact.getFullName());
        assertEquals("talha@example.com", savedContact.getEmail());
        assertEquals("9876543210", savedContact.getPhoneNumber());
        assertEquals("IIT Delhi", savedContact.getCollege());
        assertEquals("2nd Year", savedContact.getYearOfStudy());
        assertEquals("CSE", savedContact.getBranch());
        assertEquals("WhatsApp Group", savedContact.getHearAboutUs());
        assertEquals("Friend recommendation", savedContact.getHearAboutUsOther());
    }

    @Test
    void testProcessContactFormEmailFailure_StatusIsEmailFailed() {
        // Arrange
        TCSContactRequest request = createValidRequest();
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendContactEmail(request);

        ArgumentCaptor<TCSContact> contactCaptor = ArgumentCaptor.forClass(TCSContact.class);
        when(contactRepository.save(any(TCSContact.class)))
                .thenAnswer(invocation -> {
                    TCSContact contact = invocation.getArgument(0);
                    contact.setId(1L);
                    return contact;
                });

        // Act
        contactService.processContactForm(request);

        // Assert - Verify status is EMAIL_FAILED (final save)
        verify(contactRepository, atLeast(2)).save(contactCaptor.capture());
        TCSContact savedContact = contactCaptor.getAllValues().get(contactCaptor.getAllValues().size() - 1);

        assertEquals("EMAIL_FAILED", savedContact.getStatus());
    }

    @Test
    void testProcessContactFormEmailFailure_ExpiryDateSet() {
        // Arrange
        TCSContactRequest request = createValidRequest();
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendContactEmail(request);

        ArgumentCaptor<TCSContact> contactCaptor = ArgumentCaptor.forClass(TCSContact.class);
        when(contactRepository.save(any(TCSContact.class)))
                .thenAnswer(invocation -> {
                    TCSContact contact = invocation.getArgument(0);
                    contact.setId(1L);
                    return contact;
                });

        LocalDateTime beforeTest = LocalDateTime.now();

        // Act
        contactService.processContactForm(request);

        LocalDateTime afterTest = LocalDateTime.now().plusDays(30);

        // Assert - Verify expiry is set to 30 days from now (final save)
        verify(contactRepository, atLeast(2)).save(contactCaptor.capture());
        TCSContact savedContact = contactCaptor.getAllValues().get(contactCaptor.getAllValues().size() - 1);

        assertNotNull(savedContact.getExpiresAt());
        assertTrue(savedContact.getExpiresAt().isAfter(beforeTest.plusDays(29)));
        assertTrue(savedContact.getExpiresAt().isBefore(afterTest.plusDays(1)));
    }

    @Test
    void testProcessContactFormEmailFailure_SubmittedAtIsSet() {
        // Arrange
        TCSContactRequest request = createValidRequest();
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendContactEmail(request);

        ArgumentCaptor<TCSContact> contactCaptor = ArgumentCaptor.forClass(TCSContact.class);
        when(contactRepository.save(any(TCSContact.class)))
                .thenAnswer(invocation -> {
                    TCSContact contact = invocation.getArgument(0);
                    contact.setId(1L);
                    return contact;
                });

        LocalDateTime beforeTest = LocalDateTime.now();

        // Act
        contactService.processContactForm(request);

        LocalDateTime afterTest = LocalDateTime.now();

        // Assert - Verify submittedAt is set to now (final save)
        verify(contactRepository, atLeast(2)).save(contactCaptor.capture());
        TCSContact savedContact = contactCaptor.getAllValues().get(contactCaptor.getAllValues().size() - 1);

        assertNotNull(savedContact.getSubmittedAt());
        assertTrue(savedContact.getSubmittedAt().isAfter(beforeTest.minusSeconds(1)));
        assertTrue(savedContact.getSubmittedAt().isBefore(afterTest.plusSeconds(1)));
    }

    @Test
    void testProcessContactFormSuccess_NoExceptionThrown() {
        // Arrange
        TCSContactRequest request = createValidRequest();
        doNothing().when(emailService).sendContactEmail(request);

        when(contactRepository.save(any(TCSContact.class)))
                .thenAnswer(invocation -> {
                    TCSContact contact = invocation.getArgument(0);
                    contact.setId(1L);
                    return contact;
                });

        // Act & Assert - Should not throw any exception
        assertDoesNotThrow(() -> contactService.processContactForm(request));
    }

    @Test
    void testProcessContactFormEmailFailure_NoExceptionThrown() {
        // Arrange - Service should handle exception gracefully
        TCSContactRequest request = createValidRequest();
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendContactEmail(request);

        when(contactRepository.save(any(TCSContact.class)))
                .thenAnswer(invocation -> {
                    TCSContact contact = invocation.getArgument(0);
                    contact.setId(1L);
                    return contact;
                });

        // Act & Assert - Should not throw exception (graceful failure)
        assertDoesNotThrow(() -> contactService.processContactForm(request));
    }
}
