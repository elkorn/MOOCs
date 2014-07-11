object max {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(295); 
  def max(xs: List[Int]): Int = {
    def maxIter(current: Int, rest: List[Int]): Int =
      if (rest.isEmpty)
        current
      else if (rest.head > current)
        maxIter(rest.head, rest.tail)
      else
        maxIter(current, rest.tail)
    maxIter(Int.MinValue, xs)
  };System.out.println("""max: (xs: List[Int])Int""");$skip(34); val res$0 = 
  
  max(List(-1,-2,-3,1,2,34,5));System.out.println("""res0: Int = """ + $show(res$0));$skip(19); val res$1 = 
  max(List(-1, 0));System.out.println("""res1: Int = """ + $show(res$1))}
}
