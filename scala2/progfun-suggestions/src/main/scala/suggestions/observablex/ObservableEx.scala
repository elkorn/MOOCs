package suggestions
package observablex

import scala.concurrent.{ Future, ExecutionContext }
import scala.util._
import scala.util.Success
import scala.util.Failure
import java.lang.Throwable
import rx.lang.scala.Observable
import rx.lang.scala.Scheduler
import rx.Observer
import rx.lang.scala.subscriptions.Subscription
import rx.lang.scala.subjects.AsyncSubject

object ObservableEx {

  /**
   * Returns an observable stream of values produced by the given future.
   * If the future fails, the observable will fail as well.
   *
   * @param f future whose values end up in the resulting observable
   * @return an observable completed after producing the value of the future, or with an exception
   */
  def apply[T](f: Future[T])(implicit execContext: ExecutionContext): Observable[T] = {
    val subject = AsyncSubject[T]
    f.onSuccess(x => x match {
      case s => {
        subject.onNext(s)
        subject.onCompleted()
      }
    })
    
    f.onFailure(x => x match {
      case error => {
        subject.onError(error)
      }
    })
    
    subject
  }

}