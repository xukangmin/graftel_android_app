package com.graftel.www.graftel.mail;

import android.os.StrictMode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by I R 03 on 6/8/2016.
 */
public class SendMail extends javax.mail.Authenticator
{
    private String mailhost = "smtp.sendgrid.net";
    private String user;
    private String password;
    private Session session;



    static {
        Security.addProvider(new JSSEProvider());
    }

    public SendMail(String user, String password) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.socketFactory.port", "587");
        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, String sender,String mFileName) throws Exception {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        System.setProperty("https.protocols", "TLSv1");
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("scott@graftel.com","Graftel APP"));
        message.setSubject(subject);
        message.setReplyTo(new javax.mail.Address[]
        {
                new javax.mail.internet.InternetAddress(sender)
        });
        /*if (recipients.indexOf(',') > 0) {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sender));
        }
        else*/
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(sender));
        message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse("scott@graftel.com,kangmin@graftel.com,esther@graftel.com,pdavis@graftel.com"));
        DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart); //Set Text Body

        //This is Attachment
        if(!mFileName.equals(""))
        {
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(mFileName);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(mFileName);
            multipart.addBodyPart(messageBodyPart);
        }

        message.setContent(multipart);
        Transport.send(message);
    }
    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}
