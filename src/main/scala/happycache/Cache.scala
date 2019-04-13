//package happycache
//
//import java.util.NoSuchElementException
//
//import scala.collection.immutable
//import scala.collection.immutable.{HashMap, Seq, Queue, Stack}
//import scala.concurrent.{ExecutionContext, Future}
//import scala.util.Success
//
////trait CacheEntry[K, V] { self: { def copy(key: K= this.key, futureVal: Future[V] = this.futureVal, timeUpdated: Long = this.timeUpdated): CacheEntry[K,V] } =>
////  val key: K
////  val futureVal: Future[V]
////  val timeUpdated: Long
////
////  def apply(key: K, futureVal: Future[V], timeUpdated: Long): CacheEntry[K, V]
////}
//
//case class CacheEntry[K, V](key: K, futureVal: Future[V], timeUpdated: Long)
//
//trait Cache[K, V, C <: CacheEntry[K, V], T <: Seq[C], U <: IndexedSeq[T]] {
//  // update: Replace[K, V],
//  // replace: Replace[K, V]){
//  val numSet: Int
//  val numEntry: Int
//  var cache: U
//
//  def f: K => Future[V]
//
//  def c: (K, Future[V], Long) => C
//
//  def update: (T, C) => T
//
//  def replace: (T, C) => T
//
//  def apply(k: K): Future[V] = {
//
//    val entries = cache(k.hashCode() % numSet)
//    entries.find { t => t.key == k } match {
//      case Some(ce) =>
//        cache = cache.updated(k.hashCode() % numSet, update(entries, ce)).asInstanceOf[U]
//        ce.futureVal
//      case None =>
//        val r: Future[V] = f(k)
//        cache = cache.updated(k.hashCode() % numSet, replace(entries, c(k, r, System.currentTimeMillis()))).asInstanceOf[U]
//        r
//    }
//  }
//}
//
//trait VectorCache[K, V, C <: CacheEntry[K, Future[V]], T <: Seq[C]] extends Cache[K, V, C, T, Vector[T]] {
//  override var cache: Vector[T]
//}
//
//trait QueueVectorCache[K, V, C <: CacheEntry[K, Future[V]]] extends VectorCache[K, V, C, Queue[C]] {
//  override var cache: Vector[Queue[C]]
//}
//
//
//object Cache {
//  type Replace[K, V] = (Seq[CacheEntry[K, Future[V]]], CacheEntry[K, Future[V]]) => Seq[CacheEntry[K, Future[V]]]
//
//  def futurify[K, V](f: K => V)(implicit ec: ExecutionContext): K => Future[V] = (k: K) => Future(f(k))
//
//  private class LRUCache extends
//
//  def lruCache[K, V](numSet: Int, numEntry: Int, f: K => Future[V])(implicit ec: ExecutionContext): QueueVectorCache[K, V, CacheEntry[K, Future[V]]] =
//    new QueueVectorCache[K, V, CacheEntry[K, Future[V]]] {
//
//    var cache = Vector.fill(numSet) { Queue.empty[CacheEntry[K,Future[V]]] }
//
//    def c(k: K, f: Future[V], t: Long) = CacheEntry(k, f, t)
//
//    def replace(entries: Queue[CacheEntry[K, Future[V]]], replaceWith: CacheEntry[K, Future[V]]) ={
//        val e = entries.enqueue(replaceWith)
//        if (e.size > numEntry) e.dequeue._2
//        else e
//    }
//
//    def update(entries: Queue[CacheEntry[K, Future[V]]], toReplace: CacheEntry[K, Future[V]] ) = {
//        toReplace.copy(timeUpdated = System.currentTimeMillis()) +: entries.filterNot(_ == toReplace)
//
//    }
//
//
//  }
//  def qcache[K,V](numSet: Int, numEntry: Int, f: K => Future[V], )
//
//  //  def mruCache[K, V](numSet: Int, numEntry: Int, f: K => Future[V])(implicit ec: ExecutionContext): Cache[K, V] = {
//  //    val queues: Vector[List[(K, Future[V])]] = Vector.fill(numSet) {
//  //      immutable.List.empty[(K, Future[V])]
//  //    }
//  //
//  //    val get: K => Future[V] =
//  //      k => {
//  //        val queue = queues(k.hashCode() % numSet)
//  //        queue.find { t => t._1 == k } match {
//  //          case Some((_, v)) => v;
//  //          case None => Future.failed(new NoSuchElementException())
//  //        }
//  //      }
//  //
//  //
//  //    val put: (K, Future[V]) => Unit =
//  //      (k: K, v: Future[V]) => {
//  //        var q: List[(K, Future[V])] = queues(k.hashCode() % numSet)
//  //        q = (k, v) :: q
//  //
//  //        if (q.size > numEntry) {
//  //          queues(k.hashCode() % numSet) = q tail
//  //        }
//  //      }
//  //
//  //    cache((k: K) => get(k), f, put)
//  //
//  //  }
//
//  def cache[K, V, C <: CacheEntry[K, Future[V]], T <: Seq[C], U <: IndexedSeq[T]]
//
//  (
//  implicit ec: ExecutionContext
//  ): Cache[K, V] =
//
//
//}
//
//}
//




