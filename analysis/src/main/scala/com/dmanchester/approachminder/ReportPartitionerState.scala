package com.dmanchester.approachminder

import com.dmanchester.approachminder.ReportPartitionerStateUtils.beginNewAccumulatingPartition

/**
 * Trait for the possible states of a reports partitioner.
 *
 * This trait and its implementing classes are an implementation detail of ReportUtils.partition(). See that method's
 * tests for coverage of this file's code.
 *
 * @tparam R The reports' type.
 */
trait ReportPartitionerState[R <: HasPositionReportIdentifiers] {
  def processReport(report: R): ReportPartitionerState[R]
  def partitions: Seq[(Option[String], Seq[R])]
}

class StateInitial[R <: HasPositionReportIdentifiers](timeGapSecs: Int) extends ReportPartitionerState[R] {

  override def processReport(report: R): ReportPartitionerState[R] = {
    beginNewAccumulatingPartition(report, Seq.empty, timeGapSecs)
  }

  override def partitions: Seq[(Option[String], Seq[R])] = Seq.empty
}

class StateAccumulatingWithoutCallsign[R <: HasPositionReportIdentifiers](partitionInProgress: Seq[R], completedPartitions: Seq[(Option[String], Seq[R])], timeGapSecs: Int) extends ReportPartitionerState[R] {

  override def processReport(report: R): ReportPartitionerState[R] = {

    if (report.timePosition - partitionInProgress.last.timePosition >= timeGapSecs) {
      beginNewAccumulatingPartition(report, partitions, timeGapSecs)
    } else {
      val partitionInProgressUpdated = partitionInProgress :+ report
      report.callsign.map { theCallsign =>
        new StateAccumulatingWithCallsign(partitionInProgressUpdated, theCallsign, completedPartitions, timeGapSecs)
      } getOrElse {
        new StateAccumulatingWithoutCallsign(partitionInProgressUpdated, completedPartitions, timeGapSecs)
      }
    }
  }

  override def partitions: Seq[(Option[String], Seq[R])] = completedPartitions :+ (None, partitionInProgress)
}

class StateAccumulatingWithCallsign[R <: HasPositionReportIdentifiers](partitionInProgress: Seq[R], callsign: String, completedPartitions: Seq[(Option[String], Seq[R])], timeGapSecs: Int /*TODO Is Int big enough?*/) extends ReportPartitionerState[R] {

  override def processReport(report: R): ReportPartitionerState[R] = {
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
      beginNewAccumulatingPartition(report, partitions, timeGapSecs)
    }
  }

  override def partitions: Seq[(Option[String], Seq[R])] = completedPartitions :+ (Some(callsign), partitionInProgress)
}

object ReportPartitionerStateUtils {
  def beginNewAccumulatingPartition[R <: HasPositionReportIdentifiers](report: R, completedPartitions: Seq[(Option[String], Seq[R])], timeGapSecs: Int): ReportPartitionerState[R] = {
    report.callsign.map { theCallsign =>
      new StateAccumulatingWithCallsign(Seq(report), theCallsign, completedPartitions, timeGapSecs)
    } getOrElse {
      new StateAccumulatingWithoutCallsign(Seq(report), completedPartitions, timeGapSecs)
    }
  }
}

object ReportPartitionerState {
  def initial[R <: HasPositionReportIdentifiers](timeGapSecs: Int): ReportPartitionerState[R] = new StateInitial(timeGapSecs)
}