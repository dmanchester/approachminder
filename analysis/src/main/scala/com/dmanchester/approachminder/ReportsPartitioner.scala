package com.dmanchester.approachminder

private trait ReportsPartitioner[R <: HasPositionReportIdentifiers] {

  def processReport(report: R): ReportsPartitioner[R]

  def partitionedReports: Seq[(Option[String], Seq[R])]

  // TODO Is there a better place to put this utility method? (Some object?)
  protected def beginAccumlatingSeq[R1 <: HasPositionReportIdentifiers](report: R1, completedSeqs: Seq[(Option[String], Seq[R1])], timeGapForPartitioning: Int): ReportsPartitioner[R1] = {
    report.callsign.map { theCallsign =>
      new PartitionerAccumulatingWithCallsignState(Seq(report), theCallsign, completedSeqs, timeGapForPartitioning)
    } getOrElse {
      new PartitionerAccumulatingWithoutCallsignState(Seq(report), completedSeqs, timeGapForPartitioning)
    }
  }

}

// TODO For class params, "val" vs. "public val" vs ...?

private class PartitionerInitState[R <: HasPositionReportIdentifiers](val timeGapForPartitioning: Int) extends ReportsPartitioner[R] {

  override def processReport(report: R): ReportsPartitioner[R] = {
    beginAccumlatingSeq(report, Seq.empty, timeGapForPartitioning)
  }

  override def partitionedReports: Seq[(Option[String], Seq[R])] = Seq.empty
}

private class PartitionerAccumulatingWithoutCallsignState[R <: HasPositionReportIdentifiers](val seqInProgress: Seq[R], val completedSeqs: Seq[(Option[String], Seq[R])], val timeGapForPartitioning: Int) extends ReportsPartitioner[R] {

  override def processReport(report: R): ReportsPartitioner[R] = {

    if (report.timePosition - seqInProgress.last.timePosition >= timeGapForPartitioning) {
      val completedSeqsUpdated = partitionedReports
      beginAccumlatingSeq(report, completedSeqsUpdated, timeGapForPartitioning)
    } else {
      val seqInProgressUpdated = seqInProgress :+ report
      report.callsign.map { theCallsign =>
        new PartitionerAccumulatingWithCallsignState(seqInProgressUpdated, theCallsign, completedSeqs, timeGapForPartitioning)
      } getOrElse {
        new PartitionerAccumulatingWithoutCallsignState(seqInProgressUpdated, completedSeqs, timeGapForPartitioning)
      }
    }
  }

  override def partitionedReports: Seq[(Option[String], Seq[R])] = completedSeqs :+ (None, seqInProgress)
}

private class PartitionerAccumulatingWithCallsignState[R <: HasPositionReportIdentifiers](val seqInProgress: Seq[R], val callsign: String, val completedSeqs: Seq[(Option[String], Seq[R])], val timeGapForPartitioning: Int /*TODO Is Int big enough?*/) extends ReportsPartitioner[R] {

  override def processReport(report: R): ReportsPartitioner[R] = {
    // Check for two criteria:
    //
    //   * whether the new report has the same callsign as the sequence in progress, or has no callsign; and
    //   * whether the time gap between the new report and the sequence in progress's last report isn't large enough to
    //     warrant partitioning.
    if ((report.callsign.contains(callsign) || report.callsign.isEmpty) &&
      report.timePosition - seqInProgress.last.timePosition < timeGapForPartitioning) {
      // The above two criteria are met. Append the new report to the sequence in progress.
      val seqInProgressUpdated = seqInProgress :+ report
      new PartitionerAccumulatingWithCallsignState(seqInProgressUpdated, callsign, completedSeqs, timeGapForPartitioning)
    } else {
      // At least one of the above two criteria are not met. Treat the sequence in progress as complete (the natural
      // behavior of partitionedReports) and begin a new sequence.
      val completedSeqsUpdated = partitionedReports
      beginAccumlatingSeq(report, completedSeqsUpdated, timeGapForPartitioning)
    }
  }

  override def partitionedReports: Seq[(Option[String], Seq[R])] = completedSeqs :+ (Some(callsign), seqInProgress)
}

object ReportsPartitioner {

  private def apply[R <: HasPositionReportIdentifiers](timeGapForPartitioning: Int): ReportsPartitioner[R] = new PartitionerInitState(timeGapForPartitioning)

  def partitionOnTimeGapAndCallsignChange[R <: HasPositionReportIdentifiers](reports: Iterable[R], timeGap: Int): Seq[(Option[String], Seq[R])] = {
    reports.foldLeft(ReportsPartitioner[R](timeGap)) { (partitioner, report) =>
      partitioner.processReport(report)
    }.partitionedReports
  }
}