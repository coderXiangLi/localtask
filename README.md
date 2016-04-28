# localtask
A simple cron job framework. Developer can write a personal job.

Framework will schedule your job.

Develop:
 * implement a executor and match it with a task
 * run a localtask with Annotation @PersonTask.
 * run Bootstrap to run tasks(group-task & a multi-task) in project.
 * run Bootstrap with args : -run group-task and only run that task.

VM options:
 * -Dcore=4
 * -DpartitionCount=2
 * -DpartitionNum=1
Args:
 * -run group-task-1-0