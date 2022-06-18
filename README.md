# Job Scheduler

Features: (6.18)
1. Support: create job/update task/list task/delete task
2. Support: start job/stop job
3. Support 2 scheduler types: crontab and fix rate
4. Support retry when job is failed
5. Use time wheel to control the granularity of trigger time
6. Use database lock to control the conflict in high concurrent situation 
7. Client use callback to notify job's result, and scheduler use okhttp to notify client to executer jobs
8. Both scheduler and client support to scale up

Need to approve:
1. Use Kafka to replace http invoke: scheduler send message, client consume them
2. Support multi route strategies when there are lots of works
3. Implement Unit test
4. Support Docker image
5. Add more metrics
6. Support parent and child jobs (DAG)
7. UI
8. Bugfix