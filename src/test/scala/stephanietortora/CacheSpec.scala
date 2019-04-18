package stephanietortora

import org.scalatest.{AsyncFlatSpec, FlatSpec, OptionValues}
import Cache._
import collection.immutable.SortedSet

case class TimedResult[V](result: V, time: Double)

class CacheSpec extends FlatSpec with OptionValues {
  val fib1024: BigInt = BigInt("4506699633677819813104383235728886049367860596218604830803023149600030645708721396248792609141030396244873266580345011219530209367425581019871067646094200262285202346655868899711089246778413354004103631553925405243")


  def time[V](f: => V): TimedResult[V] = {
    val start = System.currentTimeMillis()
    val result = f

    TimedResult(result, System.currentTimeMillis() - start)
  }


  def fib(n: BigInt): BigInt = {
    val zero = BigInt(0)

    def go(n: BigInt, acc: BigInt, x: BigInt): BigInt = n match {
      case `zero` => acc
      case _ => go(n - 1, x, acc + x)
    }

    go(n, 0, 1)

  }

  behavior of "LRU fib"
  val lruCache = Cache.lruCache[BigInt, BigInt](3, 5)

  it should "immediately return a cached result" in {

    val k = BigDecimal(Math.pow(2, 10)).toBigInt()

    val tr: TimedResult[BigInt] = time(lruCache.get(k) match { case Some(v) => v; case None => fib(k) })
    println(s"Un-cached time: ${tr.time}")
    assert(tr.result == fib1024)
    lruCache.put(k, tr.result)

    val tr2 = time(lruCache.get(k) match { case Some(v) => v; case None => fib(k) })
    println(s"Cached time: ${tr2.time}")
    assert(tr2.result == fib1024)

  }

  val lruCache2 = Cache.lruCache[Int, String](3, 5)
  it should "store up to numEntry vals per set" in {
    for (i <- 0 to 14) lruCache2.put(i, i.toString)
    for (y <- 0 to 14) {
      assert(lruCache2.get(y).isDefined); lruCache2.get(y).map(v => assert(v == y.toString))
    }
  }

  it should "replace oldest element in set when at capacity" in {

    lruCache2.put(15, "15")
    assert(lruCache2.get(15).isDefined)
    lruCache2.get(15).map(x => assert(x == "15"))
    assert(lruCache2.get(0).isEmpty)

  }

  it should "update timestamp on get" in {
    assert(lruCache2.get(1).isDefined) //update 1 timestamp
    lruCache2.put(16, "16")
    assert(lruCache2.get(1).isDefined)
    assert(lruCache2.get(4).isEmpty) //should have replaced oldest (4)

  }

  behavior of "MRU"

  val mruCache = Cache.mruCache[Int, String](3, 5)

  it should "store up to numEntry vals per set" in {
    for (i <- 0 to 14) mruCache.put(i, i.toString)
    for (y <- 0 to 14) {
      assert(mruCache.get(y).isDefined); mruCache.get(y).map(v => assert(v == y.toString))
    }
  }

  it should "replace newest element in set when at capacity" in {

    mruCache.put(15, "15")
    assert(mruCache.get(15).isDefined)
    mruCache.get(15).map(x => assert(x == "15"))
    assert(mruCache.get(12).isEmpty)

  }

  it should "update timestamp on get" in {
    assert(mruCache.get(1).isDefined) //update 1 timestamp
    mruCache.put(16, "16")
    assert(mruCache.get(1).isEmpty) //should have replaced 1 because its now newest

  }

  behavior of "cache with client implemented update"


  val update = (set: SortedSet[CacheEntryTime[Int, String]], ce: CacheEntryTime[Int, String]) => (set - set.min + ce, set.min)

  it should "prevent non-positive size" in {
    val c = cache[Int, String, CacheEntryTime[Int, String]](-1, 0, update, CacheEntryTime.apply, (ce: CacheEntryTime[Int,String]) => ce.copy(timeUpdated = System.nanoTime()))
    assert(c.numSet == 1 && c.numEntry == 1)
  }

  val badUpdate = (set: SortedSet[CacheEntryTime[Int, String]], ce: CacheEntryTime[Int, String]) => (set + ce, set.min)
  it should "prevent bad update" in {
    val c = cache[Int, String, CacheEntryTime[Int, String]](0, 0, badUpdate, CacheEntryTime.apply, (ce: CacheEntryTime[Int,String]) => ce.copy(timeUpdated = System.nanoTime()))
    c.put(0, "0")
    assertThrows[IllegalStateException](c.put(1,"1"))
  }
}
