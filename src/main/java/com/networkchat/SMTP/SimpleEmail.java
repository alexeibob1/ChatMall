package com.networkchat.SMTP;

import javax.mail.Session;
import java.util.Properties;

public class SimpleEmail {
    public static void main(String[] args) {

        System.out.println("SimpleEmail Start");

        //String smtpHostServer = "smtp.example.com";
        String smtpHostServer = "smtp.example.com";
        String emailID = "fingerloom2004@gmail.com";

        Properties props = System.getProperties();

        props.put("mail.smtp.host", smtpHostServer);

        Session session = Session.getInstance(props, null);

        EmailUtil.sendEmail(session, emailID,"SimpleEmail Testing Subject", "SimpleEmail Testing Body");
    }
}
