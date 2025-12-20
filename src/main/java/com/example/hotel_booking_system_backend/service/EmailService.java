package com.example.hotel_booking_system_backend.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAdminRegistrationCode(String toEmail, String adminName, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your Admin Access Code - Luxury Hotel");

            // HTML email template
            String htmlContent = buildAdminRegistrationEmail(adminName, code);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendAdminLoginCode(String toEmail, String adminName, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your Login Code - Luxury Hotel Admin");

            // HTML email template
            String htmlContent = buildAdminLoginEmail(adminName, code);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildAdminRegistrationEmail(String adminName, String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #333;
                        margin: 0;
                        padding: 0;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 0 auto; 
                        padding: 20px;
                        background-color: #f9f9f9;
                    }
                    .header { 
                        background: linear-gradient(135deg, #c1bd3f, #a8a535); 
                        color: white; 
                        padding: 30px 20px; 
                        text-align: center; 
                        border-radius: 10px 10px 0 0; 
                    }
                    .content { 
                        background: white; 
                        padding: 30px; 
                        border-radius: 0 0 10px 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .code { 
                        font-size: 32px; 
                        font-weight: bold; 
                        letter-spacing: 5px; 
                        text-align: center; 
                        background: #f8f9fa; 
                        padding: 20px; 
                        border: 2px dashed #c1bd3f; 
                        margin: 30px 0; 
                        border-radius: 8px;
                        color: #333;
                    }
                    .warning { 
                        background: #fff3cd; 
                        border-left: 4px solid #ffc107; 
                        padding: 15px; 
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #eee;
                        color: #666;
                        font-size: 12px;
                        text-align: center;
                    }
                    .steps {
                        margin: 20px 0;
                        padding-left: 20px;
                    }
                    .steps li {
                        margin-bottom: 10px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to Luxury Hotel Admin Portal</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>You have been added as an administrator for the Luxury Hotel system.</p>
                        
                        <p>Your 8-digit verification code is:</p>
                        
                        <div class="code">%s</div>
                        
                        <div class="warning">
                            <p><strong>Important:</strong> This code will expire in 15 minutes.</p>
                        </div>
                        
                        <p>To activate your account and set up your password:</p>
                        <ol class="steps">
                            <li>Go to the admin login page</li>
                            <li>Enter your email address</li>
                            <li>Enter the 8-digit code above</li>
                            <li>Follow the instructions to set up your password</li>
                        </ol>
                        
                        <p>If you didn't request this or have any questions, please contact the super administrator immediately.</p>
                        
                        <div class="footer">
                            <p>Best regards,<br>
                            <strong>Luxury Hotel Management</strong></p>
                            <p>© %d Luxury Hotel. All rights reserved.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(adminName, code, java.time.Year.now().getValue());
    }

    private String buildAdminLoginEmail(String adminName, String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #333;
                        margin: 0;
                        padding: 0;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 0 auto; 
                        padding: 20px;
                        background-color: #f9f9f9;
                    }
                    .header { 
                        background: linear-gradient(135deg, #c1bd3f, #a8a535); 
                        color: white; 
                        padding: 30px 20px; 
                        text-align: center; 
                        border-radius: 10px 10px 0 0; 
                    }
                    .content { 
                        background: white; 
                        padding: 30px; 
                        border-radius: 0 0 10px 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .code { 
                        font-size: 32px; 
                        font-weight: bold; 
                        letter-spacing: 5px; 
                        text-align: center; 
                        background: #f8f9fa; 
                        padding: 20px; 
                        border: 2px dashed #c1bd3f; 
                        margin: 30px 0; 
                        border-radius: 8px;
                        color: #333;
                    }
                    .warning { 
                        background: #fff3cd; 
                        border-left: 4px solid #ffc107; 
                        padding: 15px; 
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #eee;
                        color: #666;
                        font-size: 12px;
                        text-align: center;
                    }
                    .steps {
                        margin: 20px 0;
                        padding-left: 20px;
                    }
                    .steps li {
                        margin-bottom: 10px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Your Admin Login Code</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>You requested to log in to the Luxury Hotel Admin Portal.</p>
                        
                        <p>Your 8-digit verification code is:</p>
                        
                        <div class="code">%s</div>
                        
                        <div class="warning">
                            <p><strong>Security Alert:</strong> This code will expire in 15 minutes.</p>
                            <p>If you didn't request this login, please contact the super administrator immediately.</p>
                        </div>
                        
                        <p>To complete your login:</p>
                        <ol class="steps">
                            <li>Return to the login page</li>
                            <li>Enter your email address</li>
                            <li>Enter the 8-digit code above</li>
                            <li>Click "Verify & Login"</li>
                        </ol>
                        
                        <div class="footer">
                            <p>Best regards,<br>
                            <strong>Luxury Hotel Management</strong></p>
                            <p>© %d Luxury Hotel. All rights reserved.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(adminName, code, java.time.Year.now().getValue());
    }
}