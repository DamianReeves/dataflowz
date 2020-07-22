package dataflowz

trait SequenceProvider[A] extends Serializable { self =>
  type Sequence
  def next(value:A):Sequence
}

object SequenceProvider {
  type Aux[A0,Sequence0] = SequenceProvider[A0] { type Sequence =  Sequence0}

  def intSequenceProvider[A](getCurrent: A => Int):Aux[A,Int] =
    intSequenceProvider(getCurrent, (_,i) => i + 1)

  def intSequenceProvider[A](getCurrent: A => Int, getNext: (A, Int) => Int):Aux[A,Int] = new SequenceProvider[A] {
    type Sequence = Int
    def next(value:A):Sequence  =
      getNext(value, getCurrent(value))
  }

  def longSequenceProvider[A](getCurrent: A => Long):Aux[A,Long] =
    longSequenceProvider(getCurrent,  (_,i) => i + 1)

  def longSequenceProvider[A](getCurrent: A => Long, getNext: (A, Long) => Long):Aux[A,Long] = new SequenceProvider[A] {
    type Sequence = Long
    def next(value:A):Sequence  =
      getNext(value, getCurrent(value))
  }
}
