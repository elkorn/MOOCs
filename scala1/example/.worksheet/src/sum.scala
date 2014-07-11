object sum {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(225); 
    def sum(xs: List[Int]): Int = {
    def sumIter(current: Int, rest: List[Int]): Int =
      if (rest.isEmpty)
        current
      else
        sumIter(current + rest.head, rest.tail)

    sumIter(0, xs)
  };System.out.println("""sum: (xs: List[Int])Int""");$skip(26); val res$0 = 
  
  sum(List(1,2,3,4,5));System.out.println("""res0: Int = """ + $show(res$0));$skip(14); val res$1 = 
  sum(List());System.out.println("""res1: Int = """ + $show(res$1));$skip(22); val res$2 = 
  sum(List(-10,-2,3));System.out.println("""res2: Int = """ + $show(res$2))}
}
