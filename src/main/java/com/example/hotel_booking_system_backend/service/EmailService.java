package com.example.hotel_booking_system_backend.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDate;

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

    public void sendPasswordResetCode(String email, String code, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set email details
            helper.setTo(email);
            helper.setSubject("Password Reset Code - Hotel Booking System");

            // Create HTML email content
            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .code { font-size: 24px; font-weight: bold; color: #4CAF50; text-align: center; margin: 20px 0; padding: 10px; background-color: #f9f9f9; border-radius: 5px; }
                        .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; font-size: 12px; color: #777; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>Password Reset Request</h2>
                        </div>
                        <p>Hello <strong>%s</strong>,</p>
                        <p>You have requested to reset your password for the Hotel Booking System.</p>
                        <p>Your password reset code is:</p>
                        <div class="code">%s</div>
                        <p><strong>This code will expire in 15 minutes.</strong></p>
                        <p>If you didn't request this password reset, please ignore this email.</p>
                        <div class="footer">
                            <p>Hotel Booking System<br>
                            This is an automated message, please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(fullName, code);

            helper.setText(htmlContent, true); // true = HTML content

            // Send email
            mailSender.send(message);

//            System.out.println("Password reset email sent successfully to: " + email);

        } catch (MessagingException e) {
//            System.err.println("Failed to send password reset email to: " + email);
            e.printStackTrace();

            // Fallback to console output
//            System.out.println("=== FALLBACK: PASSWORD RESET EMAIL ===");
//            System.out.println("To: " + email);
//            System.out.println("Name: " + fullName);
//            System.out.println("Reset Code: " + code);
//            System.out.println("==========================");
        }
    }

    // Add this method to EmailService.java

    public void sendWalkInPasswordSetup(String toEmail, String code, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Set Up Your Hotel Account Password");

            // ✅ FIX: Pass toEmail to the builder method
            String htmlContent = buildWalkInPasswordSetupEmail(fullName, code, toEmail);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    // ✅ FIX: Add toEmail parameter
    private String buildWalkInPasswordSetupEmail(String fullName, String code, String toEmail) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #4CAF50, #45a049); color: white; padding: 30px 20px; text-align: center; border-radius: 10px 10px 0 0; }
                .content { background: white; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                .code { font-size: 32px; font-weight: bold; letter-spacing: 5px; text-align: center; background: #f8f9fa; padding: 20px; border: 2px dashed #4CAF50; margin: 30px 0; border-radius: 8px; color: #333; }
                .info-box { background: #e8f5e9; border-left: 4px solid #4CAF50; padding: 15px; margin: 20px 0; border-radius: 4px; }
                .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; font-size: 12px; text-align: center; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Welcome to Our Hotel!</h1>
                </div>
                <div class="content">
                    <p>Dear <strong>%s</strong>,</p>
                    
                    <p>Thank you for your walk-in booking! We noticed you don't have a password set up yet for your account.</p>
                    
                    <div class="info-box">
                        <p><strong>Setting up your password will give you:</strong></p>
                        <ul>
                            <li>Full access to view your booking history</li>
                            <li>Ability to manage your profile</li>
                            <li>Quick rebooking for future stays</li>
                            <li>Special offers and loyalty rewards</li>
                        </ul>
                    </div>
                    
                    <p>Your verification code is:</p>
                    
                    <div class="code">%s</div>
                    
                    <p><strong>This code will expire in 15 minutes.</strong></p>
                    
                    <p>To set up your password:</p>
                    <ol>
                        <li>Click on "Forgot Password" or "Set Password" on our login page</li>
                        <li>Enter your email address: <strong>%s</strong></li>
                        <li>Enter the 4-digit code above</li>
                        <li>Create a secure password for your account</li>
                    </ol>
                    
                    <div class="footer">
                        <p>Best regards,<br>
                        <strong>Hotel Management</strong></p>
                        <p>© %d Hotel. All rights reserved.</p>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.formatted(fullName, code, toEmail, java.time.Year.now().getValue());
    }

    public void sendWalkInBookingConfirmation(
            String toEmail,
            String customerName,
            Long bookingId,
            String roomName,
            LocalDate checkIn,
            LocalDate checkOut,
            String roomNumber) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Booking Confirmation - Welcome to Our Hotel!");

            String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #4CAF50, #45a049); color: white; padding: 30px 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: white; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .booking-details { background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #dee2e6; }
                    .detail-label { font-weight: bold; color: #666; }
                    .detail-value { color: #333; }
                    .highlight { background: #4CAF50; color: white; padding: 15px; border-radius: 8px; text-align: center; margin: 20px 0; }
                    .info-box { background: #e3f2fd; border-left: 4px solid #2196F3; padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; font-size: 12px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Booking Confirmed!</h1>
                        <p>Thank you for choosing our hotel</p>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Your walk-in booking has been confirmed. We're looking forward to hosting you!</p>
                        
                        <div class="highlight">
                            <h2 style="margin: 0;">Room Number: %s</h2>
                        </div>
                        
                        <div class="booking-details">
                            <h3>Booking Details</h3>
                            <div class="detail-row">
                                <span class="detail-label">Booking ID:</span>
                                <span class="detail-value">#%d</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Room:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Check-in:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Check-out:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Room Number:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="info-box">
                            <p><strong>📧 Set Up Your Online Account</strong></p>
                            <p>You can now access your booking online! Visit our website and click "Forgot Password" to set up your account password.</p>
                            <p><strong>Benefits:</strong></p>
                            <ul>
                                <li>View booking history</li>
                                <li>Manage your profile</li>
                                <li>Quick rebooking</li>
                                <li>Exclusive member offers</li>
                            </ul>
                        </div>
                        
                        <p>If you have any questions, feel free to contact our front desk.</p>
                        
                        <div class="footer">
                            <p>Best regards,<br>
                            <strong>Hotel Management</strong></p>
                            <p>© %d Hotel. All rights reserved.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                    customerName,
                    roomNumber != null ? roomNumber : "TBA",
                    bookingId,
                    roomName,
                    checkIn.toString(),
                    checkOut.toString(),
                    roomNumber != null ? roomNumber : "To be assigned",
                    java.time.Year.now().getValue()
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send booking confirmation email", e);
        }
    }
}