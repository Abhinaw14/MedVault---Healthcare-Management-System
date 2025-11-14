package com.medvault.demo.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send password email after registration approval
     */
    public void sendPasswordEmail(String toEmail, String fullName, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("MedVault - Account Approved");
            message.setText(
                    "Dear " + fullName + ",\n\n" +
                            "Congratulations! Your registration request has been approved by the admin.\n\n" +
                            "Your login credentials are:\n" +
                            "----------------------------------------\n" +
                            "Username/Email: " + toEmail + "\n" +
                            "Password: " + password + "\n" +
                            "----------------------------------------\n\n" +
                            "For security reasons, please change your password after your first login.\n\n" +
                            "You can now login to the MedVault system.\n\n" +
                            "Best regards,\n" +
                            "MedVault Team"
            );

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email to: " + toEmail);
            e.printStackTrace();
            // You can throw exception or handle it based on your requirement
            // throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send rejection email when registration is rejected
     */
    public void sendRejectionEmail(String toEmail, String fullName, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("MedVault - Registration Request Update");
            message.setText(
                    "Dear " + fullName + ",\n\n" +
                            "We regret to inform you that your registration request could not be approved at this time.\n\n" +
                            "If you have any questions, please contact our support team.\n\n" +
                            "Best regards,\n" +
                            "MedVault Team"
            );

            mailSender.send(message);
            System.out.println("Rejection email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send rejection email to: " + toEmail);
            e.printStackTrace();
        }
    }

    /**
     * Send confirmation email when registration request is received
     */
    public void sendRegistrationConfirmation(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("MedVault - Registration Request Received");
            message.setText(
                    "Dear " + fullName + ",\n\n" +
                            "Thank you for registering with MedVault.\n\n" +
                            "Your registration request has been received and is currently under review by our admin team.\n\n" +
                            "You will receive an email notification once your request is processed.\n\n" +
                            "Best regards,\n" +
                            "MedVault Team"
            );

            mailSender.send(message);
            System.out.println("Registration confirmation email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email to: " + toEmail);
            e.printStackTrace();
        }
    }
}