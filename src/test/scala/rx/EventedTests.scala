package rx

import org.scalatest.FreeSpec
import concurrent.duration._
import scala.concurrent.ExecutionContext
import org.scalatest.concurrent.Eventually._

import rx.core.Propagator
import rx.ops._


/**
 * Tests cases where the Rxs are able to give off events and begin propagations
 * entirely on their own. Timers, Delays, Debounces, etc.
 */
class EventedTests extends FreeSpec {
  implicit val patience = PatienceConfig(1 second)
  implicit val prop = Propagator.Immediate
  implicit val executionContext = new ExecutionContext {
    def reportFailure(t: Throwable) { t.printStackTrace() }
    def execute(runnable: Runnable) {runnable.run()}
  }
  implicit val scheduler = new TestScheduler()
  "EventedTests"- {
    "a Timer" - {
      "should work properly and give off events on its own" in {
        val t = Timer(100 millis)
        var count = 0
        val o = Obs(t){
          count = count + 1
        }

        for(i <- 3 to 5){
          eventually{ assert(t() == i) }(patience)
        }

        assert(count >= 5)
      }

    }


    "debounce" - {
      "simple" in {
        val a = Var(10)
        val b = a.debounce(100 millis)
        a() = 5
        assert(b() == 5)

        a() = 2
        assert(b() == 5)

        eventually{
          assert(b() == 2)
        }(patience)

        a() = 1
        assert(b() == 2)

        eventually{
          assert(b() == 1)
        }(patience)
      }
      "longer" in {
        val a = Var(10)
        val b = a.debounce(200 millis)
        val c = Rx( a() * 2 ).debounce(200 millis)
        var count = 0
        val o = Obs(b){ count += 1 }
        a() = 5
        assert(b() == 5)
        assert(c() == 10)

        a() = 2
        assert(b() == 5)
        assert(c() == 10)

        a() = 7
        assert(b() == 5)
        assert(c() == 10)

        eventually{
          assert(b() == 7)
          assert(c() == 14)
        }(patience)

        a() = 1
        assert(b() == 7)
        assert(c() == 14)

        eventually{
          assert(b() == 1)
          assert(c() == 2)
        }(patience)

        assert(count == 4)
      }


    }
    "delayed" - {
      "simple" in {
        val a = Var(10)
        val b = a.delay(100 millis)

        a() = 5
        assert(b() == 10)
        eventually{
          assert(b() == 5)
        }(patience)

        a() = 4
        assert(b() == 5)
        eventually{
          assert(b() == 4)
        }(patience)
      }
      "longer" in {
        val a = Var(10)
        val b = a.delay(100 millis)
        val c = Rx( a() * 2 ).delay(100 millis)
        var count = 0

        a() = 5
        assert(b() == 10)
        assert(c() == 20)
        eventually{
          assert(b() == 5)
          assert(c() == 10)
        }(patience)

        a() = 4
        assert(b() == 5)
        assert(c() == 10)
        eventually{
          assert(b() == 4)
          assert(c() == 8)
        }(patience)

        a() = 7
        assert(b() == 4)
        assert(c() == 8)
        eventually{
          assert(b() == 7)
          assert(c() == 14)
        }(patience)
      }
    }
  }
}