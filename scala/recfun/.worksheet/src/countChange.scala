object countChange {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(244); 
  def countChange(amount: Int, coins:List[Int]) = {
	  val ways = Array.fill(amount + 1)(0)
	  ways(0) = 1
	  coins.foreach (coin =>
	  for (j<-coin to amount)
		  ways(j) =  ways(j) + ways(j - coin)
		  )
	ways(amount)
  };System.out.println("""countChange: (amount: Int, coins: List[Int])Int""");$skip(48); val res$0 = 
 
countChange(300,List(5,10,20,50,100,200,500));System.out.println("""res0: Int = """ + $show(res$0));$skip(46); val res$1 = 
countChange(301,List(5,10,20,50,100,200,500));System.out.println("""res1: Int = """ + $show(res$1));$skip(46); val res$2 = 
countChange(300,List(500,5,50,100,20,200,10));System.out.println("""res2: Int = """ + $show(res$2));$skip(25); val res$3 = 
countChange(4,List(1,2));System.out.println("""res3: Int = """ + $show(res$3))}
}
