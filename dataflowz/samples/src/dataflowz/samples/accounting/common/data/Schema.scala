package dataflowz.samples.accounting.common.data

import org.apache.spark.sql.types.StructType

final case class Schema(f: () => StructType) {}

//https://stackoverflow.com/questions/16079113/scala-2-10-reflection-how-do-i-extract-the-field-values-from-a-case-class-i-e/16079804#16079804
//https://svejcar.dev/posts/2019/10/22/extracting-case-class-field-names-with-shapeless/
