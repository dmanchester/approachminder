package com.dmanchester.approachminder

/**
 * Trait for the possible states of a reports partitioner.
 *
 * This trait and its implementing classes are an implementation detail of ReportUtils.partition(). See that method's
 * tests for coverage of this file's code.
 *
 * @tparam R The reports' type.
 */
trait PartitionerState[R <: HasPositionReportIdentifiers] {

  def processReport(report: R): PartitionerState[R]

  def partitionedReports: Seq[(Option[String], Seq[R])]

  // TODO Is there a better place to put this utility method? (Some object?)
  protected def beginAccumlatingNewSeq[R1 <: HasPositionReportIdentifiers](report: R1, completedSeqs: Seq[(Option[String], Seq[R1])], timeGapSecs: Int): PartitionerState[R1] = {
    report.callsign.map { theCallsign =>
      new StateAccumulatingWithCallsign(Seq(report), theCallsign, completedSeqs, timeGapSecs)
    } getOrElse {
      new StateAccumulatingWithoutCallsign(Seq(report), completedSeqs, timeGapSecs)
    }
  }
}

class StateInitial[R <: HasPositionReportIdentifiers](timeGapSecs: Int) extends PartitionerState[R] {

  override def processReport(report: R): PartitionerState[R] = {
    beginAccumlatingNewSeq(report, Seq.empty, timeGapSecs)
  }

  override def partitionedReports: Seq[(Option[String], Seq[R])] = Seq.empty
}

class StateAccumulatingWithoutCallsign[R <: HasPositionReportIdentifiers](seqInProgress: Seq[R], completedSeqs: Seq[(Option[String], Seq[R])], timeGapSecs: Int) extends PartitionerState[R] {

  override def processReport(report: R): PartitionerState[R] = {

    if (report.timePosition - seqInProgress.last.timePosition >= timeGapSecs) {
      beginAccumlatingNewSeq(report, partitionedReports, timeGapSecs)
    } else {
      val seqInProgressUpdated = seqInProgress :+ report
      report.callsign.map { theCallsign =>
        new StateAccumulatingWithCallsign(seqInProgressUpdated, theCallsign, completedSeqs, timeGapSecs)
      } getOrElse {
        new StateAccumulatingWithoutCallsign(seqInProgressUpdated, completedSeqs, timeGapSecs)
      }
    }
  }

  override def partitionedReports: Seq[(Option[String], Seq[R])] = completedSeqs :+ (None, seqInProgress)
}

class StateAccumulatingWithCallsign[R <: HasPositionReportIdentifiers](seqInProgress: Seq[R], callsign: String, completedSeqs: Seq[(Option[String], Seq[R])], timeGapSecs: Int /*TODO Is Int big enough?*/) extends PartitionerState[R] {

  override def processReport(report: R): PartitionerState[R] = {
    // Check for two criteria:
    //
    //   * whether the new report has the same callsign as the sequence in progress, or has no callsign; and
    //   * whether the time gap between the new report and the sequence in progress's last report isn't large enough to
    //     warrant partitioning.
    if ((report.callsign.contains(callsign) || report.callsign.isEmpty) &&
      report.timePosition - seqInProgress.last.timePosition < timeGapSecs) {
      // The above two criteria are met. Append the new report to the sequence in progress.
      val seqInProgressUpdated = seqInProgress :+ report
      new StateAccumulatingWithCallsign(seqInProgressUpdated, callsign, completedSeqs, timeGapSecs)
    } else {
      // At least one of the above two criteria are not met. Treat the sequence in progress as complete and begin a new
      // one.
      beginAccumlatingNewSeq(report, partitionedReports, timeGapSecs)
    }
  }

  override def partitionedReports: Seq[(Option[String], Seq[R])] = completedSeqs :+ (Some(callsign), seqInProgress)
}

object PartitionerState {
  def initial[R <: HasPositionReportIdentifiers](timeGapSecs: Int): PartitionerState[R] = new StateInitial(timeGapSecs)
}