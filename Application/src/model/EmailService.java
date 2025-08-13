package model;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {

    private final String senderEmail;
    private final String appPassword; // Gmail App Password, not your normal password

    public EmailService(String senderEmail, String appPassword) {
        this.senderEmail = senderEmail;
        this.appPassword = appPassword;
    }

    public void sendEmail(String recipientEmail, String subject, String body, String attachmentPath) throws MessagingException {
        // SMTP server settings for Gmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Authenticate with Gmail using app password
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        // Create email
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);

        // Email body + attachment
        Multipart multipart = new MimeMultipart();

        // Body text
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body);
        multipart.addBodyPart(textPart);

        // Attachment
        if (attachmentPath != null && !attachmentPath.isEmpty()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            try {
                attachmentPart.attachFile(attachmentPath);
                multipart.addBodyPart(attachmentPart);
            } catch (Exception e) {
                throw new MessagingException("Failed to attach file: " + attachmentPath, e);
            }
        }

        // Combine parts and send
        message.setContent(multipart);
        Transport.send(message);

        System.out.println("Email sent successfully to " + recipientEmail);
    }
}
