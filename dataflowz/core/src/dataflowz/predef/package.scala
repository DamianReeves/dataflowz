package dataflowz

package object predef {
  type Reader[-R, +E, +A] = Flowz[Any, Any, R, E, A]
  //type RReader[-R, +A] = Flowz[Any, Any, R, Throwable, A]
}
