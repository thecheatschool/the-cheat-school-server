package com.thecheatschool.thecheatschool.server.service.tcs;

import com.thecheatschool.thecheatschool.server.model.tcs.TCSNotifyMeRequest;
import com.thecheatschool.thecheatschool.server.model.tcs.TCSNotifyMeSignup;
import com.thecheatschool.thecheatschool.server.repository.TCSNotifyMeRepository;
import com.thecheatschool.thecheatschool.server.service.queue.ContactEmailPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TCSNotifyMeServiceTest {

    @Mock
    private TCSNotifyMeRepository notifyMeRepository;

    @Mock
    private TCSEmailService emailService;

    @Mock
    private ContactEmailPublisher contactEmailPublisher;

    private TCSNotifyMeService notifyMeService;

    @BeforeEach
    void setUp() {
        notifyMeService = new TCSNotifyMeService(notifyMeRepository, emailService, contactEmailPublisher);
        ReflectionTestUtils.setField(notifyMeService, "queueEnabled", false);
    }

    @Test
    void testNewSignup_SendsEmailAndMarksSent() {
        TCSNotifyMeRequest request = new TCSNotifyMeRequest("Talha", "talha@example.com", "9876543210");

        when(notifyMeRepository.findByEmail("talha@example.com")).thenReturn(Optional.empty());
        when(notifyMeRepository.save(any(TCSNotifyMeSignup.class))).thenAnswer(invocation -> {
            TCSNotifyMeSignup s = invocation.getArgument(0);
            if (s.getId() == null) s.setId(1L);
            return s;
        });

        doNothing().when(emailService).sendNotifyMeEmail(request);

        notifyMeService.processNotifyMe(request);

        verify(emailService, times(1)).sendNotifyMeEmail(request);
        ArgumentCaptor<TCSNotifyMeSignup> captor = ArgumentCaptor.forClass(TCSNotifyMeSignup.class);
        verify(notifyMeRepository, atLeast(2)).save(captor.capture());
        TCSNotifyMeSignup last = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals("SENT", last.getStatus());
    }

    @Test
    void testDuplicateSignup_UpdatesExistingRow() {
        TCSNotifyMeRequest request = new TCSNotifyMeRequest("Talha", "talha@example.com", "9876543210");
        TCSNotifyMeSignup existing = new TCSNotifyMeSignup();
        existing.setId(10L);
        existing.setEmail("talha@example.com");

        when(notifyMeRepository.findByEmail("talha@example.com")).thenReturn(Optional.of(existing));
        when(notifyMeRepository.save(any(TCSNotifyMeSignup.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendNotifyMeEmail(request);

        notifyMeService.processNotifyMe(request);

        ArgumentCaptor<TCSNotifyMeSignup> captor = ArgumentCaptor.forClass(TCSNotifyMeSignup.class);
        verify(notifyMeRepository, atLeastOnce()).save(captor.capture());
        TCSNotifyMeSignup saved = captor.getAllValues().get(0);
        assertEquals(10L, saved.getId());
        assertEquals("talha@example.com", saved.getEmail());
    }

    @Test
    void testEmailFailure_MarksEmailFailed() {
        TCSNotifyMeRequest request = new TCSNotifyMeRequest("Talha", "talha@example.com", "9876543210");

        when(notifyMeRepository.findByEmail("talha@example.com")).thenReturn(Optional.empty());
        when(notifyMeRepository.save(any(TCSNotifyMeSignup.class))).thenAnswer(invocation -> {
            TCSNotifyMeSignup s = invocation.getArgument(0);
            if (s.getId() == null) s.setId(1L);
            return s;
        });

        doThrow(new RuntimeException("email down")).when(emailService).sendNotifyMeEmail(request);

        assertDoesNotThrow(() -> notifyMeService.processNotifyMe(request));

        ArgumentCaptor<TCSNotifyMeSignup> captor = ArgumentCaptor.forClass(TCSNotifyMeSignup.class);
        verify(notifyMeRepository, atLeast(2)).save(captor.capture());
        TCSNotifyMeSignup last = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals("EMAIL_FAILED", last.getStatus());
    }
}
