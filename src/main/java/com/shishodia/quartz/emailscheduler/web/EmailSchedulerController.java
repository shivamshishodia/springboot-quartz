package com.shishodia.quartz.emailscheduler.web;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import javax.validation.Valid;

import com.shishodia.quartz.emailscheduler.job.EmailJob;
import com.shishodia.quartz.emailscheduler.payload.EmailRequest;
import com.shishodia.quartz.emailscheduler.payload.EmailResponse;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/schedule/")
public class EmailSchedulerController {

    @Autowired
    private Scheduler scheduler;

    @PostMapping(path = "email")
    public ResponseEntity<EmailResponse> scheduleEmail(@Valid @RequestBody EmailRequest emailRequest) {
        try {
            // Get the date time passed in the email request.
            ZonedDateTime dateTime = ZonedDateTime.of(emailRequest.getDateTime(), emailRequest.getTimeZone());

            // If the date time is in the past, don't process the request.
            if (dateTime.isBefore(ZonedDateTime.now())) {
                EmailResponse emailResponse = new EmailResponse(false, "Data time must be after current time.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emailResponse);
            }

            // Create the job and pass it in a trigger.
            JobDetail jobDetail = buildJobDetail(emailRequest);
            Trigger trigger = buildTrigger(jobDetail, dateTime);

            // Now call the scheduler API.
            scheduler.scheduleJob(jobDetail, trigger);

            // Success message.
            EmailResponse emailResponse = new EmailResponse(true, jobDetail.getKey().getName(),
            jobDetail.getKey().getGroup(), "Email schedule successfully.");
            return ResponseEntity.status(HttpStatus.OK).body(emailResponse);

        } catch (SchedulerException e) {
            log.error("Error while scheduling email: ", e);
            EmailResponse emailResponse = new EmailResponse(false, "Error while scheduling email.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emailResponse);
        }
    }

    private JobDetail buildJobDetail(EmailRequest scheduleEmailRequest) {

        // JobDataMap holds the data for the job which is getting executed.
        JobDataMap jobDataMap = new JobDataMap();

        // We pass the data from request to the JobDataMap.
        jobDataMap.put("email", scheduleEmailRequest.getEmail());
        jobDataMap.put("subject", scheduleEmailRequest.getSubject());
        jobDataMap.put("body", scheduleEmailRequest.getBody());

        // Create a new job.
        return JobBuilder.newJob(EmailJob.class)
            // Identifier is set as a random UUID and the group is "email-jobs".
            .withIdentity(UUID.randomUUID().toString(), "email-jobs")
            // Description is just a meta data.
            .withDescription("Send Email Job")
            // Attach the job data map from request.
            .usingJobData(jobDataMap)
            // Store the jobs even without trigger and persist the job in the database.
            .storeDurably()
            .build();
    }

    // buildTrigger takes the job details and 'when do we start the job?' ZonedDateTime.
    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt) {
        // Build a trigger.
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            // The group name is "email-triggers".
            .withIdentity(jobDetail.getKey().getName(), "email-triggers")
            // Description is just a meta data.
            .withDescription("Send Email Trigger")
            .startAt(Date.from(startAt.toInstant()))
            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
            .build();
    }
    
}
