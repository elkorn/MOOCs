object countChange {
  def countChange(amount: Int, coins:List[Int]) = {
	  val ways = Array.fill(amount + 1)(0)
	  ways(0) = 1
	  coins.foreach (coin =>
	  for (j<-coin to amount)
		  ways(j) =  ways(j) + ways(j - coin)
		  )
	ways(amount)
  }                                               //> countChange: (amount: Int, coins: List[Int])Int
 
countChange(300,List(5,10,20,50,100,200,500))     //> res0: Int = 1022
countChange(301,List(5,10,20,50,100,200,500))     //> res1: Int = 0
countChange(300,List(500,5,50,100,20,200,10))     //> res2: Int = 1022
countChange(4,List(1,2))                          //> res3: Int = 3
}