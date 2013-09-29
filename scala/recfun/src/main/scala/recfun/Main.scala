package recfun
import common._

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int =
    if (r == 0 || r == 1 || c == 0 || c == r)
      1
    else
      pascal(c - 1, r - 1) + pascal(c, r - 1)

  /**
   * Exercise 2
   */
  def balance(chars: List[Char]): Boolean = {
    def balanceIter(chars: List[Char], opened: Int): Boolean = {
      if (chars.isEmpty) opened == 0
      else if (chars.head == '(')
        balanceIter(chars.tail, opened + 1)
      else if (chars.head == ')') {
        if (opened == 0) false
        else balanceIter(chars.tail, opened - 1)
      } else balanceIter(chars.tail, opened)
    }

    balanceIter(chars, 0)
  }

  /**
   * Exercise 3
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    val ways = Array.fill(money + 1)(0)
    ways(0) = 1
    coins.foreach(coin =>
      for (j <- coin to money)
        ways(j) = ways(j) + ways(j - coin))
    ways(money)
  }

  Main.main(Array("test"));
}