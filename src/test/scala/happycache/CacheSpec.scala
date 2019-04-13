package happycache

import org.scalatest.AsyncFlatSpec

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

case class TimedResult[V](result: V, time: Double)

class CacheSpec extends AsyncFlatSpec {
  val fib1024: BigInt = BigInt("4506699633677819813104383235728886049367860596218604830803023149600030645708721396248792609141030396244873266580345011219530209367425581019871067646094200262285202346655868899711089246778413354004103631553925405243")


  def time[V](f: => V): TimedResult[V] = {
    val start = System.currentTimeMillis()
    val result = f

    TimedResult(result, System.currentTimeMillis() - start)
  }


  def timedFuture[V](f: => Future[V]) = {
    val start = System.currentTimeMillis()
    f.onComplete({
      case _ => println(s"Future took ${System.currentTimeMillis() - start} ms")
    })
  }

  def fib(n: BigInt): BigInt = {
    val zero = BigInt(0)
    def go(n: BigInt, acc: BigInt, x: BigInt): BigInt = n match {
      case `zero` => acc
      case _ => go(n - 1, x, acc + x)
    }
    go(n, 0, 1)

  }


  behavior of "fib"

  it should "immediately compute fib(0)" in {
    assert(fib(BigInt(0)) == BigInt(0))
  }

  it should "slowly compute big fib" in {

    val tr:TimedResult[BigInt] = time(fib(BigDecimal(Math.pow(2, 10)).toBigInt()))
    print(s"time: ${tr.time}")

    assert( tr.result == fib1024)

  }


  behavior of "fib and caching"

  val lruCache = Cache.lruCache[BigInt, Future[BigInt]](16384, 16384, Cache.futurify[BigInt,BigInt](fib))
//  val mruCache = Cache.mruCache[BigInt, BigInt](16384, 16384, Cache.futurify[BigInt,BigInt](fib))

  it should "eventually compute fib of big number" in {

    timedFuture[BigInt](lruCache(BigDecimal(Math.pow(2, 10)).toBigInt()))
//    print(s"time: ${tr.time}")
    lruCache(BigDecimal(Math.pow(2, 10)).toBigInt()).map((n:BigInt) => assert( n == fib1024))
//    mruCache(BigDecimal(Math.pow(2, 10)).toBigInt()).map((n:BigInt) => assert( n == fib1024))

  }
}
