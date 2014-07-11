object balance {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(539); 
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
  };System.out.println("""balance: (chars: String)Boolean""");$skip(20); val res$0 = 
  
  balance("())");System.out.println("""res0: Boolean = """ + $show(res$0));$skip(40); val res$1 = 
  balance("(if (zero? x) max (/ 1 x))");System.out.println("""res1: Boolean = """ + $show(res$1));$skip(78); val res$2 = 
  balance("I told him (that it’s not (yet) done). (But he wasn’t listening)");System.out.println("""res2: Boolean = """ + $show(res$2));$skip(50); val res$3 = 
            balance("(if (zero? x) max (/ 1 x))");System.out.println("""res3: Boolean = """ + $show(res$3));$skip(18); val res$4 = 
  balance("())(");System.out.println("""res4: Boolean = """ + $show(res$4));$skip(21); val res$5 = 
  
  balance("())(");System.out.println("""res5: Boolean = """ + $show(res$5))}
}
