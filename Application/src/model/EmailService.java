package model;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * A service class for sending emails through Gmail using SMTP and an app password.
 * <p>
 * This class supports sending plain text emails with optional file attachments.
 * Authentication requires the sender's Gmail address and a valid app password,
 * not the user's regular Gmail password.
 *
 * @author Abdulrahman Hassan and Kevin Kamau
 */
public class EmailService {

    private final String mySenderEmail;
    private final String myAppPassword; // Gmail App Password, not your normal password

    /**
     * Constructs an EmailService with the given sender email and Gmail app password.
     *
     * @param theSenderEmail the Gmail address used to send emails
     * @param theAppPassword the Gmail app password (not the regular account password)
     */
    public EmailService(final String theSenderEmail, final String theAppPassword) {
        this.mySenderEmail = theSenderEmail;
        this.myAppPassword = theAppPassword;
    }

    /**
     * Sends an email with the specified recipient, subject, body text, and optional attachment.
     * <p>
     * If the attachment path is null or empty, only the body text will be sent.
     *
     * @param thRecipientEmail the recipient's email address
     * @param theSubject       the subject of the email
     * @param theBody          the plain text body of the email
     * @param theAttachmentPath the optional file path of an attachment (may be null or empty)
     * @throws MessagingException if an error occurs while constructing or sending the email
     */
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

        // Email body and optional attachment
        Multipart multipart = getMultipart(theBody, theAttachmentPath);

        // Combine parts and send
        message.setContent(multipart);
        Transport.send(message);
    }

    /**
     * Builds a multipart message containing the text body and optional attachment.
     *
     * @param theBody the plain text content of the email
     * @param theAttachmentPath the file path of an attachment (may be null or empty)
     * @return a multipart email body containing text and optional attachment
     * @throws MessagingException if the attachment cannot be added
     */
    private static Multipart getMultipart(String theBody, String theAttachmentPath) throws MessagingException {
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
        return multipart;
    }
}
