package com.example.chatproject;

import android.os.AsyncTask;
import android.util.Log;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

// Remplace EmailSender.java
public class NotificationService {

    private static final String TAG = "NotificationService";

    // TODO: Remplacez par vos informations d'identification d'e-mail
    // Si vous utilisez Gmail, vous devrez générer un "mot de passe d'application"
    // et l'utiliser à la place de votre mot de passe habituel.
    private static final String SENDER_EMAIL = "ahmedsilini47@gmail.com";
    private static final String SENDER_PASSWORD = "fesh wtyd hqrn katd";


    /**
     * Envoie un e-mail de manière asynchrone.
     * Nécessite les dépendances JavaMail (par exemple, compile group: 'com.sun.mail', name: 'android-mail', version: '1.6.6'
     * et compile group: 'com.sun.mail', name: 'android-activation', version: '1.6.6' dans votre build.gradle (app))
     *
     * @param recipientEmail L'adresse e-mail du destinataire.
     * @param subject Le sujet de l'e-mail.
     * @param body Le corps du message de l'e-mail.
     */
    public static void sendEmail(String recipientEmail, String subject, String body) {
        new SendMailTask().execute(recipientEmail, subject, body);
    }

    private static class SendMailTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String recipientEmail = params[0];
            String subject = params[1];
            String body = params[2];

            Properties props = new Properties();

            // Configuration pour Gmail (ou un autre serveur SMTP)
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.ssl.enable", "true"); // Assurez-vous que SSL est activé

            Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

            try {
                MimeMessage mm = new MimeMessage(session);
                mm.setFrom(new InternetAddress(SENDER_EMAIL));
                mm.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
                mm.setSubject(subject);
                mm.setText(body);

                // Envoi réel
                Transport.send(mm);

                Log.d(TAG, "Email sent successfully to: " + recipientEmail);
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Error sending email to " + recipientEmail + ": " + e.getMessage(), e);
                return false;
            }
        }

        // Vous pouvez ajouter onPostExecute pour gérer le résultat si nécessaire.
        // @Override
        // protected void onPostExecute(Boolean result) {
        //    if (result) {
        //        Log.i(TAG, "Mail task completed.");
        //    }
        // }
    }
}