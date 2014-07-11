package week2

object rationals {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(54); val res$0 = 
  new Rational(1, 2);System.out.println("""res0: week2.Rational = """ + $show(res$0));$skip(29); 
  val x = new Rational(1, 3);System.out.println("""x  : week2.Rational = """ + $show(x ));$skip(29); 
  val y = new Rational(5, 7);System.out.println("""y  : week2.Rational = """ + $show(y ));$skip(29); 
  val z = new Rational(3, 2);System.out.println("""z  : week2.Rational = """ + $show(z ));$skip(59); val res$1 = 
  x - y - z;System.out.println("""res1: week2.Rational = """ + $show(res$1));$skip(8); val res$2 =  /* functions can be used as infix operators */
  x < z;System.out.println("""res2: Boolean = """ + $show(res$2));$skip(8); val res$3 = 
  z < y;System.out.println("""res3: Boolean = """ + $show(res$3));$skip(8); val res$4 = 
  x > y;System.out.println("""res4: week2.Rational = """ + $show(res$4));$skip(13); val res$5 = 

  x - y - z;System.out.println("""res5: week2.Rational = """ + $show(res$5));$skip(8); val res$6 = 
  x + z;System.out.println("""res6: week2.Rational = """ + $show(res$6));$skip(6); val res$7 = 

  -z;System.out.println("""res7: week2.Rational = """ + $show(res$7));$skip(11); val res$8 = 

  x * - x;System.out.println("""res8: week2.Rational = """ + $show(res$8));$skip(18); val res$9 = 
  new Rational(2);System.out.println("""res9: week2.Rational = """ + $show(res$9));$skip(42); val res$10 = 
  new Rational(3, 4) + new Rational(1, 4);System.out.println("""res10: week2.Rational = """ + $show(res$10))}
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
