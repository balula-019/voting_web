package com.yudhassif.election.services;

import com.yudhassif.election.exception.MessageNotFoundException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // ---------------- OTP Email (your existing method) ----------------
    public void sendOtpEmail(String userEmail, String otp, String firstName, String roleName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(userEmail);
            helper.setSubject("ADMIN".equals(roleName)
                    ? "Admin Password Reset Code"
                    : "Your Password Reset Code");

            helper.setText(buildEmailContent(firstName, otp, roleName), true);
            helper.setFrom("no-reply@desi.go.tz");

            mailSender.send(message);
        } catch (Exception ex) {
            throw new MessageNotFoundException("Failed to send OTP email");
        }
    }

    // ---------------- Activation Link Email ----------------
    public void sendActivationLink(String userEmail, String firstName, String activationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(userEmail);
            helper.setSubject("Activate Your Election Account");

            // Build HTML content (clean formatting, senior-level)
            String htmlContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<body style=\"font-family: Arial, sans-serif;\">" +
                    "<h2 style=\"color:#1976D2;\">Welcome to ARIS 3.0</h2>" +
                    "<p>Dear <strong>" + (firstName != null ? firstName : "Student") + "</strong>,</p>" +
                    "<p>Your account has been created. To activate your account, please click the link below:</p>" +
                    "<p><a href=\"" + activationLink + "\" " +
                    "style=\"display:inline-block;padding:10px 20px;background-color:#1976D2;" +
                    "color:white;text-decoration:none;border-radius:5px;\">Activate Account</a></p>" +
                    "<p>This link will expire in 24 hours.</p>" +
                    "<p>If you did not request this, please ignore this email.</p>" +
                    "<p>Regards,<br>ARIS 3.0 Team</p>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);
            helper.setFrom("no-reply@desi.go.tz");

            mailSender.send(message);

        } catch (Exception ex) {
            throw new MessageNotFoundException("Failed to send activation email");
        }
    }

    // ---------------- Helper for OTP HTML ----------------
    private String buildEmailContent(String firstName, String otp, String roleName) {
        if ("ADMIN".equals(roleName)) return adminTemplate(firstName, otp);
        return studentTemplate(firstName, otp);
    }

    private String adminTemplate(String firstName, String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color:#B00020;">ADMIN SECURITY ALERT</h2>
                <p>
                    Dear <strong>%s</strong>,
                    <br>
                    A password reset was requested for your ADMIN account.
                </p>
                <div style="font-size:32px;font-weight:bold;letter-spacing:6px;">
                    %s
                </div>
                <p>
                    This code expires in <strong>5 minutes</strong>.
                    <br>
                    If this was NOT you, contact system security immediately.
                </p>
            </body>
            </html>
        """.formatted(
                firstName != null ? firstName : "Admin",
                otp
        );
    }
//        return "<!DOCTYPE html>" +
//                "<html><body style=\"font-family: Arial,sans-serif;\">" +
//                "<h2 style=\"color:#B00020;\">ADMIN SECURITY ALERT</h2>" +
//                "<p>Dear <strong>" + (firstName != null ? firstName : "Admin") + "</strong>,<br>" +
//                "A password reset was requested for your ADMIN account.</p>" +
//                "<div style=\"font-size:32px;font-weight:bold;letter-spacing:6px;\">" + otp + "</div>" +
//                "<p>This code expires in <strong>5 minutes</strong>.<br>" +
//                "If this was NOT you, contact system security immediately.</p>" +
//                "</body></html>";


    private String studentTemplate(String firstName, String otp) {
        return "<!DOCTYPE html>" +
                "<html><body style=\"font-family: Arial,sans-serif;\">" +
                "<h2>E-VOTING Security Code</h2>" +
                "<p>Dear <strong>" + (firstName != null ? firstName : "Student") + "</strong>,</p>" +
                "<p>Please use the following verification code:</p>" +
                "<div style=\"font-size:32px;font-weight:bold;letter-spacing:5px;\">" + otp + "</div>" +
                "<p>Use this code to access your e-voting account.<br>" +
                "For help contact <strong>+255 659 819 040</strong></p>" +
                "</body></html>";
    }
    }






//
//import com.yudhassif.election.exception.MessageNotFoundException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    public void sendOtpEmail(
//            String userEmail,
//            String otp,
//            String firstName,
//            String roleName
//            // "ADMIN" or "STUDENT"
//    ) {
//
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper =
//                    new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setTo(userEmail);
//
//            // Subject based on role
//            helper.setSubject(
//                    "ADMIN".equals(roleName)
//                            ? "Admin Password Reset Code"
//                            : "Your Password Reset Code"
//            );
//
//            // Content based on role
//            String content = buildEmailContent(firstName, otp, roleName);
//
//            helper.setText(content, true);
//            helper.setFrom("no-reply@desi.go.tz");
//
//            mailSender.send(message);
//
//        } catch (Exception ex) {
//            throw new MessageNotFoundException("Failed to send OTP email");
//        }
//    }
//
//    // 🔹 Clean role switch
//    private String buildEmailContent(
//            String firstName,
//            String otp,
//            String roleName
//    ) {
//
//        if ("ADMIN".equals(roleName)) {
//            return adminTemplate(firstName, otp);
//        }
//        return studentTemplate(firstName, otp);
//    }
//
//    // 🔐 ADMIN TEMPLATE
//    private String adminTemplate(String firstName, String otp) {
//        return """
//            <!DOCTYPE html>
//            <html>
//            <body style="font-family: Arial, sans-serif;">
//                <h2 style="color:#B00020;">ADMIN SECURITY ALERT</h2>
//                <p>
//                    Dear <strong>%s</strong>,
//                    <br>
//                    A password reset was requested for your ADMIN account.
//                </p>
//                <div style="font-size:32px;font-weight:bold;letter-spacing:6px;">
//                    %s
//                </div>
//                <p>
//                    This code expires in <strong>5 minutes</strong>.
//                    <br>
//                    If this was NOT you, contact system security immediately.
//                </p>
//            </body>
//            </html>
//        """.formatted(
//                firstName != null ? firstName : "Admin",
//                otp
//        );
//    }
//
//    // 🎓 STUDENT TEMPLATE
//    private String studentTemplate(String firstName, String otp) {
//        return """
//            <!DOCTYPE html>
//            <html>
//            <body style="font-family: Arial, sans-serif;">
//                <h2>ARIS 3.0 Security Code</h2>
//                <p>
//                    Dear <strong>%s</strong>,
//                    <br>
//                    Please use the following verification code:
//                </p>
//                <div style="font-size:32px;font-weight:bold;letter-spacing:5px;">
//                    %s
//                </div>
//                <p>
//                    Use this code to access your ARIS 3.0 account.
//                    <br>
//                    For help contact <strong>+255 659 819 040</strong>
//                </p>
//            </body>
//            </html>
//        """.formatted(
//                firstName != null ? firstName : "Student",
//                otp
//        );
//    }
//}





























//
//
//import com.yudhassif.election.exception.MessageNotFoundException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class EmailService {
//    private final JavaMailSender mailSender;
//    //here we need to create and send message through the java mail sender
//    public void sendOtpEmail(String userEmail, String otp, String firstName){
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper  helper = new MimeMessageHelper(message, false, "UTF-8");
//            //here the message is the object , false it means no any file attachment or complex html, utf means correct symbol like""" or +-
//            helper.setTo(userEmail);
//            helper.setSubject("Your password reset code");
//            String content = """
//                <!DOCTYPE html>
//                <html>
//                <body style="font-family: Arial, sans-serif;">
//                    <h2>ARIS 3.0 Security Code</h2>
//                    <p>
//                        Dear <strong>%s</strong>,
//                        <br>
//                        Please receive your verification code:
//                    </p>
//                    <div style="font-size:32px;font-weight:bold;letter-spacing:5px;">
//                        %s
//                    </div>
//                    <p>Use this code to access your ARIS 3.0 account.<br><h2>For any help contact <strong>+255659819040<strong></h2></p>
//                </body>
//                </html>
//                """.formatted(firstName != null ? firstName : "", otp);
//
//            helper.setText(content,true); // if I want my otp to be bold I can enable html to be true
//            helper.setFrom("no-reply@aris.go.tz"); // here we can put like the name of the company or platform
//            mailSender.send(message);
//        } catch (Exception ex) {
//            throw new MessageNotFoundException("Failed to send email");
//        }
//
//
//
//    }
//
//
//}
