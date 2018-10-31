package com.thorton.grant.uspto.prototypewebapp.service.registratrion;

import com.thorton.grant.uspto.prototypewebapp.interfaces.Secruity.IUserService;
import com.thorton.grant.uspto.prototypewebapp.model.entities.security.UserCredentials;
import com.thorton.grant.uspto.prototypewebapp.service.mail.gmail.GmailJavaMailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegistrationListener implements
        ApplicationListener<OnRegistrationCompleteEvent> {

    private final IUserService service;

    // private final MessageSource messages;

    private final GmailJavaMailSenderService mailSender;

    @Autowired
    public RegistrationListener(IUserService service, GmailJavaMailSenderService mailSender) {
        this.service = service;
        //this.mailSender = mailSender;
        //this.messages = messages;
        this.mailSender = mailSender;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {



        UserCredentials userCredentials = event.getUserCredentials();

        String token = UUID.randomUUID().toString();
        service.createVerificationToken(userCredentials, token);

        String recipientAddress = userCredentials.getEmail();
        String subject = "Registration Confirmation";
        String confirmationUrl
                = event.getAppUrl() + "/registrationConfirm/?token=" + token;
       //  String message = messages.getMessage("message.regSucc", null, event.getLocale());

        System.out.println("email verification link: "+confirmationUrl);

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);


        //email.setText(message + " rn" + "http://localhost:8080" + confirmationUrl);
        // make send email call none blocking
        new Thread(new Runnable() {
            public void run()
            {

                // perform any operation
                mailSender.sendEmailverificationLink("http://18.221.138.198:8080"+confirmationUrl,recipientAddress);
                System.out.println("ACCOUNT ACITVATION EMAIL SENT!");
            }
        }).start();
        //mailSender.send(email);
    }
}