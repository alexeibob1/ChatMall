package com.networkchat.smtp;

import com.networkchat.client.User;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

public class SSLEmail {
    private final String fromEmail = "fingerloom2004@gmail.com";
    private final String password = "eprxzfqggxyfjsqz";
    private final String subject = "Please verify your registration";
    private User user;
    private String content = "Dear [[name]],\n"
            + "Please enter the code below in the application to verify your registration:\n\n"
            + "[[code]]\n\n"
            + "Thank you,\n"
            + "Team of ChatMall application.";

    public SSLEmail(User user) {
        this.user = user;
    }

    public void sendConfirmationMessage(String code) {
        Properties props = new Properties();
        initProperties(props);
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };

        content = content.replace("[[name]]", user.getUsername());
        content = content.replace("[[code]]", code);

        Session session = Session.getDefaultInstance(props, auth);
        EmailUtil.sendEmail(session, this.user.getEmail(), subject, content);
    }

    private void initProperties(Properties props) {
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
    }
}