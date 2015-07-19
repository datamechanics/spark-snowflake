package com.databricks.spark.redshift

import org.scalatest.{Matchers, FunSuite}

/**
 * Check validation of parameter config
 */
class ParametersSuite extends FunSuite with Matchers {

  test("Minimal valid parameter map is accepted") {
    val params =
      Map(
        "tempdir" -> "s3://foo/bar",
        "redshifttable" -> "test_table",
        "jdbcurl" -> "jdbc:postgresql://foo/bar")

    val mergedParams = Parameters.mergeParameters(params)

    mergedParams.tempPath should startWith (params("tempdir"))
    mergedParams.jdbcUrl shouldBe params("jdbcurl")
    mergedParams.table shouldBe params("redshifttable")

    // Check that the defaults have been added
    Parameters.DEFAULT_PARAMETERS foreach {
      case (key, value) => mergedParams.parameters(key) shouldBe value
    }
  }

  test("New instances have distinct temp paths") {
    val params =
      Map(
        "tempdir" -> "s3://foo/bar",
        "redshifttable" -> "test_table",
        "jdbcurl" -> "jdbc:postgresql://foo/bar")

    val mergedParams1 = Parameters.mergeParameters(params)
    val mergedParams2 = Parameters.mergeParameters(params)

    mergedParams1.tempPath should not equal mergedParams2.tempPath
  }

  test("Errors are thrown when mandatory parameters are not provided") {

    def checkMerge(params: Map[String, String]): Unit = {
      intercept[Exception] {
        Parameters.mergeParameters(params)
      }
    }

    checkMerge(Map("redshifttable" -> "test_table", "jdbcurl" -> "jdbc:postgresql://foo/bar"))
    checkMerge(Map("tempdir" -> "s3://foo/bar", "jdbcurl" -> "jdbc:postgresql://foo/bar"))
    checkMerge(Map("redshifttable" -> "test_table", "tempdir" -> "s3://foo/bar"))
  }
}
