object sum {
    def sum(xs: List[Int]): Int = {
    def sumIter(current: Int, rest: List[Int]): Int =
      if (rest.isEmpty)
        current
      else
        sumIter(current + rest.head, rest.tail)

    sumIter(0, xs)
  }                                               //> sum: (xs: List[Int])Int
  
  sum(List(1,2,3,4,5))                            //> res0: Int = 15
  sum(List())                                     //> res1: Int = 0
  sum(List(-10,-2,3))                             //> res2: Int = -9
}