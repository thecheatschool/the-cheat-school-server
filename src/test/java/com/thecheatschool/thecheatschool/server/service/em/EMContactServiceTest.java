package com.thecheatschool.thecheatschool.server.service.em;

import com.thecheatschool.thecheatschool.server.service.queue.ContactEmailPublisher;
import com.thecheatschool.thecheatschool.server.model.em.EMContact;
import com.thecheatschool.thecheatschool.server.model.em.EMContactRequest;
import com.thecheatschool.thecheatschool.server.repository.EMContactRepository;
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
class EMContactServiceTest {

    @Mock
    private EMEmailService emailService;

    @Mock
    private EMContactRepository contactRepository;

    @Mock
    private ContactEmailPublisher contactEmailPublisher;

    private EMContactService contactService;

    private EMContactRequest createValidRequest() {
        EMContactRequest request = new EMContactRequest();
        request.setName("Talha Ahmed");
        request.setCompany("Emiratiyo Investments");
        request.setPhone("+971501234567");
        request.setEmail("talha@example.com");
        request.setSubject("Investment Inquiry");
        request.setMessage("Hello, I want to know more about your services.");
        return request;
    }

    @BeforeEach
    void setUp() {
        contactService = new EMContactService(emailService, contactRepository, contactEmailPublisher);
        ReflectionTestUtils.setField(contactService, "queueEnabled", false);
    }

    @Test
    void testProcessContactFormSuccess() {
        // Arrange
        EMContactRequest request = createValidRequest();
        doNothing().when(emailService).sendContactEmail(request);

        when(contactRepository.save(any(EMContact.class)))
                .thenAnswer(invocation -> {
                    EMContact contact = invocation.getArgument(0);
                    contact.setId(1L);
                    return contact;
                });

        // Act
        contactService.processContactForm(request);

        // Assert
        verify(emailService, times(1)).sendContactEmail(request);
        verify(contactRepository, atLeastOnce()).save(any(EMContact.class));
    }

    @Test
    void testProcessContactFormEmailFailure_DatabaseBackupWorks() {
        // Arrange
        EMContactRequest request = createValidRequest();
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendContactEmail(request);

        when(contactRepository.save(any(EMContact.class)))
                .thenAnswer(invocation -> {
                    EMContact contact = invocation.getArgument(0);
                    contact.setId(1L);
                    return contact;
                });

        // Act
        contactService.processContactForm(request);

        // Assert
        verify(emailService, times(1)).sendContactEmail(request);
        verify(contactRepository, times(2)).save(any(EMContact.class));
    }

    @Test
    void testProcessContactFormEmailFailure_SavesCorrectData() {
        // Arrange
        EMContactRequest request = createValidRequest();
        request.setMessage("Line1\nLine2");

        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendContactEmail(request);

        ArgumentCaptor<EMContact> contactCaptor = ArgumentCaptor.forClass(EMContact.class);
        when(contactRepository.save(any(EMContact.class)))
                .thenAnswer(invocation -> {
                    EMContact contact = invocation.getArgument(0);
                    contact.setId(1L);
                    return contact;
                });

        // Act
        contactService.processContactForm(request);

        // Assert (final save)
        verify(contactRepository, atLeast(2)).save(contactCaptor.capture());
        EMContact savedContact = contactCaptor.getAllValues().get(contactCaptor.getAllValues().size() - 1);

        assertEquals("Talha Ahmed", savedContact.getName());
        assertEquals("Emiratiyo Investments", savedContact.getCompany());
        assertEquals("+971501234567", savedContact.getPhone());
        assertEquals("talha@example.com", savedContact.getEmail());
        assertEquals("Investment Inquiry", savedContact.getSubject());
        assertEquals("Line1\nLine2", savedContact.getMessage());
        assertEquals("EMAIL_FAILED", savedContact.getStatus());
        assertNotNull(savedContact.getSubmittedAt());
        assertNotNull(savedContact.getExpiresAt());
    }

    @Test
    void testProcessContactFormEmailFailure_ExpiryDateSet() {
        // Arrange
        EMContactRequest request = createValidRequest();
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendContactEmail(request);

        ArgumentCaptor<EMContact> contactCaptor = ArgumentCaptor.forClass(EMContact.class);
        when(contactRepository.save(any(EMContact.class)))
                .thenAnswer(invocation -> {
                    EMContact contact = invocation.getArgument(0);
                    contact.setId(1L);
                    return contact;
                });

        LocalDateTime beforeTest = LocalDateTime.now();

        // Act
        contactService.processContactForm(request);

        // Assert (final save)
        verify(contactRepository, atLeast(2)).save(contactCaptor.capture());
        EMContact savedContact = contactCaptor.getAllValues().get(contactCaptor.getAllValues().size() - 1);

        assertTrue(savedContact.getExpiresAt().isAfter(beforeTest.plusDays(29)));
        assertTrue(savedContact.getExpiresAt().isBefore(beforeTest.plusDays(31)));
    }

    @Test
    void testProcessContactFormSuccess_NoExceptionThrown() {
        EMContactRequest request = createValidRequest();
        doNothing().when(emailService).sendContactEmail(request);

        when(contactRepository.save(any(EMContact.class)))
                .thenAnswer(invocation -> {
                    EMContact contact = invocation.getArgument(0);
                    contact.setId(1L);
                    return contact;
                });

        assertDoesNotThrow(() -> contactService.processContactForm(request));
    }

    @Test
    void testProcessContactFormEmailFailure_NoExceptionThrown() {
        EMContactRequest request = createValidRequest();
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendContactEmail(request);

        when(contactRepository.save(any(EMContact.class)))
                .thenAnswer(invocation -> {
                    EMContact contact = invocation.getArgument(0);
                    contact.setId(1L);
                    return contact;
                });

        assertDoesNotThrow(() -> contactService.processContactForm(request));
    }
}
