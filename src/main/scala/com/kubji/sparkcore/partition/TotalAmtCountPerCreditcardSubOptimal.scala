package com.kubji.sparkcore.partition

import com.kubji.sparkcore.CreditcardTransaction
import org.apache.spark.sql.SparkSession


object TotalAmtCountPerCreditcardSubOptimal {

  def main(args: Array[String]) {

    val masterOfCluster = args(0)
    val inputPath = args(1)
    val totalAmtPerCredicardOutput = args(2)


    val sparkSession = SparkSession
      .builder()
      .master(masterOfCluster)
      .appName("Load Credit card data")
      .config("spark.some.config.option", "some-value")
      .getOrCreate()


    val dataRdd = sparkSession.sparkContext.textFile(inputPath)
    val transactionRdd = dataRdd.map(CreditcardTransaction.parse)

    val genuineTransactionRdd = transactionRdd.filter(_.isFraud == 0)


    val partitionedPairRdd = genuineTransactionRdd.map(transaction => {
      (transaction.cc_num, transaction.amt)
    }).cache()

    val totalCountPerCreditcard = partitionedPairRdd.countByKey()

    totalCountPerCreditcard.foreach(println)

    val totalAmtPerCreditcard = partitionedPairRdd.reduceByKey(_ + _)
    totalAmtPerCreditcard.saveAsTextFile(totalAmtPerCredicardOutput)

  }
}