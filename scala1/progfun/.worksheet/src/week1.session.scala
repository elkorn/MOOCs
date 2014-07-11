package week1

object session {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(75); 
  println("Welcome to the Scala worksheet");$skip(8); val res$0 = 
  1 + 2;System.out.println("""res0: Int(3) = """ + $show(res$0));$skip(44); 
  def abs(x: Double) = if (x < 0) -x else x;System.out.println("""abs: (x: Double)Double""");$skip(10); val res$1 = 
  abs(-2);System.out.println("""res1: Double = """ + $show(res$1));$skip(375); 
  def sqrt(x: Double): Double = {
    def sqrIter(guess: Double, x: Double): Double =
      if (isGoodEnough(guess, x)) guess
      else sqrIter(improve(guess, x), x)

    def isGoodEnough(guess: Double, x: Double): Boolean =
      abs(guess * guess - x) / x < 0.001

    def improve(guess: Double, x: Double): Double =
      (guess + x / guess) / 2

    sqrIter(1.0, x)
  };System.out.println("""sqrt: (x: Double)Double""");$skip(11); val res$2 = 

  sqrt(2);System.out.println("""res2: Double = """ + $show(res$2));$skip(10); val res$3 = 
  sqrt(4);System.out.println("""res3: Double = """ + $show(res$3));$skip(13); val res$4 = 
  sqrt(1e-6);System.out.println("""res4: Double = """ + $show(res$4));$skip(13); val res$5 = 
  sqrt(1e60);System.out.println("""res5: Double = """ + $show(res$5))}
}
