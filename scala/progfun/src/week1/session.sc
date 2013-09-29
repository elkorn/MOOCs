package week1

object session {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  1 + 2                                           //> res0: Int(3) = 3
  def abs(x: Double) = if (x < 0) -x else x       //> abs: (x: Double)Double
  abs(-2)                                         //> res1: Double = 2.0
  def sqrt(x: Double): Double = {
    def sqrIter(guess: Double, x: Double): Double =
      if (isGoodEnough(guess, x)) guess
      else sqrIter(improve(guess, x), x)

    def isGoodEnough(guess: Double, x: Double): Boolean =
      abs(guess * guess - x) / x < 0.001

    def improve(guess: Double, x: Double): Double =
      (guess + x / guess) / 2

    sqrIter(1.0, x)
  }                                               //> sqrt: (x: Double)Double

  sqrt(2)                                         //> res2: Double = 1.4142156862745097
  sqrt(4)                                         //> res3: Double = 2.000609756097561
  sqrt(1e-6)                                      //> res4: Double = 0.0010000001533016628
  sqrt(1e60)                                      //> res5: Double = 1.0000788456669446E30
}