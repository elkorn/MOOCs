package greeter

object Sheet {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(77); 
  println("Welcome to the Scala worksheet\n");$skip(30); 
  def increase(i:Int) = i + 1;System.out.println("""increase: (i: Int)Int""");$skip(15); val res$0 = 
 s increase(1);System.out.println("""res0: <error> = """ + $show(res$0))}
}
