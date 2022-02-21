# Springboot Quartz

A Basic Spring boot Quartz-based Scheduler.

References:
- https://www.callicoder.com/spring-boot-quartz-scheduler-email-scheduling-example/
- https://howtodoinjava.com/spring-batch/quartz-h2-jdbcjobstore-example/

## Dependencies

- Spring Web
- Spring Data JPA
- Java Mail Sender
- Quartz
- H2 Database
- Validation API
- Lombok

## Application Properties

- Database
  - `testdb` database can be accessed on [h2-console](http://localhost:8080/h2-console)
  - `quartz_tables.sql` provides all the initial tables required for Quartz to run.
- Quartz
- Java Mail Sender

# Quartz Scheduler's APIs and Terminologies

1. Scheduler
   * The Primary API for scheduling, unscheduling, adding, and removing Jobs.
2. Job
   * The interface to be implemented by classes that represent a 'job' in Quartz. It has a single method called execute() where you write the work that needs to be performed by the Job.
3. JobDetail (JobDataMap holds our request data)
   * A JobDetail represents an instance of a Job. It also contains additional data in the form of a JobDataMap that is passed to the Job when it is executed.
   * Every JobDetail is identified by a JobKey that consists of a name and a group. The name must be unique within a group.
4. Trigger
   * A Trigger, as the name suggests, defines the schedule at which a given Job will be executed. A Job can have many Triggers, but a Trigger can only be associated with one Job.
   * Every Trigger is identified by a TriggerKey that comprises of a name and a group. The name must be unique within a group.
   * Just like JobDetails, Triggers can also send parameters/data to the Job.
5. JobBuilder
   * JobBuilder is a fluent builder-style API to construct JobDetail instances.
6. TriggerBuilder
   * TriggerBuilder is used to instantiate Triggers.

## Building JobDataMap, JobDetails and Triggers

1. `EmailSchedulerController` contains four things:
   * `buildJobDetail` to build the JobDataMap and JobDetails.
   * `buildTrigger` to build the trigger.
   * POST request containing email request data.
   * `scheduleEmail` to schedule the job. The job envokes `EmailJob.executeInternal()` and passes the JobDetails as JobExecutionContext.

## EmailJob Class

- When the job gets triggered it invokes `EmailJob.executeInternal()`.
- The job (when triggered) passes the JobExecutionContext as the parameter inside executeInternal().
- We use JobExecutionContext to fetch the job details back so that they can be used to apply further logic.
- In this project we fetch the JobDataMap back from the context and use it to send emails.

## Trigger Email Job 

- Use Thunderclient to execute the request exported under resource folder. 
- The email will be triggered at the provided time.

## Quartz Backend

- Go to [h2-console](http://localhost:8080/h2-console) 
