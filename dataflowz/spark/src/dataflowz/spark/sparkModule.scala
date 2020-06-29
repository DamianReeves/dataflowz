package dataflowz.spark

import org.apache.spark.sql.SparkSession
import zio._
object sparkModule {

  object SparkModule {}

  def testSession(
      configure: SparkSession.Builder => SparkSession.Builder
  ): TaskLayer[Has[SparkSession]] =
    ZLayer.fromAcquireRelease(ZIO.effect {
      val builder = SparkSession
        .builder()
        .appName("test")
        .config("spark.ui.enabled", "false")
        .config("spark.sql.codegen.wholeStage", "false")
        .config("spark.sql.ui.retainedExecutions", "0")
        .config("spark.ui.retainedJobs", "0")
        .config("spark.ui.retainedStages", "0")
        .config("spark.executor.memory", "2G")
        .config("spark.driver.memory", "2G")
        .config("spark.sql.shuffle.partitions", 1)
        .config("spark.cleaner.referenceTracking.blocking", "false")
        .master("local[*")

      configure(builder).getOrCreate().newSession()
    }) { session => ZIO.effect(session.close()).orDie }

}
