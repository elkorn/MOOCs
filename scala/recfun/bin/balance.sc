object balance {
  def balance(chars: String): Boolean = {
    def balance(chars: List[Char]): Boolean = {
      def balanceIter(chars: List[Char], opened: Int): Boolean = {
        if (chars.isEmpty) opened == 0
        else if (chars.head == '(')
          balanceIter(chars.tail, opened + 1)
        else if (chars.head == ')') {
        	if(opened == 0) false
        	else balanceIter(chars.tail, opened - 1)
        } else balanceIter(chars.tail, opened)
      }
      
      balanceIter(chars, 0)
    }
    balance(chars.toList)
  }                                               //> balance: (chars: String)Boolean
  
  balance("())")                                  //> res0: Boolean = false
  balance("(if (zero? x) max (/ 1 x))")           //> res1: Boolean = true
  balance("I told him (that it’s not (yet) done). (But he wasn’t listening)")
                                                  //> res2: Boolean = true
  balance("())(")                                 //> res3: Boolean = false
}