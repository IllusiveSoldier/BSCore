package system.core;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Класс, который позволяет отправлять сообщения на электронную почту
 */
public class EmailSender {
    private String userName;
    private String userPassword;
    private Properties properties;

    public EmailSender(String userName, String userPassword) {
        this.userName = userName;
        this.userPassword = userPassword;

        properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");
    }

    /**
     * Метод, который отправляет сообщения на электронную почту
     * @param messageTheme - Тема сообщения
     * @param messageString - Текст сообщения
     * @param fromEmail - От кого
     * @param toEmail - Кому
     */
    public void send(String messageTheme, String messageString, String fromEmail, String toEmail) {
        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, userPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(messageTheme);
            message.setText(messageString);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
