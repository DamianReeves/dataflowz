package dataflowz.samples.accounting.common.data

mport scala.reflect.runtime.universe.TypeTag

object table {
  import Table.Column
  final case class Table(columns:List[Column]) {
    def columnNames:List[String] = columns.map(_.name)
  }

  object Table {

    val empty: Table = Table(List.empty)

    def withColumns(column:Column, others:Column*):Table = Table(column::others.toList)
    def of[A <: Product : TypeTag]:Table = ???

    final case class Column(name:String, value:DataValue)

    sealed abstract class DataType extends Product with Serializable
    object DataType {
      case object NA extends DataType
      case object Str extends DataType
      case object Decimal extends DataType
      case object Integer extends DataType
    }
    sealed abstract class DataValue extends Product with Serializable {
      def dataType:DataType
      def coerce(otherType:DataType):Option[DataValue]
    }

    object DataValue {
      case object NA extends DataValue { self =>
        val dataType:DataType = DataType.NA

        def coerce(otherType: DataType): Option[DataValue] = otherType match {
          case DataType.NA => Some(self)
          case _ => None
        }
      }

      final case class Str(value:String) extends DataValue { self =>
        val dataType:DataType = DataType.Str

        def coerce(otherType: DataType): Option[DataValue] = otherType match {
          case DataType.NA => Some(DataValue.NA)
          case DataType.Str => Some(self)
          case _ => None
        }
      }
    }
  }
}
