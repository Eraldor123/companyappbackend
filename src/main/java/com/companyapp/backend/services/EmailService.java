package com.companyapp.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetPasswordEmail(String to, String resetLink) {
        String subject = "Žádost o resetování hesla";
        String text = "Dobrý den,\n\nobdrželi jsme žádost o resetování vašeho hesla. Klikněte na následující odkaz pro nastavení nového hesla:\n\n" + resetLink + "\n\nPokud jste o tento požadavek nepožádali, můžete tento e-mail ignorovat.\n\nS pozdravem,\nTým CompanyApp";

        sendEmail(to, subject, text);
    }

    public void sendWelcomeEmail(String to) {
        String subject = "Vítejte v CompanyApp!";
        String text = "Dobrý den,\n\nrádi vás vítáme v naší aplikaci CompanyApp! Jsme rádi, že jste se k nám připojili. Pokud máte jakékoliv dotazy nebo potřebujete pomoc, neváhejte nás kontaktovat.\n\nS pozdravem,\nTým CompanyApp";

        sendEmail(to, subject, text);
    }

    public void sendShiftAssignmentsEmail(String to, String[] shiftDetails) {
        String subject = "Nové přiřazení na směny";
        StringBuilder textBuilder = new StringBuilder("Dobrý den,\n\nbyli jste přiřazeni na následující směny:\n\n");
        for (String detail : shiftDetails) {
            textBuilder.append("- ").append(detail).append("\n");
        }
        textBuilder.append("\nS pozdravem,\nTým CompanyApp");

        sendEmail(to, subject, textBuilder.toString());
    }

    public void sendRegistrationEmail(String to, String generatedPin, String generatedPassword) {
        String subject = "Úspěšná registrace do CompanyApp";
        String text = "Dobrý den, \n\nvaše registrace do CompanyApp byla úspěšná! Nyní můžete využívat všechny funkce naší aplikace pro správu směn a komunikaci s kolegy.\nVaše přihlašovací údaje jsou:\n\nHeslo: " + generatedPassword + "\nPin (slouží k přihlášení na směnu): " + generatedPin+"\n\nDoporučujeme vám změnit heslo po prvním přihlášení pro zvýšení bezpečnosti vašeho účtu.\n\nS pozdravem,\nTým CompanyApp";

        sendEmail(to, subject, text);
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@companyapp.cz");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}