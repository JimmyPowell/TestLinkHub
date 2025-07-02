package tech.cspioneer.backend.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Date;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SMTPUtilsTest {
    private SMTPUtils smtpUtils;

    @BeforeEach
    void setUp() throws Exception {
        smtpUtils = new SMTPUtils();
        setField("host", "smtp.test.com");
        setField("port", "465");
        setField("username", "user@test.com");
        setField("password", "pwd");
        setField("protocol", "smtp");
    }

    private void setField(String field, Object value) throws Exception {
        java.lang.reflect.Field f = smtpUtils.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(smtpUtils, value);
    }

    @Test
    void testSendEmail_Normal() throws Exception {
        try (MockedStatic<Session> sessionMock = Mockito.mockStatic(Session.class);
             MockedConstruction<MimeMessage> mimeMessageMock = Mockito.mockConstruction(MimeMessage.class,
                (mock, context) -> {
                    doNothing().when(mock).setFrom(any(InternetAddress.class));
                    doNothing().when(mock).setRecipient(any(Message.RecipientType.class), any(InternetAddress.class));
                    doNothing().when(mock).setSubject(anyString(), anyString());
                    doNothing().when(mock).setContent(anyString(), anyString());
                    doNothing().when(mock).setSentDate(any(Date.class));
                    doNothing().when(mock).saveChanges();
                })) {
            Session session = mock(Session.class);
            sessionMock.when(() -> Session.getDefaultInstance(any(Properties.class))).thenReturn(session);
            Transport transport = mock(Transport.class);
            when(session.getTransport()).thenReturn(transport);

            doNothing().when(transport).connect(anyString(), anyString());
            doNothing().when(transport).sendMessage(any(MimeMessage.class), any());
            doNothing().when(transport).close();

            assertTrue(smtpUtils.sendEmail("to@test.com", "title", "content"));
        }
    }

    @Test
    void testSendEmail_Exception() throws Exception {
        try (MockedStatic<Session> sessionMock = Mockito.mockStatic(Session.class);
             MockedConstruction<MimeMessage> mimeMessageMock = Mockito.mockConstruction(MimeMessage.class,
                (mock, context) -> {
                    doNothing().when(mock).setFrom(any(InternetAddress.class));
                    doNothing().when(mock).setRecipient(any(Message.RecipientType.class), any(InternetAddress.class));
                    doNothing().when(mock).setSubject(anyString(), anyString());
                    doNothing().when(mock).setContent(anyString(), anyString());
                    doNothing().when(mock).setSentDate(any(Date.class));
                    doNothing().when(mock).saveChanges();
                })) {
            Session session = mock(Session.class);
            sessionMock.when(() -> Session.getDefaultInstance(any(Properties.class))).thenReturn(session);
            when(session.getTransport()).thenThrow(new RuntimeException("fail"));
            assertFalse(smtpUtils.sendEmail("to@test.com", "title", "content"));
        }
    }

    @Test
    void testSendHtmlMail() {
        SMTPUtils spyUtils = spy(smtpUtils);
        doReturn(true).when(spyUtils).sendEmail(anyString(), anyString(), anyString());
        assertTrue(spyUtils.sendHtmlMail("to@test.com", "title", "<b>html</b>"));
        doReturn(false).when(spyUtils).sendEmail(anyString(), anyString(), anyString());
        assertFalse(spyUtils.sendHtmlMail("to@test.com", "title", "<b>html</b>"));
    }
} 