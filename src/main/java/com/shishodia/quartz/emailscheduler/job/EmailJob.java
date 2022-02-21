package com.shishodia.quartz.emailscheduler.job;

import java.nio.charset.StandardCharsets;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/** Tells what happens when a Quartz job gets triggered. */
@Slf4j
@Component
public class EmailJob extends QuartzJobBean {

    @Autowired
    private JavaMailSender javaMailSender;

    // This will fetch SMTP data from application.properties file.
    @Autowired
    private MailProperties mailProperties;

    /** 
     * When the job gets triggered it invokes `executeInternal()`.
     * We use JobExecutionContext to handle the things which happen around the job.
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        // When the job is executed, the JobDataMap is passed inside the JobExecutionContext.
        JobDataMap jobDataMap = context.getMergedJobDataMap();

        String subject = jobDataMap.getString("subject");
        String body = jobDataMap.getString("body");
        String recipientEmail = jobDataMap.getString("email");

        // Send this data to our SendEmail() logic.
        sendMail(mailProperties.getUsername(), recipientEmail, subject, body);

    }

    private void sendMail(String fromEmail, String toEmail, String subject, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper messageHelper = new MimeMessageHelper(message, StandardCharsets.UTF_8.toString());
            messageHelper.setSubject(subject);
            messageHelper.setText(body, true);
            messageHelper.setFrom(fromEmail);
            messageHelper.setTo(toEmail);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.error("Exception while sending mail: ", e);
        }
    }

}
