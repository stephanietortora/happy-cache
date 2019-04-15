package stephanietortora
import collection.immutable.{SortedSet, HashMap}


/**
  * An in-memory, n-way, set-associative cache
  * @tparam K key type
  * @tparam V value type
  *
  */
trait Cache[K, V] {

  /** Retrieves value stored for given key from cache
    *
    * @param k key to retrieve
    * @return Some(value) if present in cache, None if not present
    */
  def get(k: K): Option[V]

  /** Inserts key-value mapping into cache
    *
    * @param k key
    * @param v value
    */
  def put(k: K, v: V): Unit
}

/** An entry in a n-way, set-associative cache
  *
  * @tparam K key type
  * @tparam V value type
  */
trait CacheEntry[K, V] extends Ordered[CacheEntry[K, V]] {

  val key: K
  val value: V
  val timeUpdated: Long

  /** Implicit ordering of entries, used for sorting entries in a set
    *
    * @param that another CacheEntry
    * @return int representing result of comparing this CacheEntry with that CacheEntry
    */
  implicit def compare(that: CacheEntry[K, V]): Int

  /** Used to update cache entry when it is accessed
    *
    * @param key key
    * @param value value
    * @param timeUpdated timestamp in millis
    * @return a new instance of CacheEntry with field(s) updated
    */
  def copy(key: K = this.key, value: V = this.value, timeUpdated: Long = this.timeUpdated): CacheEntry[K, V]
}

/** Factory for [[stephanietortora.Cache]] instances */
object Cache {

  private case class CacheEntryTime[K, V](key: K, value: V, timeUpdated: Long) extends CacheEntry[K, V] {

    def copy(key: K = this.key, value: V = this.value, timeUpdated: Long = this.timeUpdated): CacheEntryTime[K, V] = CacheEntryTime(key, value, timeUpdated)
    implicit def compare(that: CacheEntry[K, V]): Int = this.timeUpdated.compareTo(that.timeUpdated)
  }

  private class CacheImpl[K, V]
  (numSet: Int,
   numEntry: Int,
   update: (SortedSet[CacheEntry[K, V]], CacheEntry[K, V]) => (SortedSet[CacheEntry[K, V]], CacheEntry[K, V]))
   extends Cache[K, V] {

    private var cache = Vector.fill(numSet) {
      (HashMap.empty[K, CacheEntry[K, V]], SortedSet.empty[CacheEntry[K, V]])
    }

    def get(k: K): Option[V] = {
      val (map: HashMap[K, CacheEntry[K, V]], set: SortedSet[CacheEntry[K, V]]) = cache(k.hashCode() % numSet)
      map.get(k) match {
        case Some(ce: CacheEntry[K, V]) =>
          cache = cache.updated(k.hashCode() % numSet, (map, (set - ce) + ce.copy(timeUpdated = System.currentTimeMillis()))) //todo a lot of copying?
          Some(ce.value)
        case None => None

      }
    }

    def put(k: K, v: V): Unit = {
      val (map: HashMap[K, CacheEntry[K, V]], set: SortedSet[CacheEntry[K, V]]) = cache(k.hashCode() % numSet)
      map.get(k) match {
        case Some(_) => //noop
        case None =>
          val ce = CacheEntryTime(k, v, System.currentTimeMillis())
          if (map.size < numEntry) {

            cache = cache.updated(k.hashCode() % numSet, (map + (k -> ce), set + ce))

          } else {
            val (newSet, removeK) = update(set, ce)
            cache = cache.updated(k.hashCode() % numSet, (map - removeK.key + (k -> ce), newSet))
          }
      }
    }
  }

  /** Factory for [[stephanietortora.Cache]] with Most Recently Used Replacement Policy
    *
    * Uses default [[stephanietortora.CacheEntry]] implementation
    *
    * @param numSet number of sets in the cache
    * @param numEntry number of entries per set in the cache
    * @tparam K key type
    * @tparam V value type
    * @return [[stephanietortora.Cache]]
    */
  def mruCache[K, V](numSet: Int, numEntry: Int): Cache[K, V] = {
    def update(entries: SortedSet[CacheEntry[K, V]], toReplace: CacheEntry[K, V]): (SortedSet[CacheEntry[K, V]], CacheEntry[K, V]) = {
      (entries.init + toReplace, entries.last)

    }
    cache(numSet, numEntry, update)
  }
  /** Factory for [[stephanietortora.Cache]] with Least Recently Used Replacement Policy
    *
    * Uses default [[stephanietortora.CacheEntry]] implementation
    *
    * @param numSet   number of sets in the cache
    * @param numEntry number of entries per set in the cache
    * @tparam K key type
    * @tparam V value type
    * @return [[stephanietortora.Cache]]
    */
  def lruCache[K, V](numSet: Int, numEntry: Int): Cache[K, V] = {
    def update(entries: SortedSet[CacheEntry[K, V]], toReplace: CacheEntry[K, V]): (SortedSet[CacheEntry[K, V]], CacheEntry[K, V]) = {
      (entries.tail + toReplace, entries.head)}

    cache(numSet, numEntry, update)
  }

  /** Factory for [[stephanietortora.Cache]] with replacement policy implemented by client
    *
    * Uses default [[stephanietortora.CacheEntry]] implementation
    *
    * @param numSet   number of sets in the cache
    * @param numEntry number of entries per set in the cache
    * @param update   replacement function implemented by client.
    *                 Takes a sorted set of entries and an entry to add. Returns updated sorted set and entry removed.
    * @tparam K key type
    * @tparam V value type
    * @return [[stephanietortora.Cache]]
    */
  def cache[K, V](numSet: Int,
                  numEntry: Int,
                  update: (SortedSet[CacheEntry[K, V]], CacheEntry[K, V]) => (SortedSet[CacheEntry[K, V]], CacheEntry[K, V]))
                  : Cache[K, V] = new CacheImpl(numSet, numEntry, update)


}





