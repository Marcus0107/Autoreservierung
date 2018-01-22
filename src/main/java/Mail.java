import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.logging.Logger;


public class Mail {

    private static final String SMTP_HOST_NAME = "smtp.gmail.com";
    private static final int SMTP_HOST_PORT = 465;
    private static final String SMTP_AUTH_USER = "bpm.mail.sender@gmail.com";
    private static final String SMTP_AUTH_PWD  = "start12345";

    public void sendMailTo(String receiver, String customer, String start, String end, String employeeId) throws Exception {
        Properties props = new Properties();

        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.host", SMTP_HOST_NAME);
        props.put("mail.smtps.auth", "true");
        // props.put("mail.smtps.quitwait", "false");

        Session mailSession = Session.getDefaultInstance(props);
        mailSession.setDebug(true);
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);

        message.setSubject("Sie haben eine neue Reisekostenabrechnung für den Customer " + customer + " von " + employeeId);
        message.setContent("test", "text/plain");

        final Logger LOGGER = Logger.getAnonymousLogger();

        LOGGER.info("\n\n  EMAIL LOGGING " +
                        "\nresiver: " + receiver +
                        "\ncustomer: " + customer +
                        "\nemployeeId: " + employeeId  );


/*        message.setSubject("Sie haben eine neue Reisekostenabrechnung für den Customer " + customer + " von " + employeeId);
        message.setContent("Sehr geehrter Herr Demo, \nHerr Demo hat eine neue Reisenkostenabrechnung abgegeben für den Zeitraum vom "
                +start +" bis "+ end + "\n Diese E-Mail ist automatisch generiert.", "text/plain");*/

        message.addRecipient(Message.RecipientType.TO,
                new InternetAddress(receiver));

        transport.connect
                (SMTP_HOST_NAME, SMTP_HOST_PORT, SMTP_AUTH_USER, SMTP_AUTH_PWD);

        transport.sendMessage(message,
                message.getRecipients(Message.RecipientType.TO));
        transport.close();
    }
}