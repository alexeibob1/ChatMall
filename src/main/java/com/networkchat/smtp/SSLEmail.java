package com.networkchat.smtp;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

public class SSLEmail {

    /**
     Outgoing Mail (SMTP) Server
     requires TLS or SSL: smtp.gmail.com (use authentication)
     Use Authentication: Yes
     Port for SSL: 465
     */
    public static void main(String[] args) {
        final String fromEmail = "fingerloom2004@gmail.com"; //requires valid gmail id
        final String password = "eprxzfqggxyfjsqz"; // correct password for gmail id
        final String toEmail = "kr.vadim29@gmail.com\n"; // can be any email id

        System.out.println("SSLEmail Start");
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
        props.put("mail.smtp.socketFactory.port", "465"); //SSL Port
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
        props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
        props.put("mail.smtp.port", "465"); //SMTP Port

        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };

        Session session = Session.getDefaultInstance(props, auth);
        System.out.println("Session created");
        EmailUtil.sendEmail(session, toEmail,"Проверка связи", "Проверка связи в связи со смненой пароля");

    }

}
