package com.datastax.spark.connector.japi.rdd

import java.lang.Iterable

import com.datastax.spark.connector.NamedColumnRef
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.japi.CassandraJavaUtil
import com.datastax.spark.connector.rdd.{ReadConf, CassandraRDD}
import com.datastax.spark.connector.util.JavaApiHelper
import com.datastax.spark.connector.util.JavaApiHelper._
import org.apache.spark.api.java.JavaPairRDD
import org.apache.spark.api.java.function.Function

import scala.reflect.ClassTag

class CassandraJavaPairRDD[K, V](override val rdd: CassandraRDD[(K, V)])
                                (override implicit val kClassTag: ClassTag[K],
                                 override implicit val vClassTag: ClassTag[V])
  extends JavaPairRDD[K, V](rdd) with CassandraJavaPairRDDLike[K, V, CassandraJavaPairRDD[K, V]] {

  def this(rdd: CassandraRDD[(K, V)], keyClass: Class[K], valueClass: Class[V]) =
    this(rdd)(getClassTag(keyClass), getClassTag(valueClass))

  def wrap(rdd: CassandraRDD[(K, V)]) = {
    new CassandraJavaPairRDD[K, V](rdd)
  }

  override def select(columnName1: String, columnName2: String, columnNames: String*) = {
    wrap(rdd.select(
      (columnName1 :: columnName2 :: columnNames.toList).map(c => c: NamedColumnRef): _*))
  }

  override def select(columnName: String) = {
    wrap(rdd.select(columnName))
  }

  override def selectRefs(columnRef1: NamedColumnRef, columnRef2: NamedColumnRef, columnRefs: NamedColumnRef*) = {
    wrap(rdd.select(columnRef1 :: columnRef2 :: columnRefs.toList: _*))
  }

  override def selectRefs(columnRef: NamedColumnRef) = {
    wrap(rdd.select(columnRef))
  }

  override def where(cqlWhereClause: String, arg1: Any, arg2: Any, args: AnyRef*) = {
    wrap(rdd.where(cqlWhereClause, arg1 :: arg2 :: args.toList: _*))
  }

  override def where(cqlWhereClause: String, arg: Any) = {
    wrap(rdd.where(cqlWhereClause, arg))
  }

  override def where(cqlWhereClause: String) = {
    wrap(rdd.where(cqlWhereClause))
  }

  override def withAscOrder = {
    wrap(rdd.withAscOrder)
  }

  override def withDescOrder = {
    wrap(rdd.withDescOrder)
  }

  override def limit(rowsNumber: Long) = {
    wrap(rdd.limit(rowsNumber))
  }

  override def selectedColumnNames: Array[String] = {
    rdd.selectedColumnRefs.map(_.asInstanceOf[NamedColumnRef]).map(_.columnName).toArray
  }

  override def selectedColumnRefs: Array[NamedColumnRef] = {
    rdd.selectedColumnRefs.map(_.asInstanceOf[NamedColumnRef]).toArray
  }

  override def withConnector(connector: CassandraConnector) = {
    wrap(rdd.withConnector(connector))
  }

  override def withReadConf(config: ReadConf) = {
    wrap(rdd.withReadConf(config))
  }

  override def toEmptyCassandraRDD = {
    wrap(rdd.toEmptyCassandraRDD)
  }

  override def spanBy[K2](f: Function[(K, V), K2], keyClass: Class[K2]): JavaPairRDD[K2, Iterable[(K, V)]] = {
    CassandraJavaUtil.javaFunctions(this).spanBy(f)(JavaApiHelper.getClassTag(keyClass))
  }

  override def spanByKey(): JavaPairRDD[K, Iterable[V]] = {
    CassandraJavaUtil.javaFunctions(this).spanByKey()
  }
}

