package app.uvsy.service;

import app.uvsy.environment.Environment;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import javax.mail.Authenticator;

public class EmailService {

    public void sendEmail(String subject, String content) throws MessagingException {
        Message message = prepareMessage(subject, content);
        Transport.send(message);
    }

    private Message prepareMessage(String subject, String content) throws MessagingException {

        String username = Environment.getReportEmailUsername();
        String password = Environment.getReportEmailPassword();
        String recipients = Environment.getReportEmailRecipients();

        Session session = getSession(username, password);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        message.setSubject(subject);
        message.setText(content);
        return message;
    }

    private Session getSession(String username, String password) {
        Properties props = getEmailProperties();
        Authenticator authenticator = getAuthenticator(username, password);
        return Session.getInstance(props, authenticator);
    }

    private Authenticator getAuthenticator(String username, String password) {
        return new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    private Properties getEmailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.starttls.enable", "true");
        return props;
    }
}
