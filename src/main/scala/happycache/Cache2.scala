package happycache

import java.util.NoSuchElementException

import scala.collection.immutable
import scala.collection.immutable.{HashMap, Seq, Queue, Stack}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

//trait CacheEntry[K, V] { self: { def copy(key: K= this.key, futureVal: Future[V] = this.futureVal, timeUpdated: Long = this.timeUpdated): CacheEntry[K,V] } =>
//  val key: K
//  val futureVal: Future[V]
//  val timeUpdated: Long
//
//  def apply(key: K, futureVal: Future[V], timeUpdated: Long): CacheEntry[K, V]
//}

case class CacheEntry[K, F](key: K, futureVal: F, timeUpdated: Long)

class Cache[K, F,  T <: Seq[CacheEntry[K, F]], U <: IndexedSeq[T]]
(numSet: Int,
 numEntry: Int,
 var cache: U,
 f: K => F,
// c: (K, Future[V], Long) => C,
 update: (T, CacheEntry[K, F]) => T,
 replace: (T, CacheEntry[K, F]) => T) {

  def apply(k: K): F = {

    val entries = cache(k.hashCode() % numSet)
    entries.find { t => t.key == k } match {
      case Some(ce) =>
        cache = cache.updated(k.hashCode() % numSet, update(entries, ce)).asInstanceOf[U]
        ce.futureVal
      case None =>
        val r: F = f(k)
        cache = cache.updated(k.hashCode() % numSet, replace(entries, CacheEntry(k, r, System.currentTimeMillis()))).asInstanceOf[U]
        r
    }
  }
}

//trait VectorCache[K, V, C <: CacheEntry[K, Future[V]], T <: Seq[C]] extends Cache[K, V, C, T, Vector[T]] {
//  override var cache: Vector[T]
//}

//class QueueVectorCache[K, V, C <: CacheEntry[K, Future[V]]]
//(numSet: Int,
// numEntry: Int,
// override var cache: Vector[Queue[C]],
// f: K => Future[V],
//// c: (K, Future[V], Long) => C,
// update: (Queue[C], C) => Queue[C],
// replace: (Queue[C], C) => Queue[C]) extends Cache(numSet, numEntry, cache, f, update, replace)


object Cache {
  type Replace[K, V] = (Seq[CacheEntry[K, Future[V]]], CacheEntry[K, Future[V]]) => Seq[CacheEntry[K, Future[V]]]

  def futurify[K, V](f: K => V)(implicit ec: ExecutionContext): K => Future[V] = (k: K) => Future(f(k))


  def lruCache[K, F](numSet: Int, numEntry: Int, f: K => F)(implicit ec: ExecutionContext): Cache[K, F, Queue[CacheEntry[K, F]], Vector[Queue[CacheEntry[K, F]]]] =
    {

      var cache = Vector.fill(numSet) {
        Queue.empty[CacheEntry[K, F]]
      }

//      def c(k: K, f: Future[V], t: Long) = CacheEntry[K,V](k, f, t)

      def replace(entries: Queue[CacheEntry[K, F]], replaceWith: CacheEntry[K, F]) = {
        val e = entries.enqueue(replaceWith)
        if (e.size > numEntry) e.dequeue._2
        else e
      }

      def update(entries: Queue[CacheEntry[K, F]], toReplace: CacheEntry[K, F]) = {
        toReplace.copy(timeUpdated = System.currentTimeMillis()) +: entries.filterNot(_ == toReplace)

      }

      new Cache[K, F, Queue[CacheEntry[K, F]], Vector[Queue[CacheEntry[K, F]]]](numSet, numEntry, cache, f, update, replace)

    }

  //  def mruCache[K, V](numSet: Int, numEntry: Int, f: K => Future[V])(implicit ec: ExecutionContext): Cache[K, V] = {
  //    val queues: Vector[List[(K, Future[V])]] = Vector.fill(numSet) {
  //      immutable.List.empty[(K, Future[V])]
  //    }
  //
  //    val get: K => Future[V] =
  //      k => {
  //        val queue = queues(k.hashCode() % numSet)
  //        queue.find { t => t._1 == k } match {
  //          case Some((_, v)) => v;
  //          case None => Future.failed(new NoSuchElementException())
  //        }
  //      }
  //
  //
  //    val put: (K, Future[V]) => Unit =
  //      (k: K, v: Future[V]) => {
  //        var q: List[(K, Future[V])] = queues(k.hashCode() % numSet)
  //        q = (k, v) :: q
  //
  //        if (q.size > numEntry) {
  //          queues(k.hashCode() % numSet) = q tail
  //        }
  //      }
  //
  //    cache((k: K) => get(k), f, put)
  //
  //  }

//  def cache[K, V, C <: CacheEntry[K, Future[V]], T <: Seq[C], U <: IndexedSeq[T]]
//  ()
//  (
//    implicit ec: ExecutionContext
//  ): Cache[K, V] =




}





