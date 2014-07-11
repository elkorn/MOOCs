package week2

object rationals {
  new Rational(1, 2)                              //> res0: week2.Rational = 1/2
  val x = new Rational(1, 3)                      //> x  : week2.Rational = 1/3
  val y = new Rational(5, 7)                      //> y  : week2.Rational = 5/7
  val z = new Rational(3, 2)                      //> z  : week2.Rational = 3/2
  x - y - z /* functions can be used as infix operators */
                                                  //> res1: week2.Rational = -79/42
  x < z                                           //> res2: Boolean = true
  z < y                                           //> res3: Boolean = false
  x > y                                           //> res4: week2.Rational = 5/7

  x - y - z                                       //> res5: week2.Rational = -79/42
  x + z                                           //> res6: week2.Rational = 11/6

  -z                                              //> res7: week2.Rational = -3/2

  x * - x                                         //> res8: week2.Rational = -1/9
  new Rational(2)                                 //> res9: week2.Rational = 2
  new Rational(3, 4) + new Rational(1, 4)         //> res10: week2.Rational = 1
}

class Rational(x: Int, y: Int) {
  require(y != 0, "Denominator must be non-zero")
  private def gcd(a: Int, b: Int): Int = if (b == 0) a.abs else gcd(b, a % b)
  private val g = gcd(x, y)

  val numer = x / g
  val denom = y / g

  override def toString() =
    if (denom == 1 || denom == numer) (numer).toString()
    else numer + "/" + denom

  def this(x: Int) = this(x, 1)
  def +(x: Rational) = new Rational(numer * x.denom + x.numer * denom, denom * x.denom)

  def unary_- = new Rational(-numer, denom)

  def -(x: Rational) = this + -x

  def <(that: Rational) = this.numer * that.denom < that.numer * this.denom

  def >(that: Rational) = if (this < that) that else this
  def *(that: Rational) = new Rational(numer * that.numer, denom * that.denom)

}