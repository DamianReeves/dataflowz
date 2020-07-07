package dataflowz.samples.accounting.common.data

sealed trait Table[+A] {
  def columnNames:List[String]
}

object Table {

  case object Empty extends Table[Nothing] {
    val columnNames:List[String] = List.empty
  }

  val empty: Table[Unit] = Empty
}

final case class TableColumn[-R, +A](name:String, get: Table => A, render:Table => String)
