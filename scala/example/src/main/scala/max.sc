object max {
  def max(xs: List[Int]): Int = {
    def maxIter(current: Int, rest: List[Int]): Int =
      if (rest.isEmpty)
        current
      else if (rest.head > current)
        maxIter(rest.head, rest.tail)
      else
        maxIter(current, rest.tail)
    maxIter(Int.MinValue, xs)
  }                                               //> max: (xs: List[Int])Int
  
  max(List(-1,-2,-3,1,2,34,5))                    //> res0: Int = 34
  max(List(-1, 0))                                //> res1: Int = 0
}