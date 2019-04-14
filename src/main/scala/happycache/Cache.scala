package happycache

trait CacheEntry[K, V] extends Ordered[CacheEntry[K,V]] {
  val key: K
  val value: V
  val timeUpdated: Long

  implicit def compare(that: CacheEntry[K,V]): Int
  def apply(key: K, value: V, timeUpdated: Long): CacheEntry[K,V]
  def copy(key: K = this.key, value: V = this.value, timeUpdated: Long = this.timeUpdated): CacheEntry[K,V]
}

trait Cache[K, V] {
  def get(k: K): Option[V]

  def put(k: K, v: V): Unit
}

object Cache {


  private case class CacheEntryTime[K, V](key: K, value: V, timeUpdated: Long) extends CacheEntry[K, V] {
    def copy(key: K = this.key, value: V = this.value, timeUpdated: Long = this.timeUpdated): CacheEntryTime[K, V] = CacheEntryTime(key, value, timeUpdated)
    override def apply(key: K, value: V, timeUpdated: Long): CacheEntry[K, V] = CacheEntryTime(key, value, timeUpdated).asInstanceOf[CacheEntry[K,V]]
    implicit def compare(that: CacheEntry[K, V]): Int = this.timeUpdated.compareTo(that.timeUpdated)
  }


  private class CacheImpl[K, V]
  (numSet: Int,
   numEntry: Int,
   update: (collection.immutable.SortedSet[CacheEntry[K,V]], CacheEntry[K,V]) => (collection.immutable.SortedSet[CacheEntry[K,V]], CacheEntry[K,V]),
   newEntry: (K, V, Long) => CacheEntry[K,V])(implicit ordering: Ordering[CacheEntry[K,V]]) extends Cache[K, V] {
    type T = CacheEntry[K,V]

    private var cache = Vector.fill(numSet) {
      (collection.immutable.HashMap.empty[K, T], collection.immutable.SortedSet.empty[T])
    }

    def get(k: K): Option[V] = {


      val (map: collection.immutable.HashMap[K, T], set: collection.immutable.SortedSet[T]) = cache(k.hashCode() % numSet)
      map.get(k) match {
        case Some(ce: T) =>
          cache = cache.updated(k.hashCode() % numSet, (map, (set - ce) + ce.copy(timeUpdated = System.currentTimeMillis()))) //todo a lot of copying?
          Some(ce.value)
        case None => None

      }
    }

    def put(k: K, v: V): Unit = {
      val (map: collection.immutable.HashMap[K, T], set: collection.immutable.SortedSet[T]) = cache(k.hashCode() % numSet)
      map.get(k) match {
        case Some(ce) =>
        //noop
        case None =>
          val ce = newEntry(k, v, System.currentTimeMillis()) //todo fix to be cache entry trait
          if (map.size < numEntry) {

            cache = cache.updated(k.hashCode() % numSet, (map + (k -> ce), set + ce))

          } else {
            val (newSet, removeK) = update(set, ce)
            cache = cache.updated(k.hashCode() % numSet, (map - removeK.key + (k -> ce), newSet))
          }
      }
    }
  }
  def mruCache[K, V](numSet: Int, numEntry: Int)(implicit ordering: Ordering[CacheEntry[K,V]]):Cache[K,V] = {
    def newEntry: (K, V, Long) => CacheEntry[K,V] = (k: K, v: V, l: Long) => CacheEntryTime(k, v, l)

    def update(entries: collection.immutable.SortedSet[CacheEntry[K,V]], toReplace: CacheEntry[K,V]): (collection.immutable.SortedSet[CacheEntry[K,V]], CacheEntry[K,V]) = {
      (entries.init + toReplace, entries.last)

    }
    new CacheImpl[K, V](numSet, numEntry, update, newEntry)
  }




  def lruCache[K, V](numSet: Int, numEntry: Int)(implicit ordering: Ordering[CacheEntry[K,V]]):Cache[K,V] = {
    def newEntry: (K, V, Long) => CacheEntry[K,V] = (k: K, v: V, l: Long) => CacheEntryTime(k, v, l)

    def update(entries: collection.immutable.SortedSet[CacheEntry[K,V]], toReplace: CacheEntry[K,V]): (collection.immutable.SortedSet[CacheEntry[K,V]], CacheEntry[K,V]) = {
      (entries.tail + toReplace, entries.head)

    }
    new CacheImpl[K, V](numSet, numEntry, update, newEntry)
  }

}





