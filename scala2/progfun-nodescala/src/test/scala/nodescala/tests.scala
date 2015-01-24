package nodescala

import scala.language.postfixOps
import scala.util.{ Try, Success, Failure }
import scala.collection._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.async.Async.{ async, await }
import org.scalatest._
import NodeScala._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.util.NoSuchElementException

@RunWith(classOf[JUnitRunner])
class NodeScalaSuite extends FunSuite {

  test("A Future should always be created") {
    val always = Future.always(517)

    assert(Await.result(always, 0 nanos) == 517)
  }

  test("A Future should never be created") {
    val never = Future.never[Int]

    try {
      Await.result(never, 1 second)
      assert(false)
    } catch {
      case t: TimeoutException => // ok!
    }
  }

  test("CancellationTokenSource should allow stopping the computation") {
    val cts = CancellationTokenSource()
    val ct = cts.cancellationToken
    val p = Promise[String]()

    async {
      while (ct.nonCancelled) {
        // do work
      }

      p.success("done")
    }

    cts.unsubscribe()
    assert(Await.result(p.future, 1 second) == "done")
  }

  test("When in list there is a never() future, all() can't finish") {
    val all = Future.all(List(Future.never[Int], Future { 1 }, Future.never[Int]))

    try {
      Await.result(all, 1 second)
      assert(false, "This should not be here!")
    } catch {
      case t: TimeoutException =>
    }
  }

  test("When in list there are no never() futures, all() have to finish, with preserved order") {
    val all = Future.all(List(Future { 2 }, Future { 1 }, Future { 3 }))

    try {
      assert(Await.result(all, 1 second) == List(2, 1, 3), "Result has to a list with specified order.")
    } catch {
      case t: TimeoutException => assert(false, "This should not timed out!")
    }
  }
  
  test("`now` works as intended") {
    val immediate = Future{1}
    assert(immediate.now == 1, "Must return the value of a resolved future.")
    
    val delayed = Future {
      Thread.sleep(10000)
    }
    
    try {
      delayed.now
    } catch {
      case e: NoSuchElementException =>	// OK!
      case _ => fail("Must throw NoSuchElementException.")
    }
  }
  
  test("ContinueWith works as intended") {
    val f1 = future{1}
    val partial = 2
    val expectedResult = 3
    
    val resultFuture = f1.continueWith(f => (f.value.get.get + partial))
    
    assert(Await.result(resultFuture, 10 millis) == expectedResult, "Must propagate the result to a next future.")
  }
  
 
  test("Continue works as intended") {
    val f1 = future{1}
    val partial = 2
    val expectedResult = 3
    
    val resultFuture = f1.continue(v => (v.get + partial))
    
    assert(Await.result(resultFuture, 10 millis) == expectedResult, "Must propagate the result to a next future.")
  }

  test("Cancellation token can be used as described") {
    var subscribed = true
    val working = Future.run() { ct =>
      Future {
        while (ct.nonCancelled) {
          assert(ct.nonCancelled == subscribed)
        }

        assert(ct.nonCancelled == subscribed)
      }
    }

    Future.delay(5 seconds) onSuccess {
      case _ => {
        subscribed = false
        working.unsubscribe()
      }
    }
  }

  class DummyExchange(val request: Request) extends Exchange {
    @volatile var response = ""
    val loaded = Promise[String]()
    def write(s: String) {
      response += s
    }
    def close() {
      loaded.success(response)
    }
  }

  class DummyListener(val port: Int, val relativePath: String) extends NodeScala.Listener {
    self =>

    @volatile private var started = false
    var handler: Exchange => Unit = null

    def createContext(h: Exchange => Unit) = this.synchronized {
      assert(started, "is server started?")
      handler = h
    }

    def removeContext() = this.synchronized {
      assert(started, "is server started?")
      handler = null
    }

    def start() = self.synchronized {
      started = true
      new Subscription {
        def unsubscribe() = self.synchronized {
          started = false
        }
      }
    }

    def emit(req: Request) = {
      val exchange = new DummyExchange(req)
      if (handler != null) handler(exchange)
      exchange
    }
  }

  class DummyServer(val port: Int) extends NodeScala {
    self =>
    val listeners = mutable.Map[String, DummyListener]()

    def createListener(relativePath: String) = {
      val l = new DummyListener(port, relativePath)
      listeners(relativePath) = l
      l
    }

    def emit(relativePath: String, req: Request) = this.synchronized {
      val l = listeners(relativePath)
      l.emit(req)
    }
  }

  test("Listener should serve the next request as a future") {
    val dummy = new DummyListener(8191, "/test")
    val subscription = dummy.start()

    def test(req: Request) {
      val f = dummy.nextRequest()
      dummy.emit(req)
      val (reqReturned, xchg) = Await.result(f, 1 second)

      assert(reqReturned == req)
    }

    test(immutable.Map("StrangeHeader" -> List("StrangeValue1")))
    test(immutable.Map("StrangeHeader" -> List("StrangeValue2")))

    subscription.unsubscribe()
  }

  test("Server should serve requests") {
    val dummy = new DummyServer(8191)
    val dummySubscription = dummy.start("/testDir") {
      request => for (kv <- request.iterator) yield (kv + "\n").toString
    }

    // wait until server is really installed
    Thread.sleep(500)

    def test(req: Request) {
      val webpage = dummy.emit("/testDir", req)
      val content = Await.result(webpage.loaded.future, 1 second)
      val expected = (for (kv <- req.iterator) yield (kv + "\n").toString).mkString
      assert(content == expected, s"'$content' vs. '$expected'")
    }

    test(immutable.Map("StrangeRequest" -> List("Does it work?")))
    test(immutable.Map("StrangeRequest" -> List("It works!")))
    test(immutable.Map("WorksForThree" -> List("Always works. Trust me.")))

    dummySubscription.unsubscribe()
  }

}




