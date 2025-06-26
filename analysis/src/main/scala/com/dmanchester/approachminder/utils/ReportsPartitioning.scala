package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.simpletypes.HasCallsignAndTime

/**
 * Functionality for partitioning position reports.
 */
object ReportsPartitioning {

  /**
   * Trait for the possible states when partitioning a collection of reports.
   *
   * @tparam R The reports' type.
   */
  private trait PartitioningState[R <: HasCallsignAndTime] {
    def processReport(report: R): PartitioningState[R]
    def partitions: Seq[(Option[String], Seq[R])]
  }

  private def beginNewAccumulatingPartition[R <: HasCallsignAndTime](report: R, completedPartitions: Seq[(Option[String], Seq[R])], timeGapSecs: Int): PartitioningState[R] = {
    report.callsign.map { callsign =>
      new StateAccumulatingWithCallsign(Seq(report), callsign, completedPartitions, timeGapSecs)
    } getOrElse {
      new StateAccumulatingWithoutCallsign(Seq(report), completedPartitions, timeGapSecs)
    }
  }

  private class StateInitial[R <: HasCallsignAndTime](timeGapSecs: Int) extends PartitioningState[R] {

    override def processReport(report: R): PartitioningState[R] = {
      beginNewAccumulatingPartition(report, Seq.empty, timeGapSecs)
    }

    override def partitions: Seq[(Option[String], Seq[R])] = Seq.empty
  }

  private class StateAccumulatingWithoutCallsign[R <: HasCallsignAndTime](partitionInProgress: Seq[R], completedPartitions: Seq[(Option[String], Seq[R])], timeGapSecs: Int) extends PartitioningState[R] {

    override def processReport(report: R): PartitioningState[R] = {

      if (report.timePosition - partitionInProgress.last.timePosition >= timeGapSecs) {
        beginNewAccumulatingPartition(report, partitions /* previously completed partitions plus the one that has been in progress */, timeGapSecs)
      } else {
        val partitionInProgressUpdated = partitionInProgress :+ report
        report.callsign.map { callsign =>
          new StateAccumulatingWithCallsign(partitionInProgressUpdated, callsign, completedPartitions, timeGapSecs)
        } getOrElse {
          new StateAccumulatingWithoutCallsign(partitionInProgressUpdated, completedPartitions, timeGapSecs)
        }
      }
    }

    override def partitions: Seq[(Option[String], Seq[R])] = completedPartitions :+ (None, partitionInProgress)
  }

  private class StateAccumulatingWithCallsign[R <: HasCallsignAndTime](partitionInProgress: Seq[R], callsign: String, completedPartitions: Seq[(Option[String], Seq[R])], timeGapSecs: Int /*TODO Is Int big enough?*/) extends PartitioningState[R] {

    override def processReport(report: R): PartitioningState[R] = {
      // Check for two criteria:
      //
      //   * whether the new report has the same callsign as the partition in progress, or has no callsign; and
      //   * whether the time gap between the new report and the partition in progress's last report isn't large enough to
      //     warrant partitioning.
      if ((report.callsign.contains(callsign) || report.callsign.isEmpty) &&
        report.timePosition - partitionInProgress.last.timePosition < timeGapSecs) {
        // The above two criteria are met. Append the new report to the partition in progress.
        val partitionInProgressUpdated = partitionInProgress :+ report
        new StateAccumulatingWithCallsign(partitionInProgressUpdated, callsign, completedPartitions, timeGapSecs)
      } else {
        // At least one of the above two criteria are not met. Treat the partition in progress as complete and begin a new
        // one.
        beginNewAccumulatingPartition(report, partitions /* previously completed partitions plus the one that has been in progress */, timeGapSecs)
      }
    }

    override def partitions: Seq[(Option[String], Seq[R])] = completedPartitions :+ (Some(callsign), partitionInProgress)
  }

  /**
   * Partition position reports from a single aircraft on a time gap or callsign change.
   *
   * @param reports The reports to partition.
   * @param timeGapSecs The time gap. A gap of at least this many seconds will lead to a new partition.
   * @tparam R The reports' type.
   * @return The partitioned reports.
   */
  def partition[R <: HasCallsignAndTime](reports: Iterable[R], timeGapSecs: Int): Seq[(Option[String], Seq[R])] = {
    reports.foldLeft(new StateInitial(timeGapSecs): PartitioningState[R]) { (partitionerState, report) =>
      partitionerState.processReport(report)
    }.partitions
  }
}