package model;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {

    private final String mySenderEmail;
    private final String myAppPassword; // Gmail App Password, not your normal password

    public EmailService(final String theSenderEmail, final String theAppPassword) {
        this.mySenderEmail = theSenderEmail;
        this.myAppPassword = theAppPassword;
    }

    public void sendEmail(final String thRecipientEmail, final String theSubject, final String theBody, final String theAttachmentPath) throws MessagingException {
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
                return new PasswordAuthentication(mySenderEmail, myAppPassword);
            }
        });

        // Create email
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mySenderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(thRecipientEmail));
        message.setSubject(theSubject);

        // Email theBody + attachment
        Multipart multipart = new MimeMultipart();

        // Body text
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(theBody);
        multipart.addBodyPart(textPart);

        // Attachment
        if (theAttachmentPath != null && !theAttachmentPath.isEmpty()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            try {
                attachmentPart.attachFile(theAttachmentPath);
                multipart.addBodyPart(attachmentPart);
            } catch (Exception e) {
                throw new MessagingException("Failed to attach file: " + theAttachmentPath, e);
            }
        }

        // Combine parts and send
        message.setContent(multipart);
        Transport.send(message);
    }
}
