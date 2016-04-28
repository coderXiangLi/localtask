package com.opensource.leo.localtask.cron;

import com.opensource.leo.localtask.scheduling.Trigger;

import java.util.concurrent.TimeUnit;

/**
 * Created by leo.lx on 4/14/16.
 */
public class TaskMeta {

    private Meta meta;
    private SchduleMeta schduleMeta;

    public TaskMeta(Meta meta, SchduleMeta schduleMeta) {
        this.meta = meta;
        this.schduleMeta = schduleMeta;
    }

    public String getUnique() {
        return getGroup() + "-" + getTaskName() + "-" + getProcessNum() + "-" + getThreadNum();
    }

    public String getOriginal() {
        return getGroup() + "-" + getTaskName();
    }

    public String getGroup() {
        return meta.group;
    }

    public String getTaskName() {
        return meta.taskName;
    }

    public int getDelay() {
        return schduleMeta.delay;
    }

    public int getPeriod() {
        return schduleMeta.period;
    }


    public TimeUnit getUnit() {
        return schduleMeta.unit;
    }

    public boolean isFixedRate() {
        return schduleMeta.fixedRate;
    }


    public Trigger getTrigger() {
        return schduleMeta.trigger;
    }

    public boolean isTrigger() {
        return schduleMeta.isTrigger;
    }

    public int getProcessNum() {
        return meta.partitionMeta.partitionNum;
    }

    public String getProcessMeta() {
        return meta.partitionMeta.partitionMeta;
    }

    public int getThreadNum() {
        return meta.partitionMeta.threadNum;
    }

    public String getThreadMeta() {
        return meta.partitionMeta.threadMeta;
    }

    public static class Meta {
        // TaskMeta info
        private String group;
        private String taskName;
        private PartitionMeta partitionMeta;

        public Meta(String group, String taskName, PartitionMeta partitionMeta) {
            this.group = group;
            this.taskName = taskName;
            this.partitionMeta = partitionMeta;
        }

        public Meta(String group, String taskName) {
            this.group = group;
            this.taskName = taskName;
            this.partitionMeta = new PartitionMeta();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Meta meta = (Meta) o;

            if (group != null ? !group.equals(meta.group) : meta.group != null) return false;
            if (taskName != null ? !taskName.equals(meta.taskName) : meta.taskName != null) return false;
            return partitionMeta != null ? partitionMeta.equals(meta.partitionMeta) : meta.partitionMeta == null;

        }

        @Override
        public int hashCode() {
            int result = group != null ? group.hashCode() : 0;
            result = 31 * result + (taskName != null ? taskName.hashCode() : 0);
            result = 31 * result + (partitionMeta != null ? partitionMeta.hashCode() : 0);
            return result;
        }
    }

    public static class SchduleMeta {
        // time schedule
        private int delay;
        private int period;
        private TimeUnit unit;
        private boolean fixedRate;

        // trigger
        private Trigger trigger;
        private boolean isTrigger;

        public SchduleMeta(Trigger trigger) {
            this.trigger = trigger;
            this.isTrigger = true;
        }

        public SchduleMeta(int delay, int period, TimeUnit unit, boolean fixedRate) {
            this.delay = delay;
            this.period = period;
            this.unit = unit;
            this.fixedRate = fixedRate;
            this.isTrigger = false;
        }
    }

    /**
     * Created by leo.lx on 4/14/16.
     */
    public static class PartitionMeta {
        // process
        private int partitionCount;
        private int partitionNum;
        private String partitionMeta;

        // thread
        private int threadNum;
        private String threadMeta;

        public PartitionMeta() {
        }

        public PartitionMeta(int partitionCount, int partitionNum, String partitionMeta, int threadNum, String threadMeta) {
            this.partitionCount = partitionCount;
            this.partitionNum = partitionNum;
            this.partitionMeta = partitionMeta;
            this.threadNum = threadNum;
            this.threadMeta = threadMeta;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PartitionMeta that = (PartitionMeta) o;

            if (partitionNum != that.partitionNum) return false;
            if (threadNum != that.threadNum) return false;
            if (partitionMeta != null ? !partitionMeta.equals(that.partitionMeta) : that.partitionMeta != null)
                return false;
            return threadMeta != null ? threadMeta.equals(that.threadMeta) : that.threadMeta == null;

        }

        @Override
        public int hashCode() {
            int result = partitionNum;
            result = 31 * result + (partitionMeta != null ? partitionMeta.hashCode() : 0);
            result = 31 * result + threadNum;
            result = 31 * result + (threadMeta != null ? threadMeta.hashCode() : 0);
            return result;
        }
    }
}
