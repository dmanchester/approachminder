package com.dmanchester.approachminder

private trait ReportsPartitioner[R <: HasPositionReportIdentifiers] {

  def processReport(report: R): ReportsPartitioner[R]

  def partitionedReports: Seq[(Option[String], Seq[R])]

  // TODO Is there a better place to put this utility method? (Some object?)
  protected def beginAccumlatingNewSeq[R1 <: HasPositionReportIdentifiers](report: R1, completedSeqs: Seq[(Option[String], Seq[R1])], timeGapSecs: Int): ReportsPartitioner[R1] = {
    report.callsign.map { theCallsign =>
      new PartitionerAccumulatingWithCallsign(Seq(report), theCallsign, completedSeqs, timeGapSecs)
    } getOrElse {
      new PartitionerAccumulatingWithoutCallsign(Seq(report), completedSeqs, timeGapSecs)
    }
  }
}

private class PartitionerInitial[R <: HasPositionReportIdentifiers](timeGapSecs: Int) extends ReportsPartitioner[R] {

  override def processReport(report: R): ReportsPartitioner[R] = {
    beginAccumlatingNewSeq(report, Seq.empty, timeGapSecs)
  }

  override def partitionedReports: Seq[(Option[String], Seq[R])] = Seq.empty
}

private class PartitionerAccumulatingWithoutCallsign[R <: HasPositionReportIdentifiers](seqInProgress: Seq[R], completedSeqs: Seq[(Option[String], Seq[R])], timeGapSecs: Int) extends ReportsPartitioner[R] {

  override def processReport(report: R): ReportsPartitioner[R] = {

    if (report.timePosition - seqInProgress.last.timePosition >= timeGapSecs) {
      beginAccumlatingNewSeq(report, partitionedReports, timeGapSecs)
    } else {
      val seqInProgressUpdated = seqInProgress :+ report
      report.callsign.map { theCallsign =>
        new PartitionerAccumulatingWithCallsign(seqInProgressUpdated, theCallsign, completedSeqs, timeGapSecs)
      } getOrElse {
        new PartitionerAccumulatingWithoutCallsign(seqInProgressUpdated, completedSeqs, timeGapSecs)
      }
    }
  }

  override def partitionedReports: Seq[(Option[String], Seq[R])] = completedSeqs :+ (None, seqInProgress)
}

private class PartitionerAccumulatingWithCallsign[R <: HasPositionReportIdentifiers](seqInProgress: Seq[R], callsign: String, completedSeqs: Seq[(Option[String], Seq[R])], timeGapSecs: Int /*TODO Is Int big enough?*/) extends ReportsPartitioner[R] {

  override def processReport(report: R): ReportsPartitioner[R] = {
    // Check for two criteria:
    //
    //   * whether the new report has the same callsign as the sequence in progress, or has no callsign; and
    //   * whether the time gap between the new report and the sequence in progress's last report isn't large enough to
    //     warrant partitioning.
    if ((report.callsign.contains(callsign) || report.callsign.isEmpty) &&
      report.timePosition - seqInProgress.last.timePosition < timeGapSecs) {
      // The above two criteria are met. Append the new report to the sequence in progress.
      val seqInProgressUpdated = seqInProgress :+ report
      new PartitionerAccumulatingWithCallsign(seqInProgressUpdated, callsign, completedSeqs, timeGapSecs)
    } else {
      // At least one of the above two criteria are not met. Treat the sequence in progress as complete and begin a new
      // one.
      beginAccumlatingNewSeq(report, partitionedReports, timeGapSecs)
    }
  }

  override def partitionedReports: Seq[(Option[String], Seq[R])] = completedSeqs :+ (Some(callsign), seqInProgress)
}

object ReportsPartitioner {

  private def apply[R <: HasPositionReportIdentifiers](timeGapSecs: Int): ReportsPartitioner[R] = new PartitionerInitial(timeGapSecs)

  /**
   * Partition reports from a single aircraft on a time gap or callsign change.
   *
   * @param reports The reports to partition.
   * @param timeGapSecs The time gap in seconds.
   * @tparam R The reports' type.
   * @return The partitioned reports.
   */
  def partition[R <: HasPositionReportIdentifiers](reports: Iterable[R], timeGapSecs: Int): Seq[(Option[String], Seq[R])] = {
    reports.foldLeft(ReportsPartitioner[R](timeGapSecs)) { (partitioner, report) =>
      partitioner.processReport(report)
    }.partitionedReports
  }
}