package suggestions

import language.postfixOps
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Try, Success, Failure }
import rx.lang.scala._
import rx.lang.scala.subjects.AsyncSubject
import org.scalatest._
import gui._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import rx.lang.scala.concurrency.Schedulers

@RunWith(classOf[JUnitRunner])
class MiscTest extends FunSuite with ConcreteWikipediaApi {
  import observablex._
  test("ToObservable should convert Future[T] to Observable[T] correctly: success") {
    val p = Promise[Int]()
    val observable = ObservableEx(p.future)
    p.success(2)

    assert(observable.toBlockingObservable.single == 2, "Should provide the value the promise has succeeded with.")
  }
  
  case class TestException extends Exception

  test("ToObservable should convert Future[T] to Observable[T] correctly: failure") {
    
    val p = Promise[Int]()
    val observable = ObservableEx(p.future)
    val err = new TestException
    p.failure(err)

    try {
      observable.toBlockingObservable.single
      assert(false, "Should throw an exception.")
    } catch {
      case _: Throwable => {}
    }
  }
  
  test("Recovered should retrieve Tries of values received from a stream") {
    val obs = Observable({1},{2},{3})
    val recovered = obs.recovered
    val list = recovered.toBlockingObservable.toList
    assert(list(0) == Success(1))
    assert(list(1) == Success(2))
    assert(list(2) == Success(3))
    
    val obsEmpty = Observable(List())
    val recoveredEmpty = obsEmpty.recovered
    val listEmpty = recoveredEmpty.toBlockingObservable.toList
    
    assert(listEmpty.length == 1)
  }
  
  test("concatRecovered behaves as promised") {
    val req = Observable(1,2,3,4,5)
    val response = req.concatRecovered(num => if (num != 4) Observable(num) else Observable(new Exception))
    
    val res = response.foldLeft((0,0)) { (acc, tn) =>
      tn match {
        case Success(n) => (acc._1 + n, acc._2)
        case Failure(_) => (acc._1, acc._2 + 1)
      }
    }
    
    println(response.toBlockingObservable.toList.toString)
    
    var pair = (0, 0)
    res.observeOn(Schedulers.immediate).subscribe(e => pair = e)
    val (sum, fc) = pair
    assert(fc == 1, "Wrong failurecount: " + fc)
    assert(sum == (1 + 2 + 3 + 5), "Wrong sum: " + sum)
  }
  
  test("resources should be resolved") {
    val is = this.getClass.getResourceAsStream("/suggestions/wiki-icon.png")
    assert(is != null, "/suggestions/wiki-icon.png must exist.")
    is.close()
}

}